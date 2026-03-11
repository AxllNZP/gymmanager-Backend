package com.gymmanager.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String direccion;

    @Column(name = "foto_url")
    private String fotoUrl;

    // Datos físicos (consentimiento requerido)
    private Double peso;
    private Double talla;

    @Column(name = "datos_medicos", columnDefinition = "TEXT")
    private String datosMedicos;

    @Column(name = "consentimiento_datos_sensibles")
    private Boolean consentimientoDatosSensibles = false;

    @Column(name = "consentimiento_fecha")
    private LocalDateTime consentimientoFecha;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}