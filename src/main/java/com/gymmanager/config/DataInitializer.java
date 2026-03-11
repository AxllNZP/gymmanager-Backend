package com.gymmanager.config;

import com.gymmanager.entity.Plan;
import com.gymmanager.entity.Role;
import com.gymmanager.entity.Usuario;
import com.gymmanager.repository.PlanRepository;
import com.gymmanager.repository.RoleRepository;
import com.gymmanager.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        inicializarRoles();
        inicializarAdmin();
        inicializarPlanes();
    }

    private void inicializarRoles() {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
        log.info(">>> Roles inicializados correctamente");
    }

    private void inicializarAdmin() {
        if (usuarioRepository.findByEmail("axellzurita1003@gmail.com").isEmpty()) {
            Role adminRole = roleRepository
                    .findByName(Role.RoleName.ADMIN)
                    .orElseThrow();

            Usuario admin = new Usuario();
            admin.setNombre("Axell Zurita");
            admin.setEmail("axellzurita1003@gmail.com");
            admin.setPassword(passwordEncoder.encode("Admin2026*"));
            admin.setRole(adminRole);
            admin.setActivo(true);

            usuarioRepository.save(admin);
            log.info(">>> Admin creado: axellzurita1003@gmail.com / Admin2026*");
        }
    }

    private void inicializarPlanes() {
        if (planRepository.findByActivoTrue().isEmpty()) {

            List<Plan> planes = List.of(
                    crearPlan(
                            "Plan Individual",
                            "Acceso al gimnasio para 1 persona",
                            1, 80.00),
                    crearPlan(
                            "Plan Duo",
                            "Acceso al gimnasio para 2 personas",
                            2, 140.00),
                    crearPlan(
                            "Plan Familiar",
                            "Acceso al gimnasio para 3 personas",
                            3, 200.00)
            );

            planRepository.saveAll(planes);
            log.info(">>> Planes base de Olympus Gym inicializados correctamente");
        }
    }

    private Plan crearPlan(String nombre, String descripcion,
                           int personas, double precio) {
        Plan plan = new Plan();
        plan.setNombre(nombre);
        plan.setDescripcion(descripcion);
        plan.setNumeroPersonas(personas);
        plan.setPrecio(precio);
        plan.setActivo(true);
        return plan;
    }
}