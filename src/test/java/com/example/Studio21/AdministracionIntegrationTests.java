package com.example.Studio21;

import com.example.Studio21.enums.*;
import com.example.Studio21.model.*;
import com.example.Studio21.repository.*;
import com.example.Studio21.service.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

// Sin @Transactional en el test: cada servicio confirma o revierte su propia transacción real.
@SpringBootTest
@ActiveProfiles("test")
class AdministracionIntegrationTests {
    @Autowired ProductoService productos;
    @Autowired InventarioService inventario;
    @Autowired PedidoService pedidos;
    @Autowired DonacionService donaciones;
    @Autowired ProductoRepository productoRepository;
    @Autowired PedidoRepository pedidoRepository;
    @Autowired MovimientoInventarioRepository movimientos;
    @Autowired DonanteRepository donantes;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired PasswordEncoder encoder;
    Usuario admin, cliente;
    Artista artista;
    Categoria categoria;
    Donacion donacion;
    Long pieza;

    <T> T tx(Supplier<T> accion) {
        return new TransactionTemplate(transactionManager).execute(status -> accion.get());
    }

    @BeforeEach
    void preparar() {
        tx(() -> {
            String codigo = UUID.randomUUID().toString();
            admin = new Usuario("Admin", codigo + "@admin.test", encoder.encode("clave-test"), "123", RolUsuario.ADMIN);
            cliente = new Usuario("Cliente", codigo + "@cliente.test", encoder.encode("clave-test"), "123", RolUsuario.CLIENTE);
            em.persist(admin); em.persist(cliente);
            artista = new Artista("Artista", "Pop"); em.persist(artista);
            categoria = new Categoria("Discos", "Discos", false); em.persist(categoria);
            Donante donante = new Donante("Donante", "123", null); em.persist(donante);
            donacion = new Donacion(donante, LocalDateTime.now().minusDays(1), admin, null); em.persist(donacion);
            return null;
        });
        pieza = crearPieza();
    }

    Long crearPieza() {
        return productos.crear("Disco", "Pieza única", artista.getId(), categoria.getId(), donacion.getId(),
                CondicionProducto.NUEVA, null, null, null, BigDecimal.TEN, null, admin).getId();
    }

    Long crearPedido(Long... piezas) {
        for (Long id : piezas) {
            productos.verificarAutenticidad(id, admin, "Evidencia");
            productos.publicar(id);
        }
        return tx(() -> {
            Usuario comprador = em.find(Usuario.class, cliente.getId());
            Direccion direccion = new Direccion(comprador, "Cliente", "123", "Calle", "1", "Centro", "Ciudad", "Estado", "12345");
            em.persist(direccion);
            Pedido pedido = new Pedido(UUID.randomUUID().toString().substring(0, 25), comprador, direccion, MetodoPago.TRANSFERENCIA);
            em.persist(pedido);
            for (Long id : piezas) pedido.agregarProducto(em.find(Producto.class, id));
            pedido.confirmar();
            return pedido.getId();
        });
    }

    @Test void rechazaMovimientosDuplicadosYBloqueaPublicacionSinExistencia() {
        assertThat(movimientos.saldoFisico(pieza)).isEqualTo(1);
        assertThatThrownBy(() -> inventario.registrarAjuste(pieza, 1, "Duplicada", admin)).isInstanceOf(IllegalStateException.class);
        productos.verificarAutenticidad(pieza, admin, "Evidencia");
        productos.publicar(pieza);
        inventario.registrarAjuste(pieza, -1, "Pérdida", admin);
        assertThat(productoRepository.findById(pieza).orElseThrow().estaDisponibleParaVenta()).isFalse();
        assertThatThrownBy(() -> inventario.registrarAjuste(pieza, -1, "Duplicada", admin)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> productos.publicar(pieza)).isInstanceOf(IllegalStateException.class);
        inventario.registrarAjuste(pieza, 1, "Pieza recuperada", admin);
        assertThat(productoRepository.findById(pieza).orElseThrow().isActivo()).isFalse();
        assertThat(movimientos.saldoFisico(pieza)).isEqualTo(1);
    }

    @Test void autenticidadNoMueveExistencias() {
        productos.rechazarAutenticidad(pieza, admin, "No original");
        assertThatThrownBy(() -> productos.publicar(pieza)).isInstanceOf(IllegalStateException.class);
        productos.verificarAutenticidad(pieza, admin, "Nueva evidencia");
        assertThat(movimientos.findByProductoId(pieza)).hasSize(1);
    }

    @Test void cancelarLiberaSinEntradaFisica() {
        Long pedido = crearPedido(pieza);
        assertThatThrownBy(() -> inventario.registrarAjuste(pieza, -1, "Intento de saltar reserva", admin)).isInstanceOf(IllegalStateException.class);
        pedidos.cancelar(pedido);
        assertThat(productoRepository.findById(pieza).orElseThrow().getEstadoDisponibilidad()).isEqualTo(EstadoDisponibilidad.DISPONIBLE);
        assertThat(movimientos.findByProductoId(pieza)).hasSize(1);
        assertThat(movimientos.saldoFisico(pieza)).isEqualTo(1);
    }

    @Test void pagoYDespachoSeparanDisponibilidadYExistencia() {
        Long pedido = crearPedido(pieza);
        pedidos.registrarPago(pedido);
        assertThat(productoRepository.findById(pieza).orElseThrow().getEstadoDisponibilidad()).isEqualTo(EstadoDisponibilidad.VENDIDA);
        assertThat(movimientos.saldoFisico(pieza)).isEqualTo(1);
        assertThatThrownBy(() -> inventario.registrarAjuste(pieza, -1, "Salida manual", admin)).isInstanceOf(IllegalStateException.class);
        pedidos.comenzarPreparacion(pedido);
        String fecha = LocalDateTime.now().plusMinutes(1).toString();
        pedidos.registrarEnvio(pedido, "Paquetería", "GUIA", fecha, admin);
        assertThatThrownBy(() -> pedidos.registrarEnvio(pedido, "Paquetería", "GUIA", fecha, admin)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> inventario.registrarAjuste(pieza, 1, "Restaurar venta", admin)).isInstanceOf(IllegalStateException.class);
        assertThat(movimientos.saldoFisico(pieza)).isZero();
        assertThat(movimientos.findByProductoId(pieza)).hasSize(2);
    }

    @Test void falloEnSegundaSalidaReviertePedidoYPrimeraSalida() {
        Long segunda = crearPieza();
        Long pedido = crearPedido(pieza, segunda);
        pedidos.registrarPago(pedido);
        pedidos.comenzarPreparacion(pedido);
        // Fallo de escritura real en H2 al insertar la salida de la segunda pieza.
        jdbc.execute("alter table movimiento_inventario add constraint fallo_salida check (producto_id <> " + segunda + " or cambio_cantidad <> -1)");
        try {
            assertThatThrownBy(() -> pedidos.registrarEnvio(pedido, "Paquetería", "GUIA", LocalDateTime.now().plusMinutes(1).toString(), admin))
                    .isInstanceOf(RuntimeException.class);
        } finally { jdbc.execute("alter table movimiento_inventario drop constraint fallo_salida"); }
        assertThat(pedidoRepository.findById(pedido).orElseThrow().getEstado()).isEqualTo(EstadoPedido.PROCESANDO);
        assertThat(movimientos.findByProductoId(pieza)).hasSize(1);
        assertThat(movimientos.findByProductoId(segunda)).hasSize(1);
    }

