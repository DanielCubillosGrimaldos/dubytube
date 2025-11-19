package org.dubytube.dubytube.services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.ds.ColaCircular;
import org.dubytube.dubytube.ds.GrafoSimilitud;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de reproductor de audio tipo radio con recomendaciones inteligentes.
 * 
 * <p>Implementa un reproductor continuo que usa una cola circular para
 * reproducir canciones. Utiliza GrafoSimilitud para recomendar canciones
 * basadas en similitud de género, artista y año. Si no encuentra canciones
 * similares, reproduce canciones aleatorias.</p>
 * 
 * <p><b>Estructuras de datos utilizadas:</b></p>
 * <ul>
 *   <li>ColaCircular: Para gestionar la playlist circular</li>
 *   <li>GrafoSimilitud: Para recomendaciones basadas en similitud</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 2.0 - Integración con GrafoSimilitud
 * @since 2025-11-18
 */
public class RadioService {
    
    private ColaCircular<Cancion> playlist;
    private MediaPlayer mediaPlayer;
    private Cancion cancionActual;
    private boolean shuffle;
    private boolean repeat;
    private boolean usarRecomendaciones; // Nuevo: activar/desactivar recomendaciones inteligentes
    
    private GrafoSimilitud grafoSimilitud; // Nuevo: grafo para similitud
    private Map<String, Cancion> cancionesDisponibles; // Nuevo: todas las canciones para recomendaciones
    private Set<String> cancionesReproducidas; // Nuevo: historial para evitar repetir inmediatamente
    
    // Listeners
    private List<CancionChangeListener> cancionChangeListeners;
    private List<PlaybackStateListener> playbackStateListeners;

    public RadioService() {
        this.playlist = new ColaCircular<>(500); // Capacidad para 500 canciones
        this.cancionChangeListeners = new ArrayList<>();
        this.playbackStateListeners = new ArrayList<>();
        this.shuffle = false;
        this.repeat = true; // Por defecto, modo radio activado
        this.usarRecomendaciones = true; // Por defecto, usar recomendaciones inteligentes
        this.playlist.setModoRadio(true);
        
        this.grafoSimilitud = new GrafoSimilitud();
        this.cancionesDisponibles = new HashMap<>();
        this.cancionesReproducidas = new LinkedHashSet<>(); // Mantiene orden de inserción
    }

    /**
     * Carga una lista de canciones en el reproductor y construye el grafo de similitud.
     * 
     * @param canciones Lista de canciones a cargar
     */
    public void cargarPlaylist(List<Cancion> canciones) {
        playlist.clear();
        cancionesDisponibles.clear();
        grafoSimilitud = new GrafoSimilitud(); // Reiniciar grafo
        cancionesReproducidas.clear();
        
        // Cargar canciones en playlist y mapa
        for (Cancion c : canciones) {
            if (c.getArchivoAudio() != null && !c.getArchivoAudio().isEmpty()) {
                playlist.enqueue(c);
                cancionesDisponibles.put(c.getId(), c);
                grafoSimilitud.agregarCancion(c.getId());
            }
        }
        
        // Construir aristas del grafo basadas en similitud
        construirGrafoSimilitud(new ArrayList<>(cancionesDisponibles.values()));
        
        if (shuffle) {
            playlist.shuffle();
        }
        
        System.out.println("✓ Playlist cargada: " + playlist.size() + " canciones");
        System.out.println("✓ Grafo de similitud construido con " + cancionesDisponibles.size() + " nodos");
    }
    
    /**
     * Construye las aristas del grafo calculando similitud entre todas las canciones.
     */
    private void construirGrafoSimilitud(List<Cancion> canciones) {
        for (int i = 0; i < canciones.size(); i++) {
            for (int j = i + 1; j < canciones.size(); j++) {
                Cancion a = canciones.get(i);
                Cancion b = canciones.get(j);
                double distancia = calcularDistancia(a, b);
                grafoSimilitud.agregarSimilitud(a.getId(), b.getId(), distancia);
            }
        }
    }
    
