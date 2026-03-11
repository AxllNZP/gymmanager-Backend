package com.gymmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private String nombre;
    private String role;

    //Los DTOs son objetos que transportan datos
    // entre el frontend y backend, sin exponer
    // las entidades directamente.
}