package com.example.Studio21.controller.admin;

import com.example.Studio21.enums.EstadoPedido;
import com.example.Studio21.model.Pedido;
import com.example.Studio21.service.PedidoService;
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

import java.util.List;

// Gestión de pedidos: cada botón del detalle es un POST que cambia el estado y regresa al detalle (PRG).
@Controller
public class PedidoAdminController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("admin", true);
        model.addAttribute("seccion", "pedidos");
    }

    @GetMapping("/admin/pedidos")
    public String listar(@RequestParam(name = "buscar", required = false) String buscar,
                         @RequestParam(name = "estado", required = false) EstadoPedido estado,
                         Model model) {
        model.addAttribute("pedidos", pedidoService.buscar(buscar, estado));
        model.addAttribute("buscar", buscar);
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoPedido.values());
        return "admin/pedidos";
    }

    @GetMapping("/admin/pedidos/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        return mostrarDetalle(id, model);
    }

    @PostMapping("/admin/pedidos/{id}/pagar")
    public String pagar(@PathVariable Long id, Model model) {
        try {
            pedidoService.registrarPago(id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarDetalle(id, model);
        }
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/admin/pedidos/{id}/procesar")
    public String procesar(@PathVariable Long id, Model model) {
        try {
            pedidoService.comenzarPreparacion(id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarDetalle(id, model);
        }
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/admin/pedidos/{id}/enviar")
    public String enviar(@PathVariable Long id,
                         @RequestParam(name = "paqueteria", required = false) String paqueteria,
                         @RequestParam(name = "numeroGuia", required = false) String numeroGuia,
                         @RequestParam(name = "fechaEnvio", required = false) String fechaEnvio,
                         Model model, Principal principal) {
        try {
            pedidoService.registrarEnvio(id, paqueteria, numeroGuia, fechaEnvio, usuarioService.obtenerAdministrador(principal.getName()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarDetalle(id, model);
        }
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/admin/pedidos/{id}/entregar")
    public String entregar(@PathVariable Long id,
                           @RequestParam(name = "fechaEntrega", required = false) String fechaEntrega,
                           Model model) {
        try {
            pedidoService.registrarEntrega(id, fechaEntrega);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarDetalle(id, model);
        }
        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/admin/pedidos/{id}/cancelar")
    public String cancelar(@PathVariable Long id, Model model) {
        try {
            pedidoService.cancelar(id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarDetalle(id, model);
        }
        return "redirect:/admin/pedidos/" + id;
    }

    private String mostrarDetalle(Long id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        if (pedido == null) {
            return "redirect:/admin/pedidos";
        }
        model.addAttribute("pedido", pedido);
        // Pasos de la línea de tiempo (Cancelado no es un paso).
        model.addAttribute("pasos", List.of(EstadoPedido.PENDIENTE, EstadoPedido.PAGADO,
                EstadoPedido.PROCESANDO, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO));
        return "admin/pedido";
    }
}