    /**
     * Calcula la distancia (disimilitud) entre dos canciones.
     * Menor distancia = mayor similitud.
     * 
     * Heurística:
     * - Mismo género: -0.4
     * - Mismo artista: -0.5
     * - Años cercanos: reduce distancia
     * 
     * @return Distancia entre 0.05 y ~1.4
     */
    private double calcularDistancia(Cancion a, Cancion b) {
        double d = 1.0;
        
        // Género igual = más similares
        if (a.getGenero() != null && b.getGenero() != null &&
                a.getGenero().equalsIgnoreCase(b.getGenero())) {
            d -= 0.4;
        }
        
        // Artista igual = muy similares
        if (a.getArtista() != null && b.getArtista() != null &&
                a.getArtista().equalsIgnoreCase(b.getArtista())) {
            d -= 0.5;
        }
        
        // Años cercanos = algo similares
        int diffAnios = Math.abs(a.getAnio() - b.getAnio());
        d += Math.min(diffAnios, 40) / 100.0; // +0.00 a +0.40
        
        // Distancia mínima
        if (d < 0.05) d = 0.05;
        
        return d;
    }

    /**
     * Agrega una canción a la playlist.
     * 
     * @param cancion Canción a agregar
     * @return true si se agregó exitosamente
     */
    public boolean agregarCancion(Cancion cancion) {
        if (cancion == null || cancion.getArchivoAudio() == null) {
            return false;
        }
        
        return playlist.enqueue(cancion);
    }

    /**
     * Reproduce la canción actual o inicia la playlist.
     */
    public void play() {
        if (playlist.isEmpty()) {
            System.out.println("⚠ Playlist vacía");
            return;
        }

        if (mediaPlayer != null && cancionActual != null) {
            // Si hay una canción pausada, reanudar
            mediaPlayer.play();
            notifyPlaybackState(PlaybackState.PLAYING);
            return;
        }

        // Si no hay canción actual, tomar la primera
        if (cancionActual == null) {
            cancionActual = playlist.peek();
        }

        reproducirCancion(cancionActual);
    }

    /**
     * Pausa la reproducción.
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            notifyPlaybackState(PlaybackState.PAUSED);
        }
    }

    /**
     * Detiene la reproducción.
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            notifyPlaybackState(PlaybackState.STOPPED);
        }
    }

    /**
     * Avanza a la siguiente canción con recomendaciones inteligentes.
     * 
     * Si usarRecomendaciones está activo:
     * 1. Busca canciones similares usando el grafo
     * 2. Si no encuentra, da una canción aleatoria
     * 
     * Si está desactivado:
     * - Avanza circularmente en la playlist
     */
    public void next() {
        if (playlist.isEmpty()) {
            return;
        }

        stop();
        
        if (usarRecomendaciones && cancionActual != null) {
            cancionActual = obtenerSiguienteConRecomendacion();
        } else {
            cancionActual = playlist.next();
        }
        
        reproducirCancion(cancionActual);
    }

    /**
     * Retrocede a la canción anterior.
     */
    public void previous() {
        if (playlist.isEmpty()) {
            return;
        }

        stop();
        cancionActual = playlist.previous();
        reproducirCancion(cancionActual);
    }
    
    /**
     * Obtiene la siguiente canción usando recomendaciones basadas en similitud.
     * 
     * Algoritmo:
     * 1. Usa el grafo para encontrar las 10 canciones más similares
     * 2. Filtra las que ya se reprodujeron recientemente
     * 3. Selecciona aleatoriamente entre las similares disponibles
     * 4. Si no hay similares disponibles, selecciona una aleatoria
     * 
     * @return Siguiente canción recomendada
     */
    private Cancion obtenerSiguienteConRecomendacion() {
        if (cancionActual == null) {
            return obtenerCancionAleatoria();
        }
        
        // Agregar canción actual al historial
        cancionesReproducidas.add(cancionActual.getId());
        
        // Mantener historial de máximo 20 canciones
        if (cancionesReproducidas.size() > 20) {
            Iterator<String> it = cancionesReproducidas.iterator();
            it.next();
            it.remove();
        }
        
        // Obtener canciones similares usando el grafo
        List<String> similares = grafoSimilitud.recomendarDesde(cancionActual.getId(), 10);
        
        if (similares.isEmpty()) {
            System.out.println("⚠ No se encontraron canciones similares, reproduciendo aleatoria");
            return obtenerCancionAleatoria();
        }
        
        // Filtrar canciones que no se reprodujeron recientemente
        List<String> disponibles = similares.stream()
                .filter(id -> !cancionesReproducidas.contains(id))
                .toList();
        
        if (disponibles.isEmpty()) {
            // Si todas las similares fueron reproducidas, resetear historial y usar similares
            System.out.println("✓ Todas las similares fueron reproducidas, reiniciando historial");
            cancionesReproducidas.clear();
            cancionesReproducidas.add(cancionActual.getId());
            disponibles = similares;
        }
        
        // Seleccionar aleatoriamente entre las disponibles
        Random random = new Random();
        String idSeleccionada = disponibles.get(random.nextInt(disponibles.size()));
        Cancion siguiente = cancionesDisponibles.get(idSeleccionada);
        
        System.out.println("✓ Recomendación: " + siguiente.getTitulo() + " (similar a " + cancionActual.getTitulo() + ")");
        
        return siguiente;
    }
    
    /**
     * Obtiene una canción aleatoria de las disponibles.
     */
    private Cancion obtenerCancionAleatoria() {
        if (cancionesDisponibles.isEmpty()) {
            return playlist.peek();
        }
        
        List<String> ids = new ArrayList<>(cancionesDisponibles.keySet());
        
        // Filtrar las que no se reprodujeron recientemente
        List<String> disponibles = ids.stream()
                .filter(id -> !cancionesReproducidas.contains(id))
                .toList();
        
        if (disponibles.isEmpty()) {
            disponibles = ids; // Si todas fueron reproducidas, usar todas
            cancionesReproducidas.clear();
        }
        
        Random random = new Random();
        String idAleatoria = disponibles.get(random.nextInt(disponibles.size()));
        Cancion aleatoria = cancionesDisponibles.get(idAleatoria);
        
        System.out.println("✓ Reproduciendo aleatoria: " + aleatoria.getTitulo());
        
        return aleatoria;
    }

    /**
     * Reproduce una canción específica.
     * 
     * @param cancion Canción a reproducir
     */
    private void reproducirCancion(Cancion cancion) {
        if (cancion == null || cancion.getArchivoAudio() == null) {
            System.err.println("⚠ Canción sin archivo de audio");
            return;
        }

        try {
            // Detener reproducción anterior
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            // Intentar cargar desde recursos primero (funciona en JAR y desarrollo)
            String audioPath = "/audio/" + cancion.getArchivoAudio();
            var audioUrl = getClass().getResource(audioPath);
            
            String mediaUrl;
            if (audioUrl != null) {
                // Archivo encontrado en recursos
                mediaUrl = audioUrl.toExternalForm();
                System.out.println("✓ Cargando desde recursos: " + audioPath);
            } else {
                // Fallback: intentar desde sistema de archivos
                File audioFile = new File("src/main/resources/audio/" + cancion.getArchivoAudio());
                if (!audioFile.exists()) {
                    System.err.println("⚠ Archivo no encontrado: " + cancion.getArchivoAudio());
                    System.err.println("   Ruta intentada: " + audioFile.getAbsolutePath());
                    // Intentar siguiente canción
                    if (playlist.size() > 1) {
                        next();
                    }
                    return;
                }
                mediaUrl = audioFile.toURI().toString();
                System.out.println("✓ Cargando desde filesystem: " + audioFile.getAbsolutePath());
            }

            Media media = new Media(mediaUrl);
            mediaPlayer = new MediaPlayer(media);
            
            // Establecer volumen inicial
            mediaPlayer.setVolume(0.5);

            // Configurar evento al terminar
            mediaPlayer.setOnEndOfMedia(() -> {
                System.out.println("⏭ Canción terminada, avanzando...");
                if (repeat && playlist.size() > 0) {
                    next(); // Avanzar automáticamente a la siguiente
                } else {
                    stop();
                }
            });

            // Configurar evento de error
            mediaPlayer.setOnError(() -> {
                Throwable error = mediaPlayer.getError();
                System.err.println("⚠ Error al reproducir: " + (error != null ? error.getMessage() : "Unknown error"));
                if (playlist.size() > 1) {
                    next(); // Intentar siguiente
                }
            });
            
            // Configurar evento de ready
            mediaPlayer.setOnReady(() -> {
                System.out.println("✓ Media lista para reproducir");
            });

            mediaPlayer.play();
            cancionActual = cancion;
            
            notifyCancionChange(cancion);
            notifyPlaybackState(PlaybackState.PLAYING);
            
            System.out.println("▶ Reproduciendo: " + cancion.getTitulo() + " - " + cancion.getArtista());

        } catch (Exception e) {
            System.err.println("⚠ Error al cargar audio: " + e.getMessage());
            e.printStackTrace();
            if (playlist.size() > 1) {
                next(); // Intentar siguiente
            }
        }
    }

