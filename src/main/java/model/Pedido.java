package model;

import enums.EstadoPedido;
import enums.MetodoPago;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, length = 30)
    private String numeroPedido;

    @Column()
    private LocalDateTime fechaCompra;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;

    @Embedded
    private DireccionEnvio direccionEnvio;

    @Column()
    private LocalDateTime fechaEnvio;

    @Column()
    private LocalDateTime fechaEntrega;

    @Column(length = 100)
    private String paqueteria;

    @Column(length = 100)
    private String numeroGuia;

    @Column(nullable = false)
    private boolean confirmado = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public String getPaqueteria() {
        return paqueteria;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public Pedido(String numeroPedido, Usuario usuario, Direccion direccion, MetodoPago metodoPago) {
        this.numeroPedido = Objects.requireNonNull(numeroPedido);
        this.usuario = Objects.requireNonNull(usuario);
        this.metodoPago = Objects.requireNonNull(metodoPago);
        seleccionarDireccion(direccion);
    }

    public DireccionEnvio getDireccionEnvio() {
        return direccionEnvio == null ? null : new DireccionEnvio(direccionEnvio);
    }

    public void seleccionarDireccion(Direccion direccion) {
        exigirEditable();
        Objects.requireNonNull(direccion);
        Usuario propietario = direccion.getUsuario();
        if (propietario != usuario && (usuario == null || usuario.getId() == null
                || !usuario.getId().equals(propietario.getId()))) {
            throw new IllegalArgumentException("La direccion debe pertenecer al comprador");
        }
        direccionEnvio = direccion.crearCopiaEnvio();
    }

    public void agregarProducto(Producto producto) {
        exigirEditable();
        Objects.requireNonNull(producto);
        if (!producto.estaDisponibleParaVenta()) {
            throw new IllegalStateException("La pieza no esta disponible");
        }
        for (DetallePedido detalle : detalles) {
            if (detalle.getProducto() == producto || (producto.getId() != null
                    && producto.getId().equals(detalle.getProducto().getId()))) {
                throw new IllegalArgumentException("La pieza ya esta en el pedido");
            }
        }
        detalles.add(new DetallePedido(this, producto));
        calcularTotal();
    }

    public void eliminarDetalle(DetallePedido detalle) {
        exigirEditable();
        detalles.remove(detalle);
        calcularTotal();
    }

    public void confirmar() {
        exigirEditable();
        if (detalles.isEmpty() || direccionEnvio == null || usuario == null || metodoPago == null) {
            throw new IllegalStateException("El pedido requiere comprador, direccion, pago y piezas");
        }
        for (DetallePedido detalle : detalles) {
            if (!detalle.getProducto().estaDisponibleParaVenta()) {
                throw new IllegalStateException("Una pieza ya no esta disponible");
            }
        }
        for (DetallePedido detalle : detalles) {
            detalle.congelarPrecio();
            detalle.getProducto().reservar(this);
        }
        calcularTotal();
        fechaCompra = LocalDateTime.now();
        confirmado = true;
    }

    public void pagar() {
        if (!confirmado || estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede pagar un pedido confirmado y pendiente");
        }
        validarApartados();
        for (DetallePedido detalle : detalles) {
            detalle.getProducto().vender(this);
        }
        estado = EstadoPedido.PAGADO;
    }

    public void cancelar() {
        if (estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede cancelar un pedido pendiente de pago");
        }
        if (confirmado) {
            validarApartados();
            for (DetallePedido detalle : detalles) {
                detalle.getProducto().liberar(this);
            }
        }
        estado = EstadoPedido.CANCELADO;
    }

    public void procesar() {
        if (estado != EstadoPedido.PAGADO) {
            throw new IllegalStateException("El pedido debe estar pagado");
        }
        estado = EstadoPedido.PROCESANDO;
    }

    public void enviar(String paqueteria, String numeroGuia, LocalDateTime fechaEnvio) {
        if (estado != EstadoPedido.PROCESANDO) {
            throw new IllegalStateException("El pedido debe estar en procesamiento");
        }
        if (paqueteria == null || paqueteria.isBlank() || numeroGuia == null || numeroGuia.isBlank()) {
            throw new IllegalArgumentException("El envio requiere paqueteria y numero de guia");
        }
        if (fechaEnvio == null || fechaEnvio.isBefore(fechaCompra)) {
            throw new IllegalArgumentException("La fecha de envio no puede ser anterior a la compra");
        }
        this.paqueteria = paqueteria;
        this.numeroGuia = numeroGuia;
        this.fechaEnvio = fechaEnvio;
        estado = EstadoPedido.ENVIADO;
    }

    public void entregar(LocalDateTime fechaEntrega) {
        if (estado != EstadoPedido.ENVIADO) {
            throw new IllegalStateException("El pedido debe estar enviado");
        }
        if (fechaEntrega == null || fechaEntrega.isBefore(fechaEnvio)) {
            throw new IllegalArgumentException("La entrega no puede ser anterior al envio");
        }
        this.fechaEntrega = fechaEntrega;
        estado = EstadoPedido.ENTREGADO;
    }

    private void exigirEditable() {
        if (confirmado || estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("El pedido ya no se puede modificar");
        }
    }

    private void validarApartados() {
        for (DetallePedido detalle : detalles) {
            if (!detalle.getProducto().estaApartadaPor(this)) {
                throw new IllegalStateException("Una pieza no pertenece al apartado de este pedido");
            }
        }
    }

    private void calcularTotal() {
        total = BigDecimal.ZERO;
        for (DetallePedido detalle : detalles) {
            total = total.add(detalle.getPrecioUnitarioCompra());
        }
    }
}

