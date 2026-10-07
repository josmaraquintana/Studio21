package model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    private boolean ropa;

    public Categoria() {
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isRopa() {
        return ropa;
    }

    public void setRopa(boolean ropa) {
        this.ropa = ropa;
    }

    public Categoria(String nombre, String descripcion, boolean ropa) {
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = descripcion;
        this.ropa = ropa;
    }
}

