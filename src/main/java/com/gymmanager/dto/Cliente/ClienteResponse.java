package com.gymmanager.dto.Cliente;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClienteResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String fotoUrl;
    private Double peso;
    private Double talla;
    private String datosMedicos;
    private Boolean consentimientoDatosSensibles;
    private LocalDateTime consentimientoFecha;
    private Boolean activo;
    private LocalDateTime createdAt;

    // Estado de membresía actual (para el dashboard)
    private String estadoMembresia;
    private LocalDate fechaFinMembresia;
}