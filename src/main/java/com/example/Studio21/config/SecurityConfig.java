package com.example.Studio21.config;

import com.example.Studio21.model.Usuario;
import com.example.Studio21.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Spring Security protege todo el panel: /admin/** solo para el rol ADMIN y /admin/login como ruta pública.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UsuarioService usuarioService) throws Exception {
        http
            // CSRF queda habilitado por defecto: todo formulario POST con th:action lleva su token.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                // Sin sesión de administrador, cualquier ruta del panel redirige al formulario de login.
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // La tienda pública y los recursos estáticos (css, img) quedan libres.
                .anyRequest().permitAll()
            )
            // Login con formulario propio; las credenciales se validan contra la BD con UsuarioService y BCrypt.
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .usernameParameter("correo")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    // Se guarda en sesión el administrador para mostrar su nombre y registrar quién hace cada acción.
                    Usuario admin = usuarioService.buscarPorCorreo(authentication.getName());
                    HttpSession session = request.getSession();
                    session.setAttribute("admin_id", admin.getId());
                    session.setAttribute("admin_nombre", admin.getNombre());
                    response.sendRedirect(request.getContextPath() + "/admin");
                })
                .failureUrl("/admin/login?error")
                .permitAll()
            )
            // Logout (POST con token CSRF): invalida la sesión HTTP y regresa al login.
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/admin/login?logout")
            );

        return http.build();
    }

    // BCrypt: Spring Security lo usa para comparar la contraseña escrita con el hash guardado. Nunca texto plano.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
