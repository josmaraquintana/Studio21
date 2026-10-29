package com.example.Studio21.controller;

import com.example.Studio21.vista.DatosMaquetado;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/** Rutas de la tienda (maquetado con datos de ejemplo). El panel /admin/** vive en los controladores de controller.admin. */
@Controller
public class HomeController {
    private final DatosMaquetado datos;

    public HomeController(DatosMaquetado datos) { this.datos = datos; }

    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("admin", false);
        model.addAttribute("seccion", "catalogo");
        model.addAttribute("productos", datos.catalogo());
        model.addAttribute("todosProductos", datos.todos());
        model.addAttribute("pedidos", datos.pedidos());
        model.addAttribute("pasos", datos.pasos());
    }

    @GetMapping({"/", "/index", "/catalogo"})
    public String catalogo() { return "catalogo"; }

    @GetMapping("/producto")
    public String producto(@RequestParam(defaultValue = "eras") String id, Model model) {
        model.addAttribute("producto", datos.producto(id)); return "producto";
    }

    @GetMapping("/carrito")
    public String carrito(@RequestParam(defaultValue = "") String id, Model model) {
        model.addAttribute("seccion", "carrito");
        model.addAttribute("producto", id.isBlank() ? null : datos.producto(id)); return "carrito";
    }

    @GetMapping("/checkout")
    public String checkout(@RequestParam(defaultValue = "eras") String id, Model model) {
        Map<String, Object> p = datos.producto(id);
        if (!p.get("status").equals("Disponible")) return "redirect:/carrito?id=" + id;
        model.addAttribute("producto", p); model.addAttribute("seccion", "carrito");
        model.addAttribute("nuevoPedido", id.equals("motomami") ? "8493" : id.equals("cardigan") ? "8494" : "8492");
        return "checkout";
    }

    @GetMapping("/login")
    public String login(Model model) { model.addAttribute("seccion", "cuenta"); return "login"; }

    @GetMapping("/registro")
    public String registro(Model model) { model.addAttribute("seccion", "cuenta"); return "registro"; }

    @GetMapping("/cuenta")
    public String cuenta(Model model) { model.addAttribute("seccion", "cuenta"); return "cuenta"; }

    @GetMapping("/direcciones")
    public String direcciones(Model model) { model.addAttribute("seccion", "cuenta"); return "direcciones"; }

    @GetMapping("/pedidos")
    public String pedidos(Model model) { model.addAttribute("seccion", "pedidos"); return "pedidos"; }

    @GetMapping("/pedido")
    public String pedido(@RequestParam(defaultValue = "8492") String id,
            @RequestParam(defaultValue = "") String estado,
            @RequestParam(defaultValue = "false") boolean confirmado, Model model) {
        model.addAttribute("pedido", datos.pedido(id, estado));
        model.addAttribute("seccion", "pedidos"); model.addAttribute("confirmado", confirmado);
        return "pedido";
    }
}
