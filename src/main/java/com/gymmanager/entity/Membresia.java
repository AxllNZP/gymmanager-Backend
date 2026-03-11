package com.gymmanager.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "membresias")
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMembresia estado;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Clientes adicionales vinculados (para planes de 2 o 3 personas)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "membresia_clientes_adicionales",
            joinColumns = @JoinColumn(name = "membresia_id"),
            inverseJoinColumns = @JoinColumn(name = "cliente_id")
    )
    private List<Cliente> clientesAdicionales;

    public enum EstadoMembresia {
        ACTIVA,
        EXPIRADA,
        POR_VENCER,   // faltan 5 días o menos
        CANCELADA
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}