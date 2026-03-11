package com.gymmanager.service;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.InvalidOperationException;
import com.gymmanager.entity.Membresia;
import com.gymmanager.entity.Pago;

public interface EmailService {
    void enviarConfirmacionPago(Pago pago);
    void enviarAvisoVencimiento(Membresia membresia);
}