package com.example.Studio21.controller.admin;

import com.example.Studio21.enums.EstadoVerificacion;
import com.example.Studio21.model.Producto;
import com.example.Studio21.service.ProductoService;
import com.example.Studio21.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// Revisión de autenticidad: verificar habilita la publicación; rechazar la impide.
@Controller
public class AutenticidadController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("admin", true);
        model.addAttribute("seccion", "autenticidad");
    }

    // ?id= permite revisar una pieza concreta (por ejemplo, volver a revisar una rechazada).
    @GetMapping("/admin/autenticidad")
    public String revisar(@RequestParam(name = "id", required = false) Long id, Model model) {
        return mostrarVista(model, id);
    }

    @PostMapping("/admin/autenticidad/verificar/{id}")
    public String verificar(@PathVariable Long id, @RequestParam("evidencia") String evidencia, Model model) {
        try {
            productoService.verificarAutenticidad(id, usuarioService.obtenerAdminLogueado(), evidencia);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarVista(model, id);
        }
        return "redirect:/admin/autenticidad?verificada";
    }

    @PostMapping("/admin/autenticidad/rechazar/{id}")
    public String rechazar(@PathVariable Long id, @RequestParam("evidencia") String evidencia, Model model) {
        try {
            productoService.rechazarAutenticidad(id, usuarioService.obtenerAdminLogueado(), evidencia);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarVista(model, id);
        }
        return "redirect:/admin/autenticidad?rechazada";
    }

    private String mostrarVista(Model model, Long id) {
        List<Producto> pendientes = productoService.listarPorVerificacion(EstadoVerificacion.PENDIENTE);
        Producto seleccionada = null;
        if (id != null) {
            seleccionada = productoService.buscarPorId(id);
        } else if (!pendientes.isEmpty()) {
            seleccionada = pendientes.get(0); // por defecto, la pendiente más antigua
        }
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("pieza", seleccionada);
        return "admin/autenticidad";
    }
}
