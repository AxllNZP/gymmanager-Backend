package com.gymmanager.service;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.InvalidOperationException;

import com.gymmanager.dto.Asistencia.AsistenciaRequest;
import com.gymmanager.dto.Asistencia.AsistenciaResponse;
import java.util.List;

public interface AsistenciaService {
    AsistenciaResponse registrarEntrada(AsistenciaRequest request);
    AsistenciaResponse registrarSalida(Long clienteId);
    List<AsistenciaResponse> listarTodas();
    List<AsistenciaResponse> listarPorCliente(Long clienteId);
    List<AsistenciaResponse> listarHoy();
}