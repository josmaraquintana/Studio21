package com.example.Studio21.repository;

import com.example.Studio21.model.Direccion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    Optional<Direccion> findByIdAndUsuarioId(Long id, Long usuarioId);
}
