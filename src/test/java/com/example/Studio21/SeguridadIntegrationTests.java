package com.example.Studio21;

import com.example.Studio21.enums.RolUsuario;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired com.example.Studio21.repository.DonacionRepository donaciones;
    @Autowired PasswordEncoder encoder;
    Usuario admin;

    @BeforeEach void preparar() {
        admin = usuarios.save(new Usuario("Admin prueba", UUID.randomUUID() + "@admin.test",
                encoder.encode("clave-test"), "123", RolUsuario.ADMIN));
    }

    @Test void loginYRecursosSonPublicos() throws Exception {
        mvc.perform(get("/admin/login")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
        mvc.perform(get("/css/styles.css")).andExpect(result -> assertThat(result.getResponse().getStatus()).isNotIn(302, 401, 403));
    }

    @Test void todasLasRutasAdministrativasRequierenAdmin() throws Exception {
        for (String ruta : new String[]{"/admin", "/admin/productos", "/admin/autenticidad", "/admin/donaciones", "/admin/inventario", "/admin/pedidos", "/admin/categorias", "/admin/artistas"}) {
            mvc.perform(get(ruta)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/login"));
            mvc.perform(get(ruta).with(user("cliente").roles("CLIENTE"))).andExpect(status().isForbidden());
            mvc.perform(get(ruta).with(user(admin.getCorreo()).roles("ADMIN"))).andExpect(status().isOk());
        }
    }

    @Test void loginRealUsaBaseDeDatosYBCrypt() throws Exception {
        assertThat(admin.getContraseniaHash()).startsWith("$2");
        assertThat(encoder.matches("clave-test", admin.getContraseniaHash())).isTrue();
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/admin/login").with(csrf())
                .param("correo", admin.getCorreo()).param("password", "clave-test"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin"))
                .andReturn().getRequest().getSession(false);
        mvc.perform(get("/admin/inventario").session(session)).andExpect(status().isOk());
        mvc.perform(post("/admin/login").with(csrf()).param("correo", admin.getCorreo()).param("password", "incorrecta"))
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test void clienteNoPuedeAutenticarseComoAdministrador() throws Exception {
        Usuario cliente = usuarios.save(new Usuario("Cliente", UUID.randomUUID() + "@cliente.test", encoder.encode("clave-test"), "123", RolUsuario.CLIENTE));
        mvc.perform(post("/admin/login").with(csrf()).param("correo", cliente.getCorreo()).param("password", "clave-test"))
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test void csrfBloqueaPostInclusoConAdmin() throws Exception {
        mvc.perform(post("/admin/login").param("correo", admin.getCorreo()).param("password", "clave-test")).andExpect(status().isForbidden());
        mvc.perform(post("/admin/categorias/nueva").with(user(admin.getCorreo()).roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/categorias/nueva").with(user(admin.getCorreo()).roles("ADMIN")).with(csrf().useInvalidToken())).andExpect(status().isForbidden());
        mvc.perform(post("/admin/categorias/nueva").with(user(admin.getCorreo()).roles("ADMIN")).with(csrf())
                .param("nombre", "Categoría prueba").param("descripcion", "Prueba").param("ropa", "false"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/categorias?guardada"));
    }

    @Test void logoutExigePostYCsrfEInvalidaSesion() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/admin/login").with(csrf())
                .param("correo", admin.getCorreo()).param("password", "clave-test")).andReturn().getRequest().getSession(false);
        mvc.perform(post("/admin/logout").session(session)).andExpect(status().isForbidden());
        assertThat(session.isInvalid()).isFalse();
        mvc.perform(get("/admin/logout").session(session)).andExpect(result -> assertThat(session.isInvalid()).isFalse());
        mvc.perform(post("/admin/logout").session(session).with(csrf())).andExpect(redirectedUrl("/admin/login?logout"));
        assertThat(session.isInvalid()).isTrue();
    }

    @Test void identidadResponsableProvieneDelPrincipalEIgnoraAdminDelFormulario() throws Exception {
        var resultado = mvc.perform(post("/admin/donaciones/nueva").with(user(admin.getCorreo()).roles("ADMIN")).with(csrf())
                .param("admin_id", "999999").param("donanteNombre", "Donante seguro").param("donanteTelefono", "123")
                .param("fechaRecepcion", java.time.LocalDateTime.now().minusHours(1).toString()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("/admin/donaciones/*?registrada")).andReturn();
        String ruta = resultado.getResponse().getRedirectedUrl();
        Long id = Long.valueOf(ruta.substring(ruta.lastIndexOf('/') + 1, ruta.indexOf('?')));
        assertThat(donaciones.findById(id).orElseThrow().getRecibidoPor().getId()).isEqualTo(admin.getId());
    }
}
