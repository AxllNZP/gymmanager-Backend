package com.gymmanager.service;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.InvalidOperationException;
import com.gymmanager.dto.Usuario.UsuarioRequest;
import com.gymmanager.dto.Usuario.UsuarioResponse;
import java.util.List;

public interface UsuarioService {
    UsuarioResponse crear(UsuarioRequest request);
    List<UsuarioResponse> listarTodos();
    UsuarioResponse obtenerPorId(Long id);
    UsuarioResponse actualizar(Long id, UsuarioRequest request);
    void desactivar(Long id);
    void desbloquear(Long id);
}