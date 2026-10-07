package model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "donante")
public class Donante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 150)
    private String correo;

    public Donante() {
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Donante(String nombre, String telefono, String correo) {
        this.nombre = Objects.requireNonNull(nombre);
        this.telefono = Objects.requireNonNull(telefono);
        this.correo = correo;
    }
}