    /**
     * Establece el volumen del reproductor.
     * 
     * @param volumen Volumen (0.0 a 1.0)
     */
    public void setVolumen(double volumen) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(Math.max(0.0, Math.min(1.0, volumen)));
        }
    }

    /**
     * Obtiene el volumen actual.
     * 
     * @return Volumen (0.0 a 1.0)
     */
    public double getVolumen() {
        return mediaPlayer != null ? mediaPlayer.getVolume() : 0.5;
    }

    /**
     * Busca una posición específica en la canción actual.
     * 
     * @param segundos Posición en segundos
     */
    public void seek(double segundos) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(segundos));
        }
    }

    /**
     * Obtiene la posición actual de reproducción.
     * 
     * @return Posición en segundos
     */
    public double getPosicionActual() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime().toSeconds();
        }
        return 0;
    }

    /**
     * Obtiene la duración total de la canción actual.
     * 
     * @return Duración en segundos
     */
    public double getDuracionTotal() {
        if (mediaPlayer != null) {
            return mediaPlayer.getTotalDuration().toSeconds();
        }
        return 0;
    }

    /**
     * Activa o desactiva el modo shuffle.
     * 
     * @param shuffle true para activar shuffle
     */
    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        if (shuffle) {
            playlist.shuffle();
        }
    }

    /**
     * Verifica si el shuffle está activo.
     * 
     * @return true si está activo
     */
    public boolean isShuffle() {
        return shuffle;
    }

    /**
     * Activa o desactiva el modo repeat (radio).
     * 
     * @param repeat true para repetir
     */
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        playlist.setModoRadio(repeat);
    }

    /**
     * Verifica si el repeat está activo.
     * 
     * @return true si está activo
     */
    public boolean isRepeat() {
        return repeat;
    }

    /**
     * Obtiene la canción actual.
     * 
     * @return Canción actual o null
     */
    public Cancion getCancionActual() {
        return cancionActual;
    }

    /**
     * Verifica si está reproduciendo.
     * 
     * @return true si está reproduciendo
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    /**
     * Verifica si la playlist está vacía.
     * 
     * @return true si está vacía
     */
    public boolean isPlaylistVacia() {
        return playlist.isEmpty();
    }

    /**
     * Obtiene el tamaño de la playlist.
     * 
     * @return Número de canciones
     */
    public int getPlaylistSize() {
        return playlist.size();
    }

    /**
     * Limpia la playlist y detiene la reproducción.
     */
    public void clear() {
        stop();
        playlist.clear();
        cancionActual = null;
        cancionesDisponibles.clear();
        cancionesReproducidas.clear();
    }
    
    /**
     * Activa o desactiva el sistema de recomendaciones inteligentes.
     * 
     * Si está activo: Usa GrafoSimilitud para recomendar canciones similares
     * Si está desactivo: Reproduce en orden circular/shuffle normal
     * 
     * @param activar true para activar recomendaciones
     */
    public void setUsarRecomendaciones(boolean activar) {
        this.usarRecomendaciones = activar;
        System.out.println(activar ? 
            "✓ Recomendaciones inteligentes ACTIVADAS (usa GrafoSimilitud)" : 
            "✓ Recomendaciones inteligentes DESACTIVADAS (modo circular)");
    }
    
    /**
     * Verifica si las recomendaciones inteligentes están activas.
     * 
     * @return true si están activas
     */
    public boolean isUsarRecomendaciones() {
        return usarRecomendaciones;
    }

    // -------- Listeners --------

    public void addCancionChangeListener(CancionChangeListener listener) {
        cancionChangeListeners.add(listener);
    }

    public void addPlaybackStateListener(PlaybackStateListener listener) {
        playbackStateListeners.add(listener);
    }

    private void notifyCancionChange(Cancion cancion) {
        for (CancionChangeListener listener : cancionChangeListeners) {
            listener.onCancionChanged(cancion);
        }
    }

    private void notifyPlaybackState(PlaybackState state) {
        for (PlaybackStateListener listener : playbackStateListeners) {
            listener.onPlaybackStateChanged(state);
        }
    }

    // -------- Interfaces de Listeners --------

    @FunctionalInterface
    public interface CancionChangeListener {
        void onCancionChanged(Cancion cancion);
    }

    @FunctionalInterface
    public interface PlaybackStateListener {
        void onPlaybackStateChanged(PlaybackState state);
    }

    public enum PlaybackState {
        PLAYING, PAUSED, STOPPED
    }
}
