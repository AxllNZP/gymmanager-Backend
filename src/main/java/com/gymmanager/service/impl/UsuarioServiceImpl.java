package com.gymmanager.service.impl;

import com.gymmanager.dto.Usuario.UsuarioRequest;
import com.gymmanager.dto.Usuario.UsuarioResponse;
import com.gymmanager.entity.Role;
import com.gymmanager.entity.Usuario;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.repository.RoleRepository;
import com.gymmanager.repository.UsuarioRepository;
import com.gymmanager.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {

        // Verificar email duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Ya existe un usuario con el email: " + request.getEmail()
            );
        }

        Role role = roleRepository.findByName(Role.RoleName.valueOf(request.getRole()))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Rol", "name", request.getRole())
                );

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(role);
        usuario.setActivo(true);

        Usuario saved = usuarioRepository.save(usuario);

        return toResponse(saved);
    }

    @Override
    public List<UsuarioResponse> listarTodos() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponse obtenerPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario", "id", id)
                );

        return toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario", "id", id)
                );

        // Validar email duplicado
        if (!usuario.getEmail().equals(request.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {

            throw new DuplicateResourceException(
                    "El email ya está en uso: " + request.getEmail()
            );
        }

        Role role = roleRepository.findByName(Role.RoleName.valueOf(request.getRole()))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Rol", "name", request.getRole())
                );

        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setRole(role);

        // Actualizar password solo si se envía
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Usuario updated = usuarioRepository.save(usuario);

        return toResponse(updated);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario", "id", id)
                );

        usuario.setActivo(false);

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void desbloquear(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario", "id", id)
                );

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        usuarioRepository.save(usuario);
    }

    private UsuarioResponse toResponse(Usuario u) {

        UsuarioResponse response = new UsuarioResponse();
        response.setId(u.getId());
        response.setNombre(u.getNombre());
        response.setEmail(u.getEmail());
        response.setRole(u.getRole().getName().name());
        response.setActivo(u.getActivo());
        response.setCreatedAt(u.getCreatedAt());

        return response;
    }
}