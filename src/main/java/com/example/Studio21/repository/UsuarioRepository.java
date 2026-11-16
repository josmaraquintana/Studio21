package com.example.Studio21.repository;

import com.example.Studio21.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> buscarParaActualizar(@Param("id") Long id);

    // Usado en el login para encontrar al usuario por su correo.
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    //Checa si alguien mas ya usa el correo y nos ayuda por si se quiere editar la cuenta
    boolean existsByCorreoAndIdNot(String correo, Long id);
}
