package org.dubytube.dubytube.domain;

import java.util.Objects;

public class Cancion {
    private final String id;
    private String titulo;
    private String artista;
    private String genero;
    private int anio;
    private int duracionSeg;

    public Cancion(String id, String titulo, String artista, String genero, int anio, int duracionSeg) {
        this.id = Objects.requireNonNull(id);
        this.titulo = titulo;
        this.artista = artista;
        this.genero = genero;
        this.anio = anio;
        this.duracionSeg = duracionSeg;
    }
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public int getDuracionSeg() { return duracionSeg; }
    public void setDuracionSeg(int duracionSeg) { this.duracionSeg = duracionSeg; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cancion)) return false;
        return id.equals(((Cancion) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
}

