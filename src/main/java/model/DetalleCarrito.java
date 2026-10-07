package model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "detalleCarrito")
public class DetalleCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad = 1;

    public DetalleCarrito() {
    }

    public Long getId() {
        return id;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    DetalleCarrito(Carrito carrito, Producto producto) {
        this.carrito = Objects.requireNonNull(carrito);
        this.producto = Objects.requireNonNull(producto);
    }
}

