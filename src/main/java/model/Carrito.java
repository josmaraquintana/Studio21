package model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "carrito")
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCarrito> detalles = new ArrayList<>();

    public Carrito() {
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<DetalleCarrito> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public Carrito(Usuario usuario) {
        this.usuario = Objects.requireNonNull(usuario);
    }

    public void agregarProducto(Producto producto) {
        Objects.requireNonNull(producto);
        if (!producto.estaDisponibleParaVenta()) {
            throw new IllegalStateException("La pieza no esta disponible");
        }
        for (DetalleCarrito detalle : detalles) {
            if (detalle.getProducto() == producto || (producto.getId() != null
                    && producto.getId().equals(detalle.getProducto().getId()))) {
                throw new IllegalArgumentException("La pieza ya esta en el carrito");
            }
        }
        detalles.add(new DetalleCarrito(this, producto));
    }

    public void eliminarDetalle(DetalleCarrito detalle) {
        detalles.remove(detalle);
    }

    public void vaciar() {
        detalles.clear();
    }
}
