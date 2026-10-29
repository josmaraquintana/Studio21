package com.example.Studio21.controller.admin;

import com.example.Studio21.model.Categoria;
import com.example.Studio21.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

// CRUD de categorías. Todo POST exitoso termina en redirect (patrón PRG).
@Controller
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // Datos que necesitan todas las vistas de este controlador (cabecera y menú lateral).
    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("admin", true);
        model.addAttribute("seccion", "categorias");
    }

    @GetMapping("/admin/categorias")
    public String listar(Model model) {
        model.addAttribute("categoria", new Categoria()); // objeto vacío para el formulario
        return mostrarVista(model, null);
    }

    // th:object enlaza el formulario con la categoría; el servicio valida los datos.
    @PostMapping("/admin/categorias/nueva")
    public String guardar(@ModelAttribute("categoria") Categoria categoria, Model model) {
        try {
            categoriaService.guardar(categoria);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("formularioAbierto", true); // deja el formulario abierto con lo capturado
            return mostrarVista(model, null);
        }
        return "redirect:/admin/categorias?guardada";
    }

    @GetMapping("/admin/categorias/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.buscarPorId(id);
        if (categoria == null) {
            return "redirect:/admin/categorias";
        }
        model.addAttribute("categoria", categoria); // th:field pone los valores actuales
        return mostrarVista(model, id);
    }

    @PostMapping("/admin/categorias/editar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute("categoria") Categoria categoria, Model model) {
        try {
            categoriaService.actualizar(id, categoria);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarVista(model, id);
        }
        return "redirect:/admin/categorias?guardada";
    }

    @PostMapping("/admin/categorias/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Model model) {
        try {
            categoriaService.eliminar(id);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoria", new Categoria());
            return mostrarVista(model, null);
        }
        return "redirect:/admin/categorias?eliminada";
    }

    // idEdicion = null significa alta; con valor, el formulario envía a /editar/{id}.
    private String mostrarVista(Model model, Long idEdicion) {
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("idEdicion", idEdicion);
        return "admin/categorias";
    }
}
