package com.example.Studio21.repository;

import com.example.Studio21.model.Artista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    // ORDER BY nombre ASC
    List<Artista> findAllByOrderByNombreAsc();
}
