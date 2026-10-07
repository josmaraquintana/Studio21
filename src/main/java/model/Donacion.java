package model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "donacion")
public class Donacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "donante_id", nullable = false)
    private Donante donante;

    @Column(nullable = false)
    private LocalDateTime fechaRecepcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recibidoPor_id", nullable = false)
    private Usuario recibidoPor;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL)
    private List<Producto> productos = new ArrayList<>();

    public Donacion() {
    }

    public Long getId() {
        return id;
    }

    public Donante getDonante() {
        return donante;
    }

    public LocalDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }

    public Usuario getRecibidoPor() {
        return recibidoPor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public Donacion(Donante donante, LocalDateTime fechaRecepcion, Usuario recibidoPor, String observaciones) {
        this.donante = Objects.requireNonNull(donante);
        this.fechaRecepcion = Objects.requireNonNull(fechaRecepcion);
        this.recibidoPor = Objects.requireNonNull(recibidoPor);
        this.observaciones = observaciones;
    }

    public void agregarProducto(Producto producto) {
        Objects.requireNonNull(producto);
        if (producto.getDonacion() != this) {
            throw new IllegalArgumentException("La pieza pertenece a otra donacion");
        }
        if (!productos.contains(producto)) {
            productos.add(producto);
        }
    }
}

