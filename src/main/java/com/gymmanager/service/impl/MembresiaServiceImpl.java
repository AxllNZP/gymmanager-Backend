package com.gymmanager.service.impl;

import com.gymmanager.dto.Membresia.MembresiaRequest;
import com.gymmanager.dto.Membresia.MembresiaResponse;
import com.gymmanager.dto.Cliente.ClienteResponse;
import com.gymmanager.dto.RenovacionRequest;
import com.gymmanager.entity.*;
import com.gymmanager.repository.*;
import com.gymmanager.service.MembresiaService;
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
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        // Verificar que no tenga membresía activa
        membresiaRepository.findByClienteAndEstado(cliente, Membresia.EstadoMembresia.ACTIVA)
                .ifPresent(m -> {
                    throw new RuntimeException("El cliente ya tiene una membresía activa");
                });

        // Validar cantidad de clientes adicionales según el plan
        List<Cliente> adicionales = resolverClientesAdicionales(
                request.getClientesAdicionalesIds(), plan.getNumeroPersonas());

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
        Membresia membresiaAnterior = membresiaRepository.findById(membresiaId)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        // Marcar la anterior como expirada
        membresiaAnterior.setEstado(Membresia.EstadoMembresia.EXPIRADA);
        membresiaRepository.save(membresiaAnterior);

        // Validar clientes adicionales según nuevo plan
        List<Cliente> adicionales = resolverClientesAdicionales(
                request.getClientesAdicionalesIds(), plan.getNumeroPersonas());

        // Crear nueva membresía
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
        Membresia membresia = membresiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));
        return toResponse(membresia);
    }

    @Override
    public List<MembresiaResponse> obtenerPorCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return membresiaRepository.findByCliente(cliente)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembresiaResponse> listarTodas() {
        return membresiaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembresiaResponse> listarActivas() {
        return membresiaRepository
                .findAll()
                .stream()
                .filter(m -> m.getEstado() == Membresia.EstadoMembresia.ACTIVA ||
                        m.getEstado() == Membresia.EstadoMembresia.POR_VENCER)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void actualizarEstados() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(5);

        // Expirar membresías vencidas
        List<Membresia> expiradas = membresiaRepository.findExpiradas(hoy);
        expiradas.forEach(m -> m.setEstado(Membresia.EstadoMembresia.EXPIRADA));
        membresiaRepository.saveAll(expiradas);

        // Marcar por vencer (5 días o menos)
        List<Membresia> porVencer = membresiaRepository.findPorVencer(hoy, limite);
        porVencer.forEach(m -> m.setEstado(Membresia.EstadoMembresia.POR_VENCER));
        membresiaRepository.saveAll(porVencer);
    }

    private List<Cliente> resolverClientesAdicionales(
            List<Long> ids, int numeroPersonas) {

        List<Cliente> adicionales = new ArrayList<>();

        if (ids != null && !ids.isEmpty()) {
            int maximoAdicionales = numeroPersonas - 1;

            if (ids.size() > maximoAdicionales) {
                throw new RuntimeException("El plan permite máximo "
                        + maximoAdicionales + " cliente(s) adicional(es)");
            }

            for (Long id : ids) {
                Cliente c = clienteRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(
                                "Cliente adicional no encontrado: " + id));
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
}