package com.example.Studio21.controller.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Muestra el formulario de login del panel. El POST /admin/login y el logout los procesa Spring Security (SecurityConfig).
@Controller
public class AdminAuthController {

    @GetMapping("/admin/login")
    public String mostrarLogin(HttpSession session) {
        // Si ya hay sesión de administrador, no tiene caso volver a pedir credenciales.
        if (session.getAttribute("admin_id") != null) {
            return "redirect:/admin";
        }
        return "admin/login";
    }
}
