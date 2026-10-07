package model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "direccion")
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String destinatario;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 150)
    private String calle;

    @Column(nullable = false, length = 20)
    private String numeroExterior;

    @Column(nullable = false, length = 100)
    private String colonia;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false, name = "estado_direccion", length = 100)
    private String estado;

    @Column(nullable = false, length = 10)
    private String codigoPostal;

    public Direccion() {
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumeroExterior() {
        return numeroExterior;
    }

    public void setNumeroExterior(String numeroExterior) {
        this.numeroExterior = numeroExterior;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public Direccion(Usuario usuario, String destinatario, String telefono, String calle, String numeroExterior, String colonia, String ciudad, String estado, String codigoPostal) {
        this.usuario = Objects.requireNonNull(usuario);
        this.destinatario = Objects.requireNonNull(destinatario);
        this.telefono = Objects.requireNonNull(telefono);
        this.calle = Objects.requireNonNull(calle);
        this.numeroExterior = Objects.requireNonNull(numeroExterior);
        this.colonia = Objects.requireNonNull(colonia);
        this.ciudad = Objects.requireNonNull(ciudad);
        this.estado = Objects.requireNonNull(estado);
        this.codigoPostal = Objects.requireNonNull(codigoPostal);
        usuario.agregarDireccion(this);
    }

    public DireccionEnvio crearCopiaEnvio() {
        return new DireccionEnvio(destinatario, telefono, calle, numeroExterior, colonia, ciudad, estado, codigoPostal);
    }
}