    @Test void falloAlPagarRevierteTodasLasPiezasYPedido() {
        Long segunda = crearPieza();
        Long pedido = crearPedido(pieza, segunda);
        jdbc.execute("alter table producto add constraint fallo_pago check (id <> " + segunda + " or estado_disponibilidad <> 'VENDIDA')");
        try { assertThatThrownBy(() -> pedidos.registrarPago(pedido)).isInstanceOf(RuntimeException.class); }
        finally { jdbc.execute("alter table producto drop constraint fallo_pago"); }
        assertThat(pedidoRepository.findById(pedido).orElseThrow().getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        for (Long id : new Long[]{pieza, segunda}) {
            assertThat(productoRepository.findById(id).orElseThrow().getEstadoDisponibilidad()).isEqualTo(EstadoDisponibilidad.APARTADA);
            assertThat(movimientos.saldoFisico(id)).isEqualTo(1);
        }
    }

    @Test void falloAlCancelarRevierteLaLiberacionCompleta() {
        Long segunda = crearPieza();
        Long pedido = crearPedido(pieza, segunda);
        jdbc.execute("alter table producto add constraint fallo_cancelacion check (id <> " + segunda + " or estado_disponibilidad <> 'DISPONIBLE')");
        try { assertThatThrownBy(() -> pedidos.cancelar(pedido)).isInstanceOf(RuntimeException.class); }
        finally { jdbc.execute("alter table producto drop constraint fallo_cancelacion"); }
        assertThat(pedidoRepository.findById(pedido).orElseThrow().getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        for (Long id : new Long[]{pieza, segunda}) {
            assertThat(productoRepository.findById(id).orElseThrow().getEstadoDisponibilidad()).isEqualTo(EstadoDisponibilidad.APARTADA);
        }
    }

    @Test void falloDeDonacionRevierteNuevoDonante() {
        long antes = donantes.count();
        assertThatThrownBy(() -> donaciones.registrar(null, "Nuevo", "123", null,
                LocalDateTime.now().minusHours(1).toString(), "x".repeat(501), admin)).isInstanceOf(RuntimeException.class);
        assertThat(donantes.count()).isEqualTo(antes);
    }

    @Test void falloDeEntradaRevierteAltaDeProducto() {
        long antes = productoRepository.count();
        Usuario otro = tx(() -> {
            Usuario usuario = new Usuario("Otro admin", UUID.randomUUID() + "@test", encoder.encode("clave"), "123", RolUsuario.ADMIN);
            em.persist(usuario); return usuario;
        });
        jdbc.execute("alter table movimiento_inventario add constraint fallo_entrada check (registrado_por_id <> " + otro.getId() + ")");
        try {
            assertThatThrownBy(() -> productos.crear("Otra pieza", "Descripción", artista.getId(), categoria.getId(), donacion.getId(),
                    CondicionProducto.NUEVA, null, null, null, BigDecimal.TEN, null, otro)).isInstanceOf(RuntimeException.class);
        } finally { jdbc.execute("alter table movimiento_inventario drop constraint fallo_entrada"); }
        assertThat(productoRepository.count()).isEqualTo(antes);
    }

    @Test void solicitudesSimultaneasSoloPermitenUnaSalida() throws Exception {
        comprobarSoloUnExito(() -> inventario.registrarAjuste(pieza, -1, "Pérdida", admin));
        assertThat(movimientos.saldoFisico(pieza)).isZero();
        assertThat(movimientos.findByProductoId(pieza)).hasSize(2);
    }

    @Test void solicitudesSimultaneasSoloPermitenUnaEntrada() throws Exception {
        inventario.registrarAjuste(pieza, -1, "Pérdida", admin);
        comprobarSoloUnExito(() -> inventario.registrarAjuste(pieza, 1, "Recuperada", admin));
        assertThat(movimientos.saldoFisico(pieza)).isEqualTo(1);
        assertThat(movimientos.findByProductoId(pieza)).hasSize(3);
    }

    @Test void despachosSimultaneosSoloRegistranUnaSalidaPorPieza() throws Exception {
        Long pedido = crearPedido(pieza);
        pedidos.registrarPago(pedido);
        pedidos.comenzarPreparacion(pedido);
        String fecha = LocalDateTime.now().plusMinutes(1).toString();
        comprobarSoloUnExito(() -> pedidos.registrarEnvio(pedido, "Paquetería", "GUIA", fecha, admin));
        assertThat(movimientos.saldoFisico(pieza)).isZero();
        assertThat(movimientos.findByProductoId(pieza)).hasSize(2);
        assertThat(pedidoRepository.findById(pedido).orElseThrow().getEstado()).isEqualTo(EstadoPedido.ENVIADO);
    }

    private void comprobarSoloUnExito(Runnable accion) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        Callable<Boolean> salida = () -> {
            inicio.await();
            try { accion.run(); return true; }
            catch (IllegalStateException e) { return false; }
        };
        try {
            Future<Boolean> a = pool.submit(salida), b = pool.submit(salida);
            inicio.countDown();
            assertThat((a.get(10, TimeUnit.SECONDS) ? 1 : 0) + (b.get(10, TimeUnit.SECONDS) ? 1 : 0)).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }
}
