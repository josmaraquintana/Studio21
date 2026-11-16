package com.example.Studio21.config;

import com.example.Studio21.enums.RolUsuario;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.UsuarioRepository;
import com.example.Studio21.security.JwtService;
import com.example.Studio21.service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Contexto web aislado: no arranca JPA, no conecta a MySQL ni carga datos-mock.sql.
class SecurityMergeTests {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(WebTestConfig.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @AfterEach
    void cerrar() {
        context.close();
    }

    @Test
    void catalogoYAutenticacionSonPublicosSinCsrf() throws Exception {
        for (String ruta : new String[]{"/api/productos", "/api/productos/1", "/api/categorias", "/api/artistas"}) {
            mvc.perform(get(ruta)).andExpect(status().isOk());
        }
        for (String ruta : new String[]{"/api/auth/registro", "/api/auth/login"}) {
            mvc.perform(post(ruta)).andExpect(status().isOk());
        }
        mvc.perform(post("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void rutasProtegidasDevuelven401JsonSinCrearSesion() throws Exception {
        var resultado = mvc.perform(get("/api/cuenta"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/cuenta"))
                .andExpect(jsonPath("$.message").isNotEmpty()).andReturn();
        assertNull(resultado.getRequest().getSession(false));
        mvc.perform(get("/api/futura-protegida")).andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaTokensInvalidosYVencidos() throws Exception {
        mvc.perform(get("/api/cuenta").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("El token no es válido o ha expirado"));
        var vencido = new JwtService(context.getBean(WebTestConfig.class).secreto, -60000);
        mvc.perform(get("/api/cuenta").header("Authorization", "Bearer " + vencido.generarToken(cliente())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtValidoPermiteLecturaYEscrituraSinCsrfNiSesion() throws Exception {
        String token = context.getBean(JwtService.class).generarToken(cliente());
        var resultado = mvc.perform(put("/api/cuenta").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        assertNull(resultado.getRequest().getSession(false));
        mvc.perform(get("/api/cuenta").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/cuenta")).andExpect(status().isUnauthorized());
    }

    @Test
    void permisoDenegadoDevuelve403Json() throws Exception {
        String token = context.getBean(JwtService.class).generarToken(cliente());
        mvc.perform(get("/api/restringida").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/restringida"));
    }

    @Test
    void panelConservaFormularioSesionCsrfYLogout() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
        mvc.perform(post("/admin/login").param("correo", "admin-prueba").param("password", "solo-test"))
                .andExpect(status().isForbidden());
        var login = mvc.perform(formLogin("/admin/login").user("correo", "admin-prueba").password("solo-test"))
                .andExpect(redirectedUrl("/admin")).andReturn();
        var sesion = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(sesion);
        assertEquals("Admin de prueba", sesion.getAttribute("admin_nombre"));
        mvc.perform(get("/admin").session(sesion)).andExpect(status().isOk());
        mvc.perform(post("/admin/guardar").session(sesion)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/guardar").session(sesion).with(csrf())).andExpect(status().isOk());
        mvc.perform(get("/api/cuenta").session(sesion)).andExpect(status().isUnauthorized());
        mvc.perform(post("/admin/logout").session(sesion)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/logout").session(sesion).with(csrf()))
                .andExpect(redirectedUrl("/admin/login?logout"));
        assertTrue(sesion.isInvalid());
    }

    @Test
    void jwtNoAutenticaElPanel() throws Exception {
        Usuario admin = cliente();
        admin.setRol(RolUsuario.ADMIN);
        mvc.perform(get("/admin").header("Authorization", "Bearer " + context.getBean(JwtService.class).generarToken(admin)))
                .andExpect(status().is3xxRedirection());
    }

    private static Usuario cliente() {
        var usuario = new Usuario() {
            @Override
            public Long getId() { return 1L; }
        };
        usuario.setRol(RolUsuario.CLIENTE);
        return usuario;
    }

    @Configuration
    @EnableWebMvc
    @Import(SecurityConfig.class)
    static class WebTestConfig {
        private final String secreto;

        WebTestConfig() {
            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            secreto = Base64.getEncoder().encodeToString(bytes);
        }

        @Bean
        ObjectMapper objectMapper() { return JsonMapper.builder().build(); }

        @Bean
        UsuarioRepository usuarioRepository() {
            return org.mockito.Mockito.mock(UsuarioRepository.class,
                    org.mockito.Mockito.withSettings().mockMaker(org.mockito.MockMakers.PROXY));
        }

        @Bean
        JwtService jwtService() {
            return new JwtService(secreto, 60000);
        }

        @Bean
        UsuarioService usuarioService(PasswordEncoder encoder) {
            return new UsuarioService() {
                @Override
                public UserDetails loadUserByUsername(String correo) {
                    return User.withUsername(correo).password(encoder.encode("solo-test")).roles("ADMIN").build();
                }

                @Override
                public Usuario buscarPorCorreo(String correo) {
                    var admin = new Usuario();
                    admin.setNombre("Admin de prueba");
                    return admin;
                }
            };
        }

        @Bean
        Rutas rutas() { return new Rutas(); }
    }

    @RestController
    static class Rutas {
        @GetMapping({"/api/productos", "/api/productos/{id}", "/api/categorias", "/api/artistas", "/api/cuenta", "/admin"})
        String consultar() { return "ok"; }

        @PostMapping({"/api/auth/registro", "/api/auth/login", "/admin/guardar"})
        String guardar() { return "ok"; }

        @PutMapping("/api/cuenta")
        String actualizar() { return "ok"; }

        @GetMapping("/api/restringida")
        String restringida() { throw new AccessDeniedException("Prueba de permisos"); }
    }
}
