package com.gymmanager.service;

import com.gymmanager.dto.Plan.PlanRequest;
import com.gymmanager.dto.Plan.PlanResponse;
import java.util.List;

public interface PlanService {
    PlanResponse crear(PlanRequest request);
    List<PlanResponse> listarActivos();
    List<PlanResponse> listarTodos();
    PlanResponse obtenerPorId(Long id);
    PlanResponse actualizar(Long id, PlanRequest request);
    void desactivar(Long id);
    void activar(Long id);
}