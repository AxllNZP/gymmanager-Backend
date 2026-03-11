package com.gymmanager.service;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.InvalidOperationException;
import com.gymmanager.dto.Pago.PagoRequest;
import com.gymmanager.dto.Pago.PagoResponse;
import java.util.List;

public interface PagoService {
    PagoResponse registrar(PagoRequest request, String emailUsuario);
    PagoResponse obtenerPorId(Long id);
    List<PagoResponse> listarTodos();
    List<PagoResponse> listarPorCliente(Long clienteId);
    List<PagoResponse> listarPorMes();
    void anular(Long id, String emailUsuario);
}