package com.gymmanager.service.impl;

import com.gymmanager.dto.Plan.PlanRequest;
import com.gymmanager.dto.Plan.PlanResponse;
import com.gymmanager.entity.Plan;
import com.gymmanager.repository.PlanRepository;
import com.gymmanager.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    public PlanResponse crear(PlanRequest request) {
        Plan plan = new Plan();
        plan.setNombre(request.getNombre());
        plan.setDescripcion(request.getDescripcion());
        plan.setNumeroPersonas(request.getNumeroPersonas());
        plan.setPrecio(request.getPrecio());
        plan.setActivo(true);
        return toResponse(planRepository.save(plan));
    }

    @Override
    public List<PlanResponse> listarActivos() {
        return planRepository.findByActivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanResponse> listarTodos() {
        return planRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlanResponse obtenerPorId(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        return toResponse(plan);
    }

    @Override
    public PlanResponse actualizar(Long id, PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        plan.setNombre(request.getNombre());
        plan.setDescripcion(request.getDescripcion());
        plan.setNumeroPersonas(request.getNumeroPersonas());
        plan.setPrecio(request.getPrecio());

        return toResponse(planRepository.save(plan));
    }

    @Override
    public void desactivar(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        plan.setActivo(false);
        planRepository.save(plan);
    }

    @Override
    public void activar(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        plan.setActivo(true);
        planRepository.save(plan);
    }

    private PlanResponse toResponse(Plan p) {
        PlanResponse response = new PlanResponse();
        response.setId(p.getId());
        response.setNombre(p.getNombre());
        response.setDescripcion(p.getDescripcion());
        response.setNumeroPersonas(p.getNumeroPersonas());
        response.setPrecio(p.getPrecio());
        response.setActivo(p.getActivo());
        response.setCreatedAt(p.getCreatedAt());
        return response;
    }
}