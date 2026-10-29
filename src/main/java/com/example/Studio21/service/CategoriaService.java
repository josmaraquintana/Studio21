package com.example.Studio21.service;

import com.example.Studio21.model.Categoria;
import com.example.Studio21.repository.CategoriaRepository;
import com.example.Studio21.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    public void guardar(Categoria categoria) {
        validar(categoria);
        categoria.setNombre(categoria.getNombre().trim());
        categoriaRepository.save(categoria);
    }

    // Se copian los datos del formulario sobre el registro guardado: UPDATE, no INSERT.
    public void actualizar(Long id, Categoria datos) {
        validar(datos);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada."));
        categoria.setNombre(datos.getNombre().trim());
        categoria.setDescripcion(datos.getDescripcion());
        categoria.setRopa(datos.isRopa());
        categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        if (productoRepository.existsByCategoriaId(id)) {
            throw new IllegalStateException("No se puede eliminar: hay piezas registradas en esta categoría.");
        }
        categoriaRepository.deleteById(id);
    }

    // Reglas de negocio de la categoría (mismos límites que las columnas de la entidad).
    private void validar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (categoria.getNombre().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre admite máximo 100 caracteres.");
        }
        if (categoria.getDescripcion() != null && categoria.getDescripcion().length() > 300) {
            throw new IllegalArgumentException("La descripción admite máximo 300 caracteres.");
        }
    }
}
