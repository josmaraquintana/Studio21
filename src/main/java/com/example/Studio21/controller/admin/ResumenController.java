package com.example.Studio21.controller.admin;

import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.service.PedidoService;
import com.example.Studio21.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Página de inicio del panel con los contadores de la tienda.
@Controller
public class ResumenController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/admin")
    public String resumen(Model model) {
        model.addAttribute("admin", true);
        model.addAttribute("seccion", "resumen");
        model.addAttribute("pendientes", productoService.contarPendientesDeRevision());
        model.addAttribute("disponibles", productoService.contarPublicadasDisponibles());
        model.addAttribute("apartadas", productoService.contarPorDisponibilidad(EstadoDisponibilidad.APARTADA));
        model.addAttribute("vendidas", productoService.contarPorDisponibilidad(EstadoDisponibilidad.VENDIDA));
        model.addAttribute("pedidos", pedidoService.recientes());
        return "admin/inicio";
    }
}
