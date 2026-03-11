package com.gymmanager.dto.Usuario;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;
    private String role;
    private Boolean activo;
    private LocalDateTime createdAt;
    //Los DTOs son objetos que transportan datos
    // entre el frontend y backend, sin exponer
    // las entidades directamente.
}