package com.gymmanager.service.impl;

import com.gymmanager.dto.Membresia.MembresiaRequest;
import com.gymmanager.dto.Membresia.MembresiaResponse;
import com.gymmanager.dto.Cliente.ClienteResponse;
import com.gymmanager.dto.RenovacionRequest;

import com.gymmanager.entity.*;

import com.gymmanager.repository.*;

import com.gymmanager.service.MembresiaService;

import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.InvalidOperationException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembresiaServiceImpl implements MembresiaService {

    private final MembresiaRepository membresiaRepository;
    private final ClienteRepository clienteRepository;
    private final PlanRepository planRepository;

    @Override
    public MembresiaResponse crear(MembresiaRequest request) {

        Cliente cliente = getClienteOrThrow(request.getClienteId());

        Plan plan = getPlanOrThrow(request.getPlanId());

        // verificar que no tenga membresía activa
        membresiaRepository
                .findByClienteAndEstado(cliente, Membresia.EstadoMembresia.ACTIVA)
                .ifPresent(m -> {
                    throw new InvalidOperationException(
                            "El cliente ya tiene una membresía activa"
                    );
                });

        List<Cliente> adicionales = resolverClientesAdicionales(
                request.getClientesAdicionalesIds(),
                plan.getNumeroPersonas()
        );

        Membresia membresia = new Membresia();

        membresia.setCliente(cliente);
        membresia.setPlan(plan);
        membresia.setFechaInicio(LocalDate.now());
        membresia.setFechaFin(LocalDate.now().plusMonths(1));
        membresia.setEstado(Membresia.EstadoMembresia.ACTIVA);
        membresia.setClientesAdicionales(adicionales);

        return toResponse(membresiaRepository.save(membresia));
    }

    @Override
    public MembresiaResponse renovar(Long membresiaId, RenovacionRequest request) {

        Membresia membresiaAnterior = getMembresiaOrThrow(membresiaId);

        Plan plan = getPlanOrThrow(request.getPlanId());

        // marcar la anterior como expirada
        membresiaAnterior.setEstado(Membresia.EstadoMembresia.EXPIRADA);

        membresiaRepository.save(membresiaAnterior);

        List<Cliente> adicionales = resolverClientesAdicionales(
                request.getClientesAdicionalesIds(),
                plan.getNumeroPersonas()
        );

        Membresia nueva = new Membresia();

        nueva.setCliente(membresiaAnterior.getCliente());
        nueva.setPlan(plan);
        nueva.setFechaInicio(LocalDate.now());
        nueva.setFechaFin(LocalDate.now().plusMonths(1));
        nueva.setEstado(Membresia.EstadoMembresia.ACTIVA);
        nueva.setClientesAdicionales(adicionales);

        return toResponse(membresiaRepository.save(nueva));
    }

    @Override
    public MembresiaResponse obtenerPorId(Long id) {

        Membresia membresia = getMembresiaOrThrow(id);

        return toResponse(membresia);
    }

    @Override
    public List<MembresiaResponse> obtenerPorCliente(Long clienteId) {

        Cliente cliente = getClienteOrThrow(clienteId);

        return membresiaRepository
                .findByCliente(cliente)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembresiaResponse> listarTodas() {

        return membresiaRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembresiaResponse> listarActivas() {

        return membresiaRepository
                .findAll()
                .stream()
                .filter(m ->
                        m.getEstado() == Membresia.EstadoMembresia.ACTIVA ||
                                m.getEstado() == Membresia.EstadoMembresia.POR_VENCER
                )
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void actualizarEstados() {

        LocalDate hoy = LocalDate.now();

        LocalDate limite = hoy.plusDays(5);

        List<Membresia> expiradas = membresiaRepository.findExpiradas(hoy);

        expiradas.forEach(m ->
                m.setEstado(Membresia.EstadoMembresia.EXPIRADA)
        );

        membresiaRepository.saveAll(expiradas);

        List<Membresia> porVencer = membresiaRepository.findPorVencer(hoy, limite);

        porVencer.forEach(m ->
                m.setEstado(Membresia.EstadoMembresia.POR_VENCER)
        );

        membresiaRepository.saveAll(porVencer);
    }

    private List<Cliente> resolverClientesAdicionales(
            List<Long> ids,
            int numeroPersonas
    ) {

        List<Cliente> adicionales = new ArrayList<>();

        if (ids != null && !ids.isEmpty()) {

            int maximoAdicionales = numeroPersonas - 1;

            if (ids.size() > maximoAdicionales) {
                throw new InvalidOperationException(
                        "El plan permite máximo " + maximoAdicionales + " cliente(s) adicional(es)"
                );
            }

            for (Long id : ids) {

                Cliente c = getClienteOrThrow(id);

                adicionales.add(c);
            }
        }

        return adicionales;
    }

    private ClienteResponse clienteToResponse(Cliente c) {

        ClienteResponse r = new ClienteResponse();

        r.setId(c.getId());
        r.setNombre(c.getNombre());
        r.setApellido(c.getApellido());
        r.setDni(c.getDni());
        r.setEmail(c.getEmail());
        r.setTelefono(c.getTelefono());
        r.setActivo(c.getActivo());

        return r;
    }

    private MembresiaResponse toResponse(Membresia m) {

        MembresiaResponse response = new MembresiaResponse();

        response.setId(m.getId());
        response.setClienteId(m.getCliente().getId());
        response.setClienteNombre(m.getCliente().getNombre());
        response.setClienteApellido(m.getCliente().getApellido());
        response.setClienteDni(m.getCliente().getDni());
        response.setPlanId(m.getPlan().getId());
        response.setPlanNombre(m.getPlan().getNombre());
        response.setPlanNumeroPersonas(m.getPlan().getNumeroPersonas());
        response.setPlanPrecio(m.getPlan().getPrecio());
        response.setFechaInicio(m.getFechaInicio());
        response.setFechaFin(m.getFechaFin());
        response.setEstado(m.getEstado().name());
        response.setCreatedAt(m.getCreatedAt());

        if (m.getClientesAdicionales() != null) {

            response.setClientesAdicionales(
                    m.getClientesAdicionales()
                            .stream()
                            .map(this::clienteToResponse)
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    private Cliente getClienteOrThrow(Long id) {

        return clienteRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente", "id", id)
                );
    }

    private Plan getPlanOrThrow(Long id) {

        return planRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Plan", "id", id)
                );
    }

    private Membresia getMembresiaOrThrow(Long id) {

        return membresiaRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Membresia", "id", id)
                );
    }
}