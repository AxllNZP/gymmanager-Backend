package com.gymmanager.service;

import com.gymmanager.entity.Membresia;
import com.gymmanager.entity.Pago;

public interface EmailService {
    void enviarConfirmacionPago(Pago pago);
    void enviarAvisoVencimiento(Membresia membresia);
}