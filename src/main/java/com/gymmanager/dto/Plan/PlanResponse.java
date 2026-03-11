package com.gymmanager.dto.Plan;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlanResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer numeroPersonas;
    private Double precio;
    private Boolean activo;
    private LocalDateTime createdAt;
}