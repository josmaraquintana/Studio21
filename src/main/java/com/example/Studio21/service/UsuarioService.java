package com.example.Studio21.service;

import com.example.Studio21.enums.RolUsuario;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Spring Security llama a loadUserByUsername() en el login para validar las credenciales contra la base de datos.
@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Spring inyecta la sesión de la petición actual.
    @Autowired
    private HttpSession session;

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

    // Administrador que hace la acción (recibe donaciones, verifica piezas, registra movimientos).
    public Usuario obtenerAdminLogueado() {
        Long adminId = (Long) session.getAttribute("admin_id");

        if (adminId != null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(adminId);
            if (usuarioOpt.isPresent()) {
                return usuarioOpt.get();
            }
        }
        return null;
    }
}
