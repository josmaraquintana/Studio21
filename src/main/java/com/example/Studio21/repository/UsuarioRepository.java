package com.example.Studio21.repository;

import com.example.Studio21.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Usado en el login para encontrar al usuario por su correo.
    Optional<Usuario> findByCorreo(String correo);
}
