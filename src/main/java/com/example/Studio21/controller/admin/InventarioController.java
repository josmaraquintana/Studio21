package com.example.Studio21.controller.admin;

import com.example.Studio21.service.InventarioService;
import com.example.Studio21.service.ProductoService;
import com.example.Studio21.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Movimientos físicos de inventario y ajustes manuales.
@Controller
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("admin", true);
        model.addAttribute("seccion", "inventario");
    }

    @GetMapping("/admin/inventario")
    public String listar(Model model) {
        return mostrarVista(model);
    }

    @PostMapping("/admin/inventario/ajuste")
    public String registrarAjuste(@RequestParam(name = "productoId", required = false) Long productoId,
                                  @RequestParam(name = "cambio", required = false) Integer cambio,
                                  @RequestParam(name = "motivo", required = false) String motivo,
                                  Model model, Principal principal) {
        try {
            inventarioService.registrarAjuste(productoId, cambio, motivo, usuarioService.obtenerAdministrador(principal.getName()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("formularioAbierto", true);
            return mostrarVista(model);
        }
        return "redirect:/admin/inventario?registrado";
    }

    private String mostrarVista(Model model) {
        model.addAttribute("movimientos", inventarioService.listar());
        model.addAttribute("productos", productoService.listarTodos());
        return "admin/inventario";
    }
}
