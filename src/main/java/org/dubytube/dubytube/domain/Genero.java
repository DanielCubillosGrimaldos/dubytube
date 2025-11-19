package org.dubytube.dubytube.domain;

import java.util.Objects;

/**
 * Entidad que representa un género musical en el catálogo.
 * 
 * <p>Los géneros son administrados exclusivamente por usuarios ADMIN
 * y sirven para categorizar las canciones en el sistema.</p>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class Genero {
    
    /** Identificador único del género */
    private final String id;
    
    /** Nombre del género (Rock, Pop, Jazz, etc.) */
    private String nombre;
    
    /** Descripción del género */
    private String descripcion;

    /**
     * Constructor completo del género.
     * 
     * @param id Identificador único
     * @param nombre Nombre del género
     * @param descripcion Descripción del género
     */
    public Genero(String id, String nombre, String descripcion) {
        this.id = Objects.requireNonNull(id, "El ID del género no puede ser null");
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    /**
     * Constructor sin descripción.
     * 
     * @param id Identificador único
     * @param nombre Nombre del género
     */
    public Genero(String id, String nombre) {
        this(id, nombre, "");
    }

    /**
     * Constructor vacío para deserialización JSON.
     */
    public Genero() {
        this.id = null;
    }

    // -------- Getters / Setters --------
    
    public String getId() {
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

    // -------- Igualdad y Hash --------
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genero)) return false;
        Genero genero = (Genero) o;
        return id.equals(genero.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
