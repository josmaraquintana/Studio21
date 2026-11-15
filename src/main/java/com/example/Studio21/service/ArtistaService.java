package com.example.Studio21.service;

import com.example.Studio21.model.Artista;
import com.example.Studio21.repository.ArtistaRepository;
import com.example.Studio21.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;

@Service
public class ArtistaService {

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Artista> listar() {
        return artistaRepository.findAllByOrderByNombreAsc();
    }

    public Artista buscarPorId(Long id) {
        return artistaRepository.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void guardar(Artista artista) {
        validar(artista);
        artista.setNombre(artista.getNombre().trim());
        artista.setGeneroMusical(artista.getGeneroMusical().trim());
        artistaRepository.save(artista);
    }

    // Se copian los datos del formulario sobre el registro guardado: UPDATE, no INSERT.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void actualizar(Long id, Artista datos) {
        validar(datos);
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artista no encontrado."));
        artista.setNombre(datos.getNombre().trim());
        artista.setGeneroMusical(datos.getGeneroMusical().trim());
        artistaRepository.save(artista);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Long id) {
        if (productoRepository.existsByArtistaId(id)) {
            throw new IllegalStateException("No se puede eliminar: hay piezas registradas de este artista.");
        }
        artistaRepository.deleteById(id);
    }

    // Reglas de negocio del artista (mismos límites que las columnas de la entidad).
    private void validar(Artista artista) {
        if (artista.getNombre() == null || artista.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (artista.getNombre().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre admite máximo 100 caracteres.");
        }
        if (artista.getGeneroMusical() == null || artista.getGeneroMusical().isBlank()) {
            throw new IllegalArgumentException("El género musical es obligatorio.");
        }
        if (artista.getGeneroMusical().trim().length() > 100) {
            throw new IllegalArgumentException("El género musical admite máximo 100 caracteres.");
        }
    }
}
