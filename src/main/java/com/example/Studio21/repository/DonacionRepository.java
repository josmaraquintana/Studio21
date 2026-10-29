package com.example.Studio21.repository;

import com.example.Studio21.model.Donacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonacionRepository extends JpaRepository<Donacion, Long> {

    // Donaciones más recientes primero.
    List<Donacion> findAllByOrderByFechaRecepcionDesc();
}
