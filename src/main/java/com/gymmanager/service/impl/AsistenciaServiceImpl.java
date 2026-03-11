package com.gymmanager.service.impl;

import com.gymmanager.dto.Asistencia.AsistenciaRequest;
import com.gymmanager.dto.Asistencia.AsistenciaResponse;
import com.gymmanager.entity.Asistencia;
import com.gymmanager.entity.Cliente;
import com.gymmanager.entity.Membresia;
import com.gymmanager.repository.AsistenciaRepository;
import com.gymmanager.repository.ClienteRepository;
import com.gymmanager.repository.MembresiaRepository;
import com.gymmanager.service.AsistenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final ClienteRepository clienteRepository;
    private final MembresiaRepository membresiaRepository;

    @Override
    public AsistenciaResponse registrarEntrada(AsistenciaRequest request) {

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Verificar membresía activa o por vencer
        Membresia membresia = membresiaRepository
                .findByClienteAndEstado(cliente, Membresia.EstadoMembresia.ACTIVA)
                .orElseGet(() -> membresiaRepository
                        .findByClienteAndEstado(cliente, Membresia.EstadoMembresia.POR_VENCER)
                        .orElseThrow(() -> new RuntimeException(
                                "El cliente no tiene membresía activa. " +
                                        "Por favor renueve su suscripción en recepción.")));

        // Verificar que no tenga una entrada sin salida registrada hoy
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<Asistencia> hoy = asistenciaRepository
                .findByClienteAndFechaEntradaBetween(cliente, inicioDia, finDia);

        boolean tieneEntradaAbierta = hoy.stream()
                .anyMatch(a -> a.getFechaSalida() == null);

        if (tieneEntradaAbierta) {
            throw new RuntimeException("El cliente ya tiene una entrada registrada hoy sin salida");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setCliente(cliente);
        asistencia.setMembresia(membresia);
        asistencia.setRegistradoPor(
                request.getRegistradoPor() != null
                        ? request.getRegistradoPor()
                        : "RECEPCION");

        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Override
    public AsistenciaResponse registrarSalida(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<Asistencia> hoy = asistenciaRepository
                .findByClienteAndFechaEntradaBetween(cliente, inicioDia, finDia);

        Asistencia asistenciaAbierta = hoy.stream()
                .filter(a -> a.getFechaSalida() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró entrada abierta para este cliente hoy"));

        asistenciaAbierta.setFechaSalida(LocalDateTime.now());
        return toResponse(asistenciaRepository.save(asistenciaAbierta));
    }

    @Override
    public List<AsistenciaResponse> listarTodas() {
        return asistenciaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsistenciaResponse> listarPorCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return asistenciaRepository.findByCliente(cliente)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsistenciaResponse> listarHoy() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);
        return asistenciaRepository
                .findByFechaEntradaBetween(inicioDia, finDia)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AsistenciaResponse toResponse(Asistencia a) {
        AsistenciaResponse response = new AsistenciaResponse();
        response.setId(a.getId());
        response.setClienteId(a.getCliente().getId());
        response.setClienteNombre(a.getCliente().getNombre());
        response.setClienteApellido(a.getCliente().getApellido());
        response.setClienteDni(a.getCliente().getDni());
        response.setMembresiaId(a.getMembresia().getId());
        response.setEstadoMembresia(a.getMembresia().getEstado().name());
        response.setFechaEntrada(a.getFechaEntrada());
        response.setFechaSalida(a.getFechaSalida());
        response.setRegistradoPor(a.getRegistradoPor());
        return response;
    }
}