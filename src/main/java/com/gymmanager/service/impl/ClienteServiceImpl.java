package com.gymmanager.service.impl;

import com.gymmanager.dto.Cliente.ClienteRequest;
import com.gymmanager.dto.Cliente.ClienteResponse;
import com.gymmanager.entity.Cliente;
import com.gymmanager.entity.Membresia;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.repository.ClienteRepository;
import com.gymmanager.repository.MembresiaRepository;
import com.gymmanager.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final MembresiaRepository membresiaRepository;

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {

        if (clienteRepository.existsByDni(request.getDni())) {
            throw new DuplicateResourceException("Ya existe ese numero de DNI: " + request.getDni());
        }

        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe ese correo en el sistema: " + request.getNombre());
        }

        Cliente cliente = toEntity(request);
        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteResponse> listarActivos() {
        return clienteRepository.findByActivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteResponse obtenerPorId(Long id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente", "id", id));

        return toResponse(cliente);
    }

    @Override
    public ClienteResponse obtenerPorDni(String dni) {

        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente", "dni", dni));

        return toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente", "id", id));

        if (!cliente.getDni().equals(request.getDni())
                && clienteRepository.existsByDni(request.getDni())) {

            throw new DuplicateResourceException("Ya existe ese numero de DNI: " + request.getDni());
        }

        if (!cliente.getEmail().equals(request.getEmail())
                && clienteRepository.existsByEmail(request.getEmail())) {

            throw new DuplicateResourceException("Ya existe ese correo en el sistema: " + request.getNombre());
        }

        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setDni(request.getDni());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setDireccion(request.getDireccion());
        cliente.setFotoUrl(request.getFotoUrl());
        cliente.setPeso(request.getPeso());
        cliente.setTalla(request.getTalla());

        if (Boolean.TRUE.equals(request.getConsentimientoDatosSensibles())) {
            cliente.setDatosMedicos(request.getDatosMedicos());
            cliente.setConsentimientoDatosSensibles(true);
            cliente.setConsentimientoFecha(LocalDateTime.now());
        }

        return toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente", "id", id));

        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    private Cliente toEntity(ClienteRequest request) {

        Cliente cliente = new Cliente();

        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setDni(request.getDni());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setDireccion(request.getDireccion());
        cliente.setFotoUrl(request.getFotoUrl());
        cliente.setPeso(request.getPeso());
        cliente.setTalla(request.getTalla());
        cliente.setActivo(true);

        if (Boolean.TRUE.equals(request.getConsentimientoDatosSensibles())) {

            cliente.setDatosMedicos(request.getDatosMedicos());
            cliente.setConsentimientoDatosSensibles(true);
            cliente.setConsentimientoFecha(LocalDateTime.now());

        } else {

            cliente.setConsentimientoDatosSensibles(false);

        }

        return cliente;
    }

    private ClienteResponse toResponse(Cliente c) {

        ClienteResponse response = new ClienteResponse();

        response.setId(c.getId());
        response.setNombre(c.getNombre());
        response.setApellido(c.getApellido());
        response.setDni(c.getDni());
        response.setEmail(c.getEmail());
        response.setTelefono(c.getTelefono());
        response.setFechaNacimiento(c.getFechaNacimiento());
        response.setDireccion(c.getDireccion());
        response.setFotoUrl(c.getFotoUrl());
        response.setPeso(c.getPeso());
        response.setTalla(c.getTalla());
        response.setConsentimientoDatosSensibles(c.getConsentimientoDatosSensibles());
        response.setConsentimientoFecha(c.getConsentimientoFecha());
        response.setActivo(c.getActivo());
        response.setCreatedAt(c.getCreatedAt());

        if (Boolean.TRUE.equals(c.getConsentimientoDatosSensibles())) {
            response.setDatosMedicos(c.getDatosMedicos());
        }

        Optional<Membresia> membresiaActiva = membresiaRepository
                .findByClienteAndEstado(c, Membresia.EstadoMembresia.ACTIVA);

        if (membresiaActiva.isPresent()) {

            response.setEstadoMembresia(membresiaActiva.get().getEstado().name());
            response.setFechaFinMembresia(membresiaActiva.get().getFechaFin());

        } else {

            Optional<Membresia> membresiaExpirada = membresiaRepository
                    .findByClienteAndEstado(c, Membresia.EstadoMembresia.EXPIRADA);

            if (membresiaExpirada.isPresent()) {

                response.setEstadoMembresia("EXPIRADA");
                response.setFechaFinMembresia(membresiaExpirada.get().getFechaFin());

            } else {

                response.setEstadoMembresia("SIN_MEMBRESIA");

            }
        }

        return response;
    }
}