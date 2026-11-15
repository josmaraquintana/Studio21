package com.example.Studio21.controller.api;

import com.example.Studio21.dto.ArtistaDTO;
import com.example.Studio21.dto.CategoriaDTO;
import com.example.Studio21.service.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Listas que llenan los selects "Categoría" y "Artista" del filtro del catálogo (públicas). */
@RestController
@RequestMapping("/api")
public class CatalogoApiController {

    private final CatalogoService catalogo;

    public CatalogoApiController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/categorias")
    public List<CategoriaDTO> categorias() {
        return catalogo.listarCategorias();
    }

    @GetMapping("/artistas")
    public List<ArtistaDTO> artistas() {
        return catalogo.listarArtistas();
    }
}
