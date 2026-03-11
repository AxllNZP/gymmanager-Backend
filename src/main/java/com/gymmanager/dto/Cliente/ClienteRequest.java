package com.gymmanager.dto.Cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    private String telefono;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String fotoUrl;

    // Datos físicos
    private Double peso;
    private Double talla;
    private String datosMedicos;

    @NotNull(message = "El consentimiento es obligatorio")
    private Boolean consentimientoDatosSensibles;
}