package com.example.Studio21.service;

import com.example.Studio21.dto.AuthResponse;
import com.example.Studio21.dto.CuentaResponse;
import com.example.Studio21.dto.LoginRequest;
import com.example.Studio21.dto.RegistroRequest;
import com.example.Studio21.enums.RolUsuario;
import com.example.Studio21.exception.ConflictException;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.UsuarioRepository;
import com.example.Studio21.security.JwtService;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest datos) {
        String correo = normalizarCorreo(datos.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        Usuario usuario = new Usuario(
                datos.nombre().trim(),
                correo,
                passwordEncoder.encode(datos.contrasenia()), // se guarda el hash, nunca la contraseña
                datos.telefono().trim(),
                RolUsuario.CLIENTE);                          // el rol lo decide el servidor
        return respuestaCon(usuarios.save(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest datos) {
        // Mismo mensaje si el correo no existe o si la contraseña está mal:
        // así no se revela qué correos están registrados.
        Usuario usuario = usuarios.findByCorreo(normalizarCorreo(datos.correo()))
                .filter(u -> passwordEncoder.matches(datos.contrasenia(), u.getContraseniaHash()))
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));
        return respuestaCon(usuario);
    }

    private AuthResponse respuestaCon(Usuario usuario) {
        return new AuthResponse(jwtService.generarToken(usuario), "Bearer",
                jwtService.getExpiracionMs(), CuentaResponse.de(usuario));
    }

    // "Jos@gmail.com " y "jos@gmail.com" son la misma cuenta
    static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
