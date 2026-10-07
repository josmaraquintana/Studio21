package model;

import enums.CondicionProducto;
import enums.EstadoDisponibilidad;
import enums.EstadoVerificacion;
import enums.RolUsuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioOriginal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioReventa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicionProducto condicion;

    @Column(length = 500)
    private String detalleCondicion;

    @Column(length = 20)
    private String talla;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDisponibilidad estadoDisponibilidad = EstadoDisponibilidad.DISPONIBLE;

    @Column(nullable = false)
    private boolean activo = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artista_id", nullable = false)
    private Artista artista;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(optional = false)
    @JoinColumn(name = "donacion_id", nullable = false)
    private Donacion donacion;

    @ManyToOne(optional = true)
    @JoinColumn(name = "verificadoPor_id")
    private Usuario verificadoPor;

    @Column()
    private LocalDateTime fechaVerificacion;

    @Column(length = 1000)
    private String evidenciaVerificacion;

    @ManyToOne(optional = true)
    @JoinColumn(name = "pedidoReserva_id")
    private Pedido pedidoReserva;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenProducto> imagenes = new ArrayList<>();

    public Producto() {
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioOriginal() {
        return precioOriginal;
    }

    public BigDecimal getPrecioReventa() {
        return precioReventa;
    }

    public CondicionProducto getCondicion() {
        return condicion;
    }

    public String getDetalleCondicion() {
        return detalleCondicion;
    }

    public String getTalla() {
        return talla;
    }

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }

    public EstadoDisponibilidad getEstadoDisponibilidad() {
        return estadoDisponibilidad;
    }

    public boolean isActivo() {
        return activo;
    }

    public Artista getArtista() {
        return artista;
    }

    public void setArtista(Artista artista) {
        this.artista = artista;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Donacion getDonacion() {
        return donacion;
    }

    public Usuario getVerificadoPor() {
        return verificadoPor;
    }

    public LocalDateTime getFechaVerificacion() {
        return fechaVerificacion;
    }

    public String getEvidenciaVerificacion() {
        return evidenciaVerificacion;
    }

    public Pedido getPedidoReserva() {
        return pedidoReserva;
    }

    public List<ImagenProducto> getImagenes() {
        return Collections.unmodifiableList(imagenes);
    }

    public Producto(String nombre, String descripcion, BigDecimal precioOriginal, BigDecimal precioReventa,
                    CondicionProducto condicion, String detalleCondicion, String talla,
                    Artista artista, Categoria categoria, Donacion donacion) {
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = Objects.requireNonNull(descripcion);
        setPrecioOriginal(precioOriginal);
        setPrecioReventa(precioReventa);
        this.condicion = Objects.requireNonNull(condicion);
        this.detalleCondicion = detalleCondicion;
        this.talla = talla;
        this.artista = Objects.requireNonNull(artista);
        this.categoria = Objects.requireNonNull(categoria);
        this.donacion = Objects.requireNonNull(donacion);
        validarDatosPublicacion();
        donacion.agregarProducto(this);
    }

    public void setPrecioOriginal(BigDecimal precioOriginal) {
        if (precioOriginal != null && precioOriginal.signum() < 0) {
            throw new IllegalArgumentException("El precio original no puede ser negativo");
        }
        this.precioOriginal = precioOriginal;
    }

    public void setPrecioReventa(BigDecimal precioReventa) {
        exigirDisponible();
        if (precioReventa == null || precioReventa.signum() <= 0) {
            throw new IllegalArgumentException("El precio de reventa debe ser mayor que cero");
        }
        this.precioReventa = precioReventa;
    }

    public void setCondicion(CondicionProducto condicion) {
        exigirEdicion();
        this.condicion = Objects.requireNonNull(condicion);
    }

    public void setDetalleCondicion(String detalleCondicion) {
        exigirEdicion();
        this.detalleCondicion = detalleCondicion;
    }

    public void setTalla(String talla) {
        exigirEdicion();
        this.talla = talla;
    }

    public void setCategoria(Categoria categoria) {
        exigirEdicion();
        this.categoria = Objects.requireNonNull(categoria);
    }

    public int getStock() {
        return estadoDisponibilidad == EstadoDisponibilidad.VENDIDA ? 0 : 1;
    }

    public void verificarAutenticidad(Usuario verificador, String evidencia) {
        exigirEdicion();
        registrarRevision(verificador, evidencia);
        estadoVerificacion = EstadoVerificacion.VERIFICADA;
    }

    public void rechazarAutenticidad(Usuario verificador, String motivo) {
        exigirEdicion();
        registrarRevision(verificador, motivo);
        estadoVerificacion = EstadoVerificacion.RECHAZADA;
    }

    private void registrarRevision(Usuario verificador, String evidencia) {
        Objects.requireNonNull(verificador);
        if (verificador.getRol() != RolUsuario.ADMIN) {
            throw new IllegalArgumentException("La revision debe realizarla un administrador");
        }
        if (evidencia == null || evidencia.isBlank()) {
            throw new IllegalArgumentException("Debe registrar la evidencia de la revision");
        }
        verificadoPor = verificador;
        fechaVerificacion = LocalDateTime.now();
        evidenciaVerificacion = evidencia;
    }

    public void publicar() {
        exigirDisponible();
        if (estadoVerificacion != EstadoVerificacion.VERIFICADA) {
            throw new IllegalStateException("No se puede publicar una pieza sin verificar");
        }
        validarDatosPublicacion();
        activo = true;
    }

    public void retirarPublicacion() {
        exigirDisponible();
        activo = false;
    }

    public boolean estaDisponibleParaVenta() {
        return activo && estadoVerificacion == EstadoVerificacion.VERIFICADA
                && estadoDisponibilidad == EstadoDisponibilidad.DISPONIBLE;
    }

    void reservar(Pedido pedido) {
        if (!estaDisponibleParaVenta()) {
            throw new IllegalStateException("La pieza no esta disponible para apartarse");
        }
        pedidoReserva = Objects.requireNonNull(pedido);
        estadoDisponibilidad = EstadoDisponibilidad.APARTADA;
    }

    boolean estaApartadaPor(Pedido pedido) {
        return estadoDisponibilidad == EstadoDisponibilidad.APARTADA
                && pedidoReserva != null
                && (pedidoReserva == pedido || (pedidoReserva.getId() != null
                && pedidoReserva.getId().equals(pedido.getId())));
    }

    void vender(Pedido pedido) {
        if (!estaApartadaPor(pedido)) {
            throw new IllegalStateException("La pieza no esta apartada para este pedido");
        }
        estadoDisponibilidad = EstadoDisponibilidad.VENDIDA;
        pedidoReserva = null;
        activo = false;
    }

    void liberar(Pedido pedido) {
        if (!estaApartadaPor(pedido)) {
            throw new IllegalStateException("La pieza no esta apartada para este pedido");
        }
        estadoDisponibilidad = EstadoDisponibilidad.DISPONIBLE;
        pedidoReserva = null;
    }

    public void agregarImagen(ImagenProducto imagen) {
        Objects.requireNonNull(imagen);
        if (imagen.getProducto() != this) {
            throw new IllegalArgumentException("La imagen pertenece a otra pieza");
        }
        if (!imagenes.contains(imagen)) {
            imagenes.add(imagen);
        }
    }

    public void eliminarImagen(ImagenProducto imagen) {
        imagenes.remove(imagen);
    }

    private void exigirDisponible() {
        if (estadoDisponibilidad != EstadoDisponibilidad.DISPONIBLE) {
            throw new IllegalStateException("La pieza esta apartada o vendida");
        }
    }

    private void exigirEdicion() {
        exigirDisponible();
        if (activo) {
            throw new IllegalStateException("Retire la publicacion antes de modificar la pieza");
        }
    }

    private void validarDatosPublicacion() {
        Objects.requireNonNull(condicion);
        Objects.requireNonNull(categoria);
        Objects.requireNonNull(donacion);
        if (categoria.isRopa() && (talla == null || talla.isBlank())) {
            throw new IllegalArgumentException("La ropa debe tener talla");
        }
        if (condicion == CondicionProducto.USADA_CON_DETALLES
                && (detalleCondicion == null || detalleCondicion.isBlank())) {
            throw new IllegalArgumentException("Debe describir los detalles de uso de la pieza");
        }
        if (precioReventa == null || precioReventa.signum() <= 0) {
            throw new IllegalArgumentException("El precio de reventa debe ser mayor que cero");
        }
    }
}
