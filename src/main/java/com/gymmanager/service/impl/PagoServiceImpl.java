package com.gymmanager.service.impl;

import com.gymmanager.dto.Pago.PagoRequest;
import com.gymmanager.dto.Pago.PagoResponse;
import com.gymmanager.entity.*;
import com.gymmanager.repository.*;
import com.gymmanager.service.EmailService;
import com.gymmanager.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.gymmanager.service.AuditLogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final MembresiaRepository membresiaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Override
    public PagoResponse registrar(PagoRequest request, String emailUsuario) {

        Membresia membresia = membresiaRepository.findById(request.getMembresiaId())
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Usuario usuarioRegistro = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        double montoOriginal = membresia.getPlan().getPrecio();
        double descuento = request.getDescuento() != null ? request.getDescuento() : 0.0;
        double montoFinal = montoOriginal - descuento;

        if (montoFinal < 0) {
            throw new RuntimeException("El descuento no puede ser mayor al precio del plan");
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

        // Enviar correo de confirmación de forma asíncrona
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
    public PagoResponse obtenerPorId(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        return toResponse(pago);
    }

    @Override
    public List<PagoResponse> listarTodos() {
        return pagoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoResponse> listarPorCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return pagoRepository.findByCliente(cliente)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoResponse> listarPorMes() {
        return pagoRepository.findPagosMesActual()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void anular(Long id, String emailUsuario) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        if (pago.getEstado() == Pago.EstadoPago.ANULADO) {
            throw new RuntimeException("El pago ya está anulado");
        }

        pago.setEstado(Pago.EstadoPago.ANULADO);
        pagoRepository.save(pago);
        // Al final del método anular(), antes del cierre, agrega:
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

}