package com.gymmanager.service.impl;

import com.gymmanager.entity.Membresia;
import com.gymmanager.entity.Pago;
import com.gymmanager.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void enviarConfirmacionPago(Pago pago) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(pago.getCliente().getEmail());
            helper.setSubject("Confirmación de pago - Olympus Gym");
            helper.setText(buildEmailConfirmacionPago(pago), true);

            mailSender.send(message);
            log.info("Correo de confirmación enviado a: {}", pago.getCliente().getEmail());
        } catch (Exception e) {
            log.error("Error enviando correo de confirmación: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void enviarAvisoVencimiento(Membresia membresia) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(membresia.getCliente().getEmail());
            helper.setSubject("Aviso de vencimiento de membresía - Olympus Gym");
            helper.setText(buildEmailVencimiento(membresia), true);

            mailSender.send(message);
            log.info("Correo de vencimiento enviado a: {}", membresia.getCliente().getEmail());
        } catch (Exception e) {
            log.error("Error enviando correo de vencimiento: {}", e.getMessage());
        }
    }

    private String buildEmailConfirmacionPago(Pago pago) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width:600px; margin:auto; border:1px solid #ddd; border-radius:8px; overflow:hidden;">
                    <div style="background-color:#1a1a2e; padding:20px; text-align:center;">
                        <h1 style="color:#e94560; margin:0;">OLYMPUS GYM</h1>
                        <p style="color:#fff; margin:5px 0;">Confirmación de Pago</p>
                    </div>
                    <div style="padding:30px;">
                        <p>Estimado/a <strong>%s %s</strong>,</p>
                        <p>Hemos registrado su pago exitosamente. A continuación el detalle:</p>
                        <table style="width:100%%; border-collapse:collapse; margin:20px 0;">
                            <tr style="background:#f5f5f5;">
                                <td style="padding:10px; border:1px solid #ddd;"><strong>Plan</strong></td>
                                <td style="padding:10px; border:1px solid #ddd;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding:10px; border:1px solid #ddd;"><strong>Personas</strong></td>
                                <td style="padding:10px; border:1px solid #ddd;">%d persona(s)</td>
                            </tr>
                            <tr style="background:#f5f5f5;">
                                <td style="padding:10px; border:1px solid #ddd;"><strong>Monto pagado</strong></td>
                                <td style="padding:10px; border:1px solid #ddd;">S/. %.2f</td>
                            </tr>
                            <tr>
                                <td style="padding:10px; border:1px solid #ddd;"><strong>Método de pago</strong></td>
                                <td style="padding:10px; border:1px solid #ddd;">%s</td>
                            </tr>
                            <tr style="background:#f5f5f5;">
                                <td style="padding:10px; border:1px solid #ddd;"><strong>Vigencia hasta</strong></td>
                                <td style="padding:10px; border:1px solid #ddd;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding:10px; border:1px solid #ddd;"><strong>Fecha de pago</strong></td>
                                <td style="padding:10px; border:1px solid #ddd;">%s</td>
                            </tr>
                        </table>
                        <p>Gracias por confiar en <strong>Olympus Gym</strong>. ¡Nos vemos en el gimnasio!</p>
                    </div>
                    <div style="background:#f5f5f5; padding:15px; text-align:center; font-size:12px; color:#888;">
                        <p>Olympus Gym — Lima, Perú | axellzurita1003@gmail.com</p>
                        <p>Este es un correo automático, por favor no responder.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                pago.getCliente().getNombre(),
                pago.getCliente().getApellido(),
                pago.getMembresia().getPlan().getNombre(),
                pago.getMembresia().getPlan().getNumeroPersonas(),
                pago.getMonto(),
                pago.getMetodoPago().name(),
                pago.getMembresia().getFechaFin().toString(),
                pago.getFechaPago().toLocalDate().toString()
        );
    }

    private String buildEmailVencimiento(Membresia membresia) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <div style="max-width:600px; margin:auto; border:1px solid #ddd; border-radius:8px; overflow:hidden;">
                    <div style="background-color:#1a1a2e; padding:20px; text-align:center;">
                        <h1 style="color:#e94560; margin:0;">OLYMPUS GYM</h1>
                        <p style="color:#fff; margin:5px 0;">Aviso de Vencimiento</p>
                    </div>
                    <div style="padding:30px;">
                        <p>Estimado/a <strong>%s %s</strong>,</p>
                        <p>Le informamos que su membresía vencerá el <strong>%s</strong>.</p>
                        <p>Para evitar la interrupción del servicio, puede renovar:</p>
                        <ul>
                            <li>En recepción de Olympus Gym</li>
                            <li>Contactando al administrador: axellzurita1003@gmail.com</li>
                        </ul>
                        <p><strong>Plan actual:</strong> %s — S/. %.2f</p>
                        <p>¡No pierda su acceso al gimnasio!</p>
                    </div>
                    <div style="background:#f5f5f5; padding:15px; text-align:center; font-size:12px; color:#888;">
                        <p>Olympus Gym — Lima, Perú | axellzurita1003@gmail.com</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                membresia.getCliente().getNombre(),
                membresia.getCliente().getApellido(),
                membresia.getFechaFin().toString(),
                membresia.getPlan().getNombre(),
                membresia.getPlan().getPrecio()
        );
    }
}