package model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "movimientoInventario")
public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private int cambioCantidad;

    @Column(nullable = false, length = 200)
    private String motivo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "registradoPor_id", nullable = false)
    private Usuario registradoPor;

    public MovimientoInventario() {
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getCambioCantidad() {
        return cambioCantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public Producto getProducto() {
        return producto;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public MovimientoInventario(Producto producto, Usuario registradoPor, int cambioCantidad,
                                String motivo, LocalDateTime fecha) {
        if (cambioCantidad != -1 && cambioCantidad != 1) {
            throw new IllegalArgumentException("Una pieza unica solo admite entrada o salida de una unidad");
        }
        this.producto = Objects.requireNonNull(producto);
        this.registradoPor = Objects.requireNonNull(registradoPor);
        this.cambioCantidad = cambioCantidad;
        this.motivo = Objects.requireNonNull(motivo);
        this.fecha = Objects.requireNonNull(fecha);
    }
}

