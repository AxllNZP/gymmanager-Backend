package com.gymmanager.service.impl;

import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.InvalidOperationException;
import com.gymmanager.dto.Pago.PagoRequest;
import com.gymmanager.dto.Pago.PagoResponse;
import com.gymmanager.entity.*;
import com.gymmanager.repository.*;
import com.gymmanager.service.EmailService;
import com.gymmanager.service.PagoService;
import com.gymmanager.service.AuditLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final MembresiaRepository membresiaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Override
    public PagoResponse registrar(PagoRequest request, String emailUsuario) {

        Membresia membresia = getMembresiaOrThrow(request.getMembresiaId());
        Cliente cliente = getClienteOrThrow(request.getClienteId());

        Usuario usuarioRegistro = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario", "email", emailUsuario)
                );

        double montoOriginal = membresia.getPlan().getPrecio();
        double descuento = request.getDescuento() != null ? request.getDescuento() : 0.0;
        double montoFinal = montoOriginal - descuento;

        if (montoFinal < 0) {
            throw new InvalidOperationException(
                    "El descuento no puede ser mayor al precio del plan"
            );
        }

        Pago pago = new Pago();
        pago.setMembresia(membresia);
        pago.setCliente(cliente);
        pago.setUsuarioRegistro(usuarioRegistro);
        pago.setMontoOriginal(montoOriginal);
        pago.setDescuento(descuento);
        pago.setMonto(montoFinal);
        pago.setMotivoDescuento(request.getMotivoDescuento());
        pago.setMetodoPago(Pago.MetodoPago.valueOf(request.getMetodoPago()));
        pago.setEstado(Pago.EstadoPago.COMPLETADO);
        pago.setCorreoEnviado(false);

        Pago pagoGuardado = pagoRepository.save(pago);

        // envío de correo
        emailService.enviarConfirmacionPago(pagoGuardado);

        pagoGuardado.setCorreoEnviado(true);
        pagoRepository.save(pagoGuardado);

        auditLogService.registrar(
                emailUsuario,
                "REGISTRO_PAGO",
                "Pago",
                String.valueOf(pagoGuardado.getId()),
                "Pago registrado para cliente: " + cliente.getNombre()
                        + " | Plan: " + membresia.getPlan().getNombre()
                        + " | Monto: S/. " + montoFinal,
                null,
                "EXITO"
        );

        return toResponse(pagoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(Long id) {

        Pago pago = getPagoOrThrow(id);

        return toResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarTodos() {

        return pagoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorCliente(Long clienteId) {

        Cliente cliente = getClienteOrThrow(clienteId);

        return pagoRepository.findByCliente(cliente)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorMes() {

        return pagoRepository.findPagosMesActual()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void anular(Long id, String emailUsuario) {

        Pago pago = getPagoOrThrow(id);

        if (pago.getEstado() == Pago.EstadoPago.ANULADO) {
            throw new InvalidOperationException("El pago ya está anulado");
        }

        pago.setEstado(Pago.EstadoPago.ANULADO);

        pagoRepository.save(pago);

        auditLogService.registrar(
                emailUsuario,
                "ANULACION_PAGO",
                "Pago",
                String.valueOf(id),
                "Pago anulado",
                null,
                "EXITO"
        );
    }

    private PagoResponse toResponse(Pago p) {

        PagoResponse response = new PagoResponse();

        response.setId(p.getId());
        response.setMembresiaId(p.getMembresia().getId());
        response.setClienteId(p.getCliente().getId());
        response.setClienteNombre(p.getCliente().getNombre());
        response.setClienteApellido(p.getCliente().getApellido());
        response.setPlanNombre(p.getMembresia().getPlan().getNombre());
        response.setMontoOriginal(p.getMontoOriginal());
        response.setDescuento(p.getDescuento());
        response.setMonto(p.getMonto());
        response.setMotivoDescuento(p.getMotivoDescuento());
        response.setMetodoPago(p.getMetodoPago().name());
        response.setEstado(p.getEstado().name());
        response.setCorreoEnviado(p.getCorreoEnviado());
        response.setUsuarioRegistroNombre(p.getUsuarioRegistro().getNombre());
        response.setFechaPago(p.getFechaPago());

        return response;
    }

    private Pago getPagoOrThrow(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pago", "id", id)
                );
    }

    private Cliente getClienteOrThrow(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente", "id", id)
                );
    }

    private Membresia getMembresiaOrThrow(Long id) {
        return membresiaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Membresia", "id", id)
                );
    }

}