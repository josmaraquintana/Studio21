package com.example.Studio21.controller.admin;

import com.example.Studio21.model.Donacion;
import com.example.Studio21.service.DonacionService;
import com.example.Studio21.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Donaciones recibidas y sus donantes.
@Controller
public class DonacionController {

    @Autowired
    private DonacionService donacionService;

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("admin", true);
        model.addAttribute("seccion", "donaciones");
    }

    @GetMapping("/admin/donaciones")
    public String listar(Model model) {
        return mostrarListado(model);
    }

    @PostMapping("/admin/donaciones/nueva")
    public String registrar(@RequestParam(name = "donanteId", required = false) Long donanteId,
                            @RequestParam(name = "donanteNombre", required = false) String donanteNombre,
                            @RequestParam(name = "donanteTelefono", required = false) String donanteTelefono,
                            @RequestParam(name = "donanteCorreo", required = false) String donanteCorreo,
                            @RequestParam(name = "fechaRecepcion", required = false) String fechaRecepcion,
                            @RequestParam(name = "observaciones", required = false) String observaciones,
                            Model model, Principal principal) {
        Donacion donacion;
        try {
            // Quien recibe la donación es el administrador en sesión.
            donacion = donacionService.registrar(donanteId, donanteNombre, donanteTelefono, donanteCorreo,
                    fechaRecepcion, observaciones, usuarioService.obtenerAdministrador(principal.getName()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("formularioAbierto", true);
            return mostrarListado(model);
        }
        // Se abre el detalle para registrar ahí las piezas recibidas.
        return "redirect:/admin/donaciones/" + donacion.getId() + "?registrada";
    }

    @GetMapping("/admin/donaciones/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Donacion donacion = donacionService.buscarPorId(id);
        if (donacion == null) {
            return "redirect:/admin/donaciones";
        }
        model.addAttribute("donacion", donacion);
        return "admin/donacion";
    }

    @PostMapping("/admin/donaciones/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Model model) {
        try {
            donacionService.eliminar(id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarListado(model);
        }
        return "redirect:/admin/donaciones?eliminada";
    }

    private String mostrarListado(Model model) {
        model.addAttribute("donaciones", donacionService.listar());
        model.addAttribute("donantes", donacionService.listarDonantes());
        return "admin/donaciones";
    }
}
