package com.example.Studio21.service;

import com.example.Studio21.enums.RolUsuario;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// Spring Security llama a loadUserByUsername() en el login para validar las credenciales contra la base de datos.
@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Busca al usuario por correo; Spring Security compara la contraseña con el hash usando BCrypt.
    // Solo los administradores pueden entrar al panel.
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        if (usuario.getRol() != RolUsuario.ADMIN) {
            // Mismo mensaje: no se le dan pistas a un atacante.
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getContraseniaHash())
                .roles(usuario.getRol().name()) // ADMIN se convierte en ROLE_ADMIN
                .build();
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    // El correo procede del Principal autenticado que recibe el controlador.
    public Usuario obtenerAdministrador(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Administrador no encontrado."));
        if (usuario.getRol() != RolUsuario.ADMIN) {
            throw new IllegalStateException("Se requiere un administrador.");
        }
        return usuario;
    }
}
