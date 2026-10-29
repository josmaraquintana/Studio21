package com.example.Studio21.controller.admin;

import com.example.Studio21.model.Artista;
import com.example.Studio21.service.ArtistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

// CRUD de artistas. Todo POST exitoso termina en redirect (patrón PRG).
@Controller
public class ArtistaController {

    @Autowired
    private ArtistaService artistaService;

    // Datos que necesitan todas las vistas de este controlador (cabecera y menú lateral).
    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("admin", true);
        model.addAttribute("seccion", "artistas");
    }

    @GetMapping("/admin/artistas")
    public String listar(Model model) {
        model.addAttribute("artista", new Artista()); // objeto vacío para el formulario
        return mostrarVista(model, null);
    }

    // th:object enlaza el formulario con el artista; el servicio valida los datos.
    @PostMapping("/admin/artistas/nuevo")
    public String guardar(@ModelAttribute("artista") Artista artista, Model model) {
        try {
            artistaService.guardar(artista);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("formularioAbierto", true); // deja el formulario abierto con lo capturado
            return mostrarVista(model, null);
        }
        return "redirect:/admin/artistas?guardado";
    }

    @GetMapping("/admin/artistas/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Artista artista = artistaService.buscarPorId(id);
        if (artista == null) {
            return "redirect:/admin/artistas";
        }
        model.addAttribute("artista", artista); // th:field pone los valores actuales
        return mostrarVista(model, id);
    }

    @PostMapping("/admin/artistas/editar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute("artista") Artista artista, Model model) {
        try {
            artistaService.actualizar(id, artista);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarVista(model, id);
        }
        return "redirect:/admin/artistas?guardado";
    }

    @PostMapping("/admin/artistas/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Model model) {
        try {
            artistaService.eliminar(id);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("artista", new Artista());
            return mostrarVista(model, null);
        }
        return "redirect:/admin/artistas?eliminado";
    }

    // idEdicion = null significa alta; con valor, el formulario envía a /editar/{id}.
    private String mostrarVista(Model model, Long idEdicion) {
        model.addAttribute("artistas", artistaService.listar());
        model.addAttribute("idEdicion", idEdicion);
        return "admin/artistas";
    }
}
