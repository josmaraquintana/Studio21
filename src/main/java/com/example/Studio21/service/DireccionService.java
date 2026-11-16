package com.example.Studio21.service;

import com.example.Studio21.dto.DireccionDTO;
import com.example.Studio21.dto.DireccionRequestDTO;
import com.example.Studio21.exception.NotFoundException;
import com.example.Studio21.model.Direccion;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.DireccionRepository;
import com.example.Studio21.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DireccionService {
    private final DireccionRepository direcciones;
    private final UsuarioRepository usuarios;

    public DireccionService(DireccionRepository direcciones, UsuarioRepository usuarios) {
        this.direcciones = direcciones;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<DireccionDTO> listar(Long usuarioId) {
        return direcciones.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
                .map(this::aDTO)
                .toList();
    }

    @Transactional
    public DireccionDTO crear(Long usuarioId, DireccionRequestDTO datos) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Direccion direccion = new Direccion(usuario, datos.destinatario().trim(), datos.telefono().trim(),
                datos.calle().trim(), datos.numeroExterior().trim(), datos.colonia().trim(),
                datos.ciudad().trim(), datos.estado().trim(), datos.codigoPostal().trim());
        return aDTO(direcciones.save(direccion));
    }

    @Transactional
    public DireccionDTO actualizar(Long usuarioId, Long id, DireccionRequestDTO datos) {
        Direccion direccion = buscarPropia(usuarioId, id);
        direccion.setDestinatario(datos.destinatario().trim());
        direccion.setTelefono(datos.telefono().trim());
        direccion.setCalle(datos.calle().trim());
        direccion.setNumeroExterior(datos.numeroExterior().trim());
        direccion.setColonia(datos.colonia().trim());
        direccion.setCiudad(datos.ciudad().trim());
        direccion.setEstado(datos.estado().trim());
        direccion.setCodigoPostal(datos.codigoPostal().trim());
        return aDTO(direccion);
    }

    @Transactional
    public void eliminar(Long usuarioId, Long id) {
        Direccion direccion = buscarPropia(usuarioId, id);
        direccion.getUsuario().eliminarDireccion(direccion);
        direcciones.delete(direccion);
    }

    private Direccion buscarPropia(Long usuarioId, Long id) {
        return direcciones.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada"));
    }

    private DireccionDTO aDTO(Direccion d) {
        return new DireccionDTO(d.getId(), d.getDestinatario(), d.getTelefono(), d.getCalle(),
                d.getNumeroExterior(), d.getColonia(), d.getCiudad(), d.getEstado(), d.getCodigoPostal());
    }
}
