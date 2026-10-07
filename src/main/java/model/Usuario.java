package model;

import enums.RolUsuario;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 255)
    private String contraseniaHash;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Direccion> direcciones = new ArrayList<>();

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContraseniaHash() {
        return contraseniaHash;
    }

    public void setContraseniaHash(String contraseniaHash) {
        this.contraseniaHash = contraseniaHash;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public List<Direccion> getDirecciones() {
        return Collections.unmodifiableList(direcciones);
    }

    public Usuario(String nombre, String correo, String contraseniaHash, String telefono, RolUsuario rol) {
        this.nombre = Objects.requireNonNull(nombre);
        this.correo = Objects.requireNonNull(correo);
        this.contraseniaHash = Objects.requireNonNull(contraseniaHash);
        this.telefono = Objects.requireNonNull(telefono);
        this.rol = Objects.requireNonNull(rol);
    }

    public void setRol(RolUsuario rol) {
        this.rol = Objects.requireNonNull(rol);
    }

    public void agregarDireccion(Direccion direccion) {
        Objects.requireNonNull(direccion);
        if (direccion.getUsuario() != this) {
            throw new IllegalArgumentException("La direccion pertenece a otro usuario");
        }
        if (!direcciones.contains(direccion)) {
            direcciones.add(direccion);
        }
    }

    public void eliminarDireccion(Direccion direccion) {
        direcciones.remove(direccion);
    }
}
