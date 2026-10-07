package com.example.Studio21.controller;

import com.example.Studio21.vista.DatosMaquetado;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/** Rutas del maquetado: seleccionan vistas y datos de ejemplo, sin operaciones de persistencia. */
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

    @GetMapping({"/pedido", "/admin/pedido"})
    public String pedido(@RequestParam(defaultValue = "8492") String id,
            @RequestParam(defaultValue = "") String estado,
            @RequestParam(defaultValue = "false") boolean confirmado,
            @RequestParam(defaultValue = "") String paqueteria,
            @RequestParam(defaultValue = "") String guia,
            @RequestParam(defaultValue = "") String fechaEnvio,
            @RequestParam(defaultValue = "") String fechaEntrega,
            jakarta.servlet.http.HttpServletRequest request, Model model) {
        Map<String, Object> p = datos.pedido(id, estado);
        boolean admin = request.getServletPath().startsWith("/admin/");
        if (admin && !paqueteria.isBlank() && !guia.isBlank()) p.put("envio", paqueteria + " · Guía " + guia);
        if (admin && !fechaEnvio.isBlank()) p.put("fechaEnvio", fechaEnvio);
        if (admin && !fechaEntrega.isBlank()) p.put("fechaEntrega", fechaEntrega);
        model.addAttribute("pedido", p); model.addAttribute("admin", admin);
        model.addAttribute("seccion", "pedidos"); model.addAttribute("confirmado", confirmado);
        return "pedido";
    }

    private String vistaAdmin(String vista, String seccion, Model model) {
        model.addAttribute("admin", true); model.addAttribute("seccion", seccion); return "admin/" + vista;
    }

    @GetMapping("/admin")
    public String admin(Model model) { return vistaAdmin("inicio", "resumen", model); }

    @GetMapping("/admin/productos")
    public String productos(@RequestParam(defaultValue = "") String id,
            @RequestParam(defaultValue = "false") boolean crear, Model model) {
        model.addAttribute("producto", datos.producto(id.isBlank() ? "eras" : id));
        model.addAttribute("crear", crear); model.addAttribute("editar", !id.isBlank());
        return vistaAdmin("productos", "productos", model);
    }

    @GetMapping("/admin/autenticidad")
    public String autenticidad(@RequestParam(defaultValue = "Pendiente") String estado, Model model) {
        model.addAttribute("verificacion", estado);
        return vistaAdmin("autenticidad", "autenticidad", model);
    }

    @GetMapping("/admin/donaciones")
    public String donaciones(@RequestParam(defaultValue = "false") boolean detalle, Model model) {
        model.addAttribute("detalle", detalle); return vistaAdmin("donaciones", "donaciones", model);
    }

    @GetMapping("/admin/inventario")
    public String inventario(Model model) { return vistaAdmin("inventario", "inventario", model); }

    @GetMapping("/admin/pedidos")
    public String pedidosAdmin(Model model) { return vistaAdmin("pedidos", "pedidos", model); }

    @GetMapping("/admin/categorias")
    public String categorias(Model model) { return vistaAdmin("categorias", "categorias", model); }

    @GetMapping("/admin/artistas")
    public String artistas(Model model) { return vistaAdmin("artistas", "artistas", model); }
}
