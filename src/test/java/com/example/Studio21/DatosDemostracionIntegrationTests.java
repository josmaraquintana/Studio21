package com.example.Studio21;

import com.example.Studio21.enums.RolUsuario;
import com.example.Studio21.repository.UsuarioRepository;
import com.example.Studio21.vista.DatosMaquetado;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Base exclusiva para esta clase: nunca carga los IDs fijos en la base real ni en otros tests.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:studio21mock;MODE=MySQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DatosDemostracionIntegrationTests {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired MockMvc mvc;
    @Autowired DatosMaquetado maquetado;

    @BeforeAll void cargarEnMemoria() throws Exception {
        String sql = Files.readString(Path.of("datos-mock.sql"));
        // Solo se omite la selección de base de MySQL; INSERT y UPDATE se ejecutan sin cambios.
        sql = sql.replace("USE studio21db;", "");
        try (var conexion = dataSource.getConnection()) {
            assertThat(conexion.getMetaData().getURL()).startsWith("jdbc:h2:mem:studio21mock");
            ScriptUtils.executeSqlScript(conexion,
                    new EncodedResource(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        }
    }

    @Test void usuariosTienenIdentidadesRolesYHashesCorrectos() {
        assertThat(usuarios.count()).isEqualTo(4);
        String[] nombres = {"Ori", "Jos", "Piña", "Cliente de prueba"};
        String[] correos = {"ori@studio21.com", "jos@studio21.com", "pina@studio21.com", "cliente@example.com"};
        for (int i = 0; i < 4; i++) {
            var usuario = usuarios.findById((long) i + 1).orElseThrow();
            assertThat(usuario.getNombre()).isEqualTo(nombres[i]);
            assertThat(usuario.getCorreo()).isEqualTo(correos[i]);
            assertThat(usuario.getRol()).isEqualTo(i < 3 ? RolUsuario.ADMIN : RolUsuario.CLIENTE);
            assertThat(usuario.getContraseniaHash()).startsWith("$2a$");
            assertThat(encoder.matches(i < 3 ? "admin123" : "cliente123", usuario.getContraseniaHash())).isTrue();
            assertThat(encoder.matches("incorrecta", usuario.getContraseniaHash())).isFalse();
            assertThat(usuario.getTelefono()).isEqualTo("000-000-000" + (i + 1));
        }
    }

    @Test void losTresAdministradoresPuedenEntrarYElClienteNo() throws Exception {
        for (String correo : List.of("ori@studio21.com", "jos@studio21.com", "pina@studio21.com")) {
            var resultado = mvc.perform(post("/admin/login").with(csrf()).param("correo", correo).param("password", "admin123"))
                    .andExpect(redirectedUrl("/admin")).andReturn();
            var sesion = (MockHttpSession) resultado.getRequest().getSession(false);
            assertThat(sesion.getAttribute("admin_nombre")).isEqualTo(usuarios.findByCorreo(correo).orElseThrow().getNombre());
            mvc.perform(get("/admin/pedidos/1").session(sesion)).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Cliente de prueba")));
        }
        mvc.perform(post("/admin/login").with(csrf()).param("correo", "cliente@example.com").param("password", "cliente123"))
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test void direccionesYPedidoPertenecenAlClienteYLaCopiaCoincide() {
        assertThat(jdbc.queryForList("select usuario_id from direccion order by id", Long.class)).containsExactly(4L, 4L);
        assertThat(jdbc.queryForObject("select usuario_id from pedido where id=1", Long.class)).isEqualTo(4L);
        for (String columna : List.of("destinatario", "telefono", "calle", "numero_exterior", "colonia", "ciudad", "estado_direccion", "codigo_postal")) {
            String valor = jdbc.queryForObject("select " + columna + " from pedido where id=1", String.class);
            assertThat(valor).isEqualTo(jdbc.queryForObject("select " + columna + " from direccion where id=1", String.class));
        }
        assertThat(jdbc.queryForObject("select destinatario from pedido where id=1", String.class)).isEqualTo("Cliente de prueba");
        assertThat(jdbc.queryForObject("select count(*) from direccion where usuario_id=3", Long.class)).isZero();
    }

    @Test void responsablesSonAdministradoresYParticipanLosTres() {
        assertThat(jdbc.queryForList("select recibido_por_id from donacion order by id", Long.class)).containsExactly(2L, 3L);
        assertThat(jdbc.queryForList("select verificado_por_id from producto where verificado_por_id is not null order by id", Long.class)).containsExactly(1L, 3L);
        assertThat(jdbc.queryForList("select registrado_por_id from movimiento_inventario order by id", Long.class)).containsExactly(2L, 2L, 3L);
        assertThat(jdbc.queryForList("select nombre from donante order by id", String.class)).containsExactly("Donante de prueba 1", "Donante de prueba 2");
        assertThat(jdbc.queryForList("select correo from donante order by id", String.class)).containsExactly("donante1@example.com", "donante2@example.com");
        assertThat(jdbc.queryForObject("select count(*) from usuario where rol='ADMIN'", Long.class)).isEqualTo(3);
    }

    @Test void piezasPedidoFechasYSaldoFisicoSonCoherentes() {
        assertThat(jdbc.queryForObject("select count(*) from producto where id=1 and activo=true and estado_verificacion='VERIFICADA' and estado_disponibilidad='DISPONIBLE'", Long.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from producto where id=2 and pedido_reserva_id=1 and estado_disponibilidad='APARTADA'", Long.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from producto where id=3 and activo=false and estado_verificacion='PENDIENTE' and verificado_por_id is null and fecha_verificacion is null", Long.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from pedido where id=1 and confirmado=true and estado='PENDIENTE' and fecha_envio is null and fecha_entrega is null", Long.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select total from pedido where id=1", java.math.BigDecimal.class))
                .isEqualByComparingTo(jdbc.queryForObject("select sum(cantidad*precio_unitario_compra) from detalle_pedido where pedido_id=1", java.math.BigDecimal.class))
                .isEqualByComparingTo("28.00");
        assertThat(jdbc.queryForObject("select count(*) from detalle_pedido d join producto p on p.id=d.producto_id where d.precio_unitario_compra<>p.precio_reventa or d.cantidad<>1", Long.class)).isZero();
        assertThat(jdbc.queryForList("select sum(cambio_cantidad) from movimiento_inventario group by producto_id order by producto_id", Long.class)).containsExactly(1L, 1L, 1L);
        assertThat(jdbc.queryForObject("select count(*) from movimiento_inventario", Long.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from producto p join donacion d on d.id=p.donacion_id where p.fecha_verificacion<d.fecha_recepcion", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from movimiento_inventario m join producto p on p.id=m.producto_id join donacion d on d.id=p.donacion_id where m.fecha<d.fecha_recepcion or (p.fecha_verificacion is not null and m.fecha>p.fecha_verificacion)", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from detalle_pedido d join producto p on p.id=d.producto_id join pedido pe on pe.id=d.pedido_id where pe.fecha_compra<p.fecha_verificacion", Long.class)).isZero();
    }

    @Test void pantallasPublicasMantienenElClienteYSuDireccionDePrueba() throws Exception {
        for (String ruta : List.of("/cuenta", "/direcciones", "/pedido?id=8492", "/checkout?id=motomami")) {
            mvc.perform(get(ruta)).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Cliente de prueba")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("000-000-0004")));
        }
        mvc.perform(get("/pedido?id=8492")).andExpect(content().string(org.hamcrest.Matchers.containsString("25 oct 2026")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Calle de Prueba 123")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("$28.00")));
        assertThat(maquetado.producto("eras")).containsEntry("sku", "S21-002").containsEntry("don", "DON-1").containsEntry("status", "Apartada");
        assertThat(maquetado.producto("motomami")).containsEntry("sku", "S21-001").containsEntry("don", "DON-1");
        assertThat(maquetado.producto("revision")).containsEntry("sku", "S21-003").containsEntry("don", "DON-2").containsEntry("verificado", false);
    }
}
