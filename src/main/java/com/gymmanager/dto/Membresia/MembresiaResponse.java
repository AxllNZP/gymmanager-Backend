package com.gymmanager.dto.Membresia;

import com.gymmanager.dto.Cliente.ClienteResponse;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MembresiaResponse {
    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteDni;
    private Long planId;
    private String planNombre;
    private Integer planNumeroPersonas;
    private Double planPrecio;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private LocalDateTime createdAt;
    private List<ClienteResponse> clientesAdicionales;
}