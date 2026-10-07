package model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "detallePedido")
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitarioCompra;

    public DetallePedido() {
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitarioCompra() {
        return precioUnitarioCompra;
    }

    DetallePedido(Pedido pedido, Producto producto) {
        this.pedido = Objects.requireNonNull(pedido);
        this.producto = Objects.requireNonNull(producto);
        this.precioUnitarioCompra = producto.getPrecioReventa();
    }

    void congelarPrecio() {
        if (pedido.isConfirmado()) {
            throw new IllegalStateException("El precio del pedido ya fue confirmado");
        }
        precioUnitarioCompra = producto.getPrecioReventa();
    }
}

