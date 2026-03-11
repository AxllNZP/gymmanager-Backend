package com.gymmanager.service.impl;

import com.gymmanager.config.JwtUtil;
import com.gymmanager.dto.LoginRequest;
import com.gymmanager.dto.LoginResponse;
import com.gymmanager.entity.Usuario;
import com.gymmanager.repository.UsuarioRepository;
import com.gymmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final int MAX_INTENTOS = 5;

    @Override
    public LoginResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // Verificar si está bloqueado
        if (usuario.getBloqueadoHasta() != null &&
                usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Cuenta bloqueada. Intente nuevamente después de: "
                    + usuario.getBloqueadoHasta());
        }

        // Verificar si está activo
        if (!usuario.getActivo()) {
            throw new RuntimeException("Usuario inactivo. Contacte al administrador");
        }

        // Verificar password
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            registrarIntentoFallido(usuario);
            throw new RuntimeException("Credenciales inválidas");
        }

        // Login exitoso — resetear intentos
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getRole().getName().name()
        );

        return new LoginResponse(
                token,
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRole().getName().name()
        );
    }

    private void registrarIntentoFallido(Usuario usuario) {
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= MAX_INTENTOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(30));
            usuarioRepository.save(usuario);
            throw new RuntimeException("Cuenta bloqueada por 30 minutos por múltiples intentos fallidos");
        }

        usuarioRepository.save(usuario);
    }
}