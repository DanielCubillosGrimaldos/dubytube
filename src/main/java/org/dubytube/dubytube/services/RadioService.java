package org.dubytube.dubytube.services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.ds.ColaCircular;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de reproductor de audio tipo radio.
 * 
 * <p>Implementa un reproductor continuo que usa una cola circular para
 * reproducir canciones en loop infinito. Soporta controles básicos
 * (play, pause, next, previous) y modo shuffle.</p>
 * 
 * <p><b>Estructura de datos utilizada:</b> ColaCircular<Cancion></p>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class RadioService {
    
    private ColaCircular<Cancion> playlist;
    private MediaPlayer mediaPlayer;
    private Cancion cancionActual;
    private boolean shuffle;
    private boolean repeat;
    
    // Listeners
    private List<CancionChangeListener> cancionChangeListeners;
    private List<PlaybackStateListener> playbackStateListeners;

    public RadioService() {
        this.playlist = new ColaCircular<>(500); // Capacidad para 500 canciones
        this.cancionChangeListeners = new ArrayList<>();
        this.playbackStateListeners = new ArrayList<>();
        this.shuffle = false;
        this.repeat = true; // Por defecto, modo radio activado
        this.playlist.setModoRadio(true);
    }

    /**
     * Carga una lista de canciones en el reproductor.
     * 
     * @param canciones Lista de canciones a cargar
     */
    public void cargarPlaylist(List<Cancion> canciones) {
        playlist.clear();
        
        for (Cancion c : canciones) {
            if (c.getArchivoAudio() != null && !c.getArchivoAudio().isEmpty()) {
                playlist.enqueue(c);
            }
        }
        
        if (shuffle) {
            playlist.shuffle();
        }
        
        System.out.println("✓ Playlist cargada: " + playlist.size() + " canciones");
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
     * Avanza a la siguiente canción.
     */
    public void next() {
        if (playlist.isEmpty()) {
            return;
        }

        stop();
        cancionActual = playlist.next();
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
