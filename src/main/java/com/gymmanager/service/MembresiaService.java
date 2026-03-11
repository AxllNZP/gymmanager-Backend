package com.gymmanager.service;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.InvalidOperationException;
import com.gymmanager.dto.Membresia.MembresiaRequest;
import com.gymmanager.dto.Membresia.MembresiaResponse;
import com.gymmanager.dto.RenovacionRequest;
import java.util.List;

public interface MembresiaService {
    MembresiaResponse crear(MembresiaRequest request);
    MembresiaResponse renovar(Long membresiaId, RenovacionRequest request);
    MembresiaResponse obtenerPorId(Long id);
    List<MembresiaResponse> obtenerPorCliente(Long clienteId);
    List<MembresiaResponse> listarTodas();
    List<MembresiaResponse> listarActivas();
    void actualizarEstados();
}