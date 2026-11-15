package com.example.Studio21.service;

import com.example.Studio21.dto.CuentaRequest;
import com.example.Studio21.dto.CuentaResponse;
import com.example.Studio21.exception.ConflictException;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.UsuarioRepository;
import com.example.Studio21.security.UsuarioActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaService {
    private final UsuarioActual usuarioActual;
    private final UsuarioRepository usuarios;

    public CuentaService(UsuarioActual usuarioActual, UsuarioRepository usuarios) {
        this.usuarioActual = usuarioActual;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public CuentaResponse obtener() {
        return CuentaResponse.de(usuarioActual.obtener());
    }

    @Transactional
    public CuentaResponse actualizar(CuentaRequest datos) {
        Usuario usuario = usuarioActual.obtener();
        String correo = AuthService.normalizarCorreo(datos.correo());
        if (usuarios.existsByCorreoAndIdNot(correo, usuario.getId())) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        usuario.setNombre(datos.nombre().trim());
        usuario.setTelefono(datos.telefono().trim());
        usuario.setCorreo(correo);
        return CuentaResponse.de(usuarios.save(usuario));
    }
}
