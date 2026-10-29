package com.example.Studio21.repository;

import com.example.Studio21.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // ORDER BY nombre ASC
    List<Categoria> findAllByOrderByNombreAsc();
}
