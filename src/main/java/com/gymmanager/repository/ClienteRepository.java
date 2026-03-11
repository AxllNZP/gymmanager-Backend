package com.gymmanager.repository;

import com.gymmanager.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByEmail(String email);
    Boolean existsByDni(String dni);
    Boolean existsByEmail(String email);
    List<Cliente> findByActivoTrue();
}