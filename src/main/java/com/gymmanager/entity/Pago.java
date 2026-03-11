package com.gymmanager.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membresia_id", nullable = false)
    private Membresia membresia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioRegistro; // quien cobró (recepcionista/admin)

    @Column(nullable = false)
    private Double monto;

    @Column(name = "monto_original")
    private Double montoOriginal; // precio sin descuento

    private Double descuento;

    @Column(name = "motivo_descuento")
    private String motivoDescuento;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado;

    @Column(name = "referencia_externa")
    private String referenciaExterna; // para Yape/Plin futuro

    @Column(name = "correo_enviado")
    private Boolean correoEnviado = false;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @PrePersist
    public void prePersist() {
        this.fechaPago = LocalDateTime.now();
    }

    public enum MetodoPago {
        EFECTIVO,
        YAPE,
        PLIN,
        TRANSFERENCIA
    }

    public enum EstadoPago {
        COMPLETADO,
        PENDIENTE,
        ANULADO
    }
}