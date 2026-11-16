package com.example.Studio21.repository;

import com.example.Studio21.model.Direccion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    List<Direccion> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    Optional<Direccion> findByIdAndUsuarioId(Long id, Long usuarioId);
}
