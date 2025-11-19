package org.dubytube.dubytube.domain;

import java.util.Objects;

/**
 * Entidad que representa una pista musical en el catálogo de DubyTube.
 * 
 * <p>Requisitos cumplidos:</p>
 * <ul>
 *   <li>RF-018: Almacena id (único), titulo, artista, genero, año, duración</li>
 *   <li>RF-019: Funciona como nodo en el Grafo de Similitud</li>
 *   <li>RF-020: Implementa hashCode() y equals() basado en id</li>
 * </ul>
 * 
 * <p>Esta clase es inmutable en su ID para garantizar consistencia en colecciones
 * basadas en hash y en el grafo de similitud.</p>
 * 
 * @author DubyTube Team
 * @version 2.0
 * @since 2025-11-18
 */
public class Cancion {
    /**
     * Identificador único de la canción (inmutable).
     * Usado como clave en HashMap y como identificador en el Grafo de Similitud.
     */
    private final String id;
    
    /** Título de la canción */
    private String titulo;
    
    /** Nombre del artista o banda */
    private String artista;
    
    /** Género musical (Rock, Pop, Jazz, etc.) */
    private String genero;
    
    /** Año de lanzamiento */
    private int anio;
    
    /** Duración de la canción en segundos */
    private int duracionSeg;
    
    /** Ruta del archivo de audio (relativa a la carpeta de datos) */
    private String archivoAudio;
    
    /** Username del usuario que subió esta canción */
    private String subidaPor;

    /**
     * Constructor completo de la canción.
     * 
     * @param id Identificador único (no puede ser null)
     * @param titulo Título de la canción
     * @param artista Nombre del artista
     * @param genero Género musical
     * @param anio Año de lanzamiento
     * @param duracionSeg Duración en segundos
     * @throws NullPointerException si el id es null
     */
    public Cancion(String id, String titulo, String artista, String genero, int anio, int duracionSeg) {
        this.id = Objects.requireNonNull(id, "El ID de la canción no puede ser null");
        this.titulo = titulo;
        this.artista = artista;
        this.genero = genero;
        this.anio = anio;
        this.duracionSeg = duracionSeg;
    }

    // -------- Getters y Setters --------
    
    /**
     * Obtiene el ID único de la canción.
     * @return ID de la canción
     */
    public String getId() { 
        return id; 
    }
    
    /**
     * Obtiene el título de la canción.
     * @return Título
     */
    public String getTitulo() { 
        return titulo; 
    }
    
    /**
     * Establece el título de la canción.
     * @param titulo Nuevo título
     */
    public void setTitulo(String titulo) { 
        this.titulo = titulo; 
    }
    
    /**
     * Obtiene el nombre del artista.
     * @return Nombre del artista
     */
    public String getArtista() { 
        return artista; 
    }
    
    /**
     * Establece el nombre del artista.
     * @param artista Nuevo nombre del artista
     */
    public void setArtista(String artista) { 
        this.artista = artista; 
    }
    
    /**
     * Obtiene el género musical.
     * @return Género
     */
    public String getGenero() { 
        return genero; 
    }
    
    /**
     * Establece el género musical.
     * @param genero Nuevo género
     */
    public void setGenero(String genero) { 
        this.genero = genero; 
    }
    
    /**
     * Obtiene el año de lanzamiento.
     * @return Año
     */
    public int getAnio() { 
        return anio; 
    }
    
    /**
     * Establece el año de lanzamiento.
     * @param anio Nuevo año
     */
    public void setAnio(int anio) { 
        this.anio = anio; 
    }
    
    /**
     * Obtiene la duración en segundos.
     * @return Duración en segundos
     */
    public int getDuracionSeg() { 
        return duracionSeg; 
    }
    
    /**
     * Establece la duración en segundos.
     * @param duracionSeg Nueva duración
     */
    public void setDuracionSeg(int duracionSeg) { 
        this.duracionSeg = duracionSeg; 
    }
    
    /**
     * Obtiene la ruta del archivo de audio.
     * @return Ruta relativa del archivo de audio
     */
    public String getArchivoAudio() {
        return archivoAudio;
    }
    
    /**
     * Establece la ruta del archivo de audio.
     * @param archivoAudio Ruta del archivo
     */
    public void setArchivoAudio(String archivoAudio) {
        this.archivoAudio = archivoAudio;
    }
    
    /**
     * Obtiene el username del usuario que subió esta canción.
     * @return Username del usuario
     */
    public String getSubidaPor() {
        return subidaPor;
    }
    
    /**
     * Establece el usuario que subió esta canción.
     * @param subidaPor Username del usuario
     */
    public void setSubidaPor(String subidaPor) {
        this.subidaPor = subidaPor;
    }

    // -------- Métodos auxiliares --------
    
    /**
     * Formatea la duración en formato MM:SS.
     * 
     * @return String con el formato "3:45"
     */
    public String getDuracionFormateada() {
        int minutos = duracionSeg / 60;
        int segundos = duracionSeg % 60;
        return String.format("%d:%02d", minutos, segundos);
    }

    /**
     * Calcula la similitud con otra canción basada en atributos comunes.
     * Usado por el Grafo de Similitud para calcular pesos de aristas.
     * 
     * <p>Criterios de similitud:</p>
     * <ul>
     *   <li>Mismo género: +40 puntos</li>
     *   <li>Mismo artista: +30 puntos</li>
     *   <li>Año cercano (±5 años): +20 puntos</li>
     *   <li>Duración similar (±30 seg): +10 puntos</li>
     * </ul>
     * 
     * @param otra Otra canción para comparar
     * @return Puntaje de similitud (0-100)
     */
    public double calcularSimilitud(Cancion otra) {
        if (otra == null || this.equals(otra)) {
            return 0.0;
        }

        double similitud = 0.0;

        // Mismo género: alta similitud
        if (this.genero != null && this.genero.equalsIgnoreCase(otra.genero)) {
            similitud += 40.0;
        }

        // Mismo artista: alta similitud
        if (this.artista != null && this.artista.equalsIgnoreCase(otra.artista)) {
            similitud += 30.0;
        }

        // Año cercano (±5 años): similitud moderada
        int diferenciaAnio = Math.abs(this.anio - otra.anio);
        if (diferenciaAnio <= 5) {
            similitud += 20.0 * (1.0 - diferenciaAnio / 5.0);
        }

        // Duración similar (±30 segundos): similitud baja
        int diferenciaDuracion = Math.abs(this.duracionSeg - otra.duracionSeg);
        if (diferenciaDuracion <= 30) {
            similitud += 10.0 * (1.0 - diferenciaDuracion / 30.0);
        }

        return similitud;
    }

    // -------- Igualdad y Hash basados en ID (RF-020) --------
    
    /**
     * Compara dos canciones por su ID único.
     * Cumple RF-020: equals() basado en id
     * 
     * @param o Objeto a comparar
     * @return true si tienen el mismo ID
     */
    @Override 
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cancion)) return false;
        Cancion cancion = (Cancion) o;
        return id.equals(cancion.id);
    }
    
    /**
     * Calcula el hash basado en el ID único.
     * Cumple RF-020: hashCode() basado en id
     * 
     * @return Hash del ID
     */
    @Override 
    public int hashCode() { 
        return id.hashCode(); 
    }

    /**
     * Representación en String de la canción.
     * 
     * @return String con información de la canción
     */
    @Override
    public String toString() {
        return "Cancion{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", artista='" + artista + '\'' +
                ", genero='" + genero + '\'' +
                ", anio=" + anio +
                ", duracion=" + getDuracionFormateada() +
                '}';
    }
}

