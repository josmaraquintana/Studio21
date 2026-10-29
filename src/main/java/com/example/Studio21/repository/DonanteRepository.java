package com.example.Studio21.repository;

import com.example.Studio21.model.Donante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonanteRepository extends JpaRepository<Donante, Long> {

    // ORDER BY nombre ASC
    List<Donante> findAllByOrderByNombreAsc();
}
