package com.gymmanager.service;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.InvalidOperationException;
import com.gymmanager.dto.Cliente.ClienteRequest;
import com.gymmanager.dto.Cliente.ClienteResponse;
import java.util.List;

public interface ClienteService {
    ClienteResponse crear(ClienteRequest request);
    List<ClienteResponse> listarTodos();
    List<ClienteResponse> listarActivos();
    ClienteResponse obtenerPorId(Long id);
    ClienteResponse obtenerPorDni(String dni);
    ClienteResponse actualizar(Long id, ClienteRequest request);
    void desactivar(Long id);
}