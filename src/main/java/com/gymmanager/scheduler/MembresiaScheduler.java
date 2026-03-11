package com.gymmanager.scheduler;

import com.gymmanager.entity.Membresia;
import com.gymmanager.repository.MembresiaRepository;
import com.gymmanager.service.EmailService;
import com.gymmanager.service.MembresiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembresiaScheduler {

    private final MembresiaService membresiaService;
    private final MembresiaRepository membresiaRepository;
    private final EmailService emailService;

    // Ejecuta todos los días a las 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void actualizarEstadosMembresias() {
        log.info(">>> Scheduler: Actualizando estados de membresías...");
        membresiaService.actualizarEstados();
        log.info(">>> Scheduler: Estados actualizados correctamente");
    }

    // Ejecuta todos los días a las 9:00 AM — envía avisos de vencimiento
    @Scheduled(cron = "0 0 9 * * *")
    public void enviarAvisosVencimiento() {
        log.info(">>> Scheduler: Enviando avisos de vencimiento...");

        // Avisar 5 días antes
        LocalDate fechaAviso = LocalDate.now().plusDays(5);
        List<Membresia> porVencer = membresiaRepository.findByFechaFin(fechaAviso);

        porVencer.forEach(membresia -> {
            try {
                emailService.enviarAvisoVencimiento(membresia);
                log.info("Aviso enviado a: {}", membresia.getCliente().getEmail());
            } catch (Exception e) {
                log.error("Error enviando aviso a {}: {}",
                        membresia.getCliente().getEmail(), e.getMessage());
            }
        });

        log.info(">>> Scheduler: {} avisos enviados", porVencer.size());
    }
}