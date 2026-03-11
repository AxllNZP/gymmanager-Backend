package com.gymmanager.service.impl;

import com.gymmanager.dto.Plan.PlanRequest;
import com.gymmanager.dto.Plan.PlanResponse;
import com.gymmanager.entity.Plan;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.repository.PlanRepository;
import com.gymmanager.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    public PlanResponse crear(PlanRequest request) {

        if (planRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException(
                    "Ya existe un plan con el nombre: " + request.getNombre()
            );
        }

        Plan plan = new Plan();
        plan.setNombre(request.getNombre());
        plan.setDescripcion(request.getDescripcion());
        plan.setNumeroPersonas(request.getNumeroPersonas());
        plan.setPrecio(request.getPrecio());
        plan.setActivo(true);

        Plan saved = planRepository.save(plan);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> listarActivos() {

        return planRepository.findByActivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> listarTodos() {

        return planRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse obtenerPorId(Long id) {

        Plan plan = getPlanOrThrow(id);

        return toResponse(plan);
    }

    @Override
    public PlanResponse actualizar(Long id, PlanRequest request) {

        Plan plan = getPlanOrThrow(id);

        // validar duplicado si cambia el nombre
        if (!plan.getNombre().equals(request.getNombre())
                && planRepository.existsByNombre(request.getNombre())) {

            throw new DuplicateResourceException(
                    "El plan ya existe con nombre: " + request.getNombre()
            );
        }

        plan.setNombre(request.getNombre());
        plan.setDescripcion(request.getDescripcion());
        plan.setNumeroPersonas(request.getNumeroPersonas());
        plan.setPrecio(request.getPrecio());

        Plan updated = planRepository.save(plan);

        return toResponse(updated);
    }

    @Override
    public void desactivar(Long id) {

        Plan plan = getPlanOrThrow(id);

        plan.setActivo(false);

        planRepository.save(plan);
    }

    @Override
    public void activar(Long id) {

        Plan plan = getPlanOrThrow(id);

        plan.setActivo(true);

        planRepository.save(plan);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return false;
    }

    private Plan getPlanOrThrow(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Plan", "id", id)
                );
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