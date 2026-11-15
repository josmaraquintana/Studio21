package com.example.Studio21.security;

import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Ayuda para saber quien esta haciendo la petición. El usuario sale
 * del token, nunca de un id que mande el cliente en la URL o el body
 * así nadie se hace pasar por otro
 */
@Component
public class UsuarioActual {
    private final UsuarioRepository usuarioRepository;

    public UsuarioActual(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }
    /**Id del usuario del token, no consulta la bs**/
    public Long id(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !(auth.getPrincipal() instanceof Long id)){
            throw new AuthenticationCredentialsNotFoundException("Se requiere autenticación");

        }
        return id;
    }

    //Entidad del usuario del token, Responde 401 si la cuenta y no existe
    public Usuario obtener(){
        return usuarioRepository.findById(id()).orElseThrow(() -> new AuthenticationCredentialsNotFoundException("La cuenta de este token ya no existe"));
    }
}
