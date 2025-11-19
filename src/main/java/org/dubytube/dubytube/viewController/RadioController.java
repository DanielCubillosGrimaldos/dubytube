package org.dubytube.dubytube.viewController;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.services.RadioService;

import java.util.List;

/**
 * Controlador para la vista del reproductor de radio.
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class RadioController {

    @FXML private Text txtTitulo;
    @FXML private Text txtArtista;
    @FXML private Text txtGenero;
    
    @FXML private Slider sliderProgreso;
    @FXML private Label lblTiempoActual;
    @FXML private Label lblTiempoTotal;
    
    @FXML private Button btnPlayPause;
    @FXML private Button btnShuffle;
    @FXML private Button btnRepeat;
    
    @FXML private Slider sliderVolumen;
    @FXML private Label lblVolumen;
    
    @FXML private ListView<String> listCanciones;
    @FXML private Label lblCantidadCanciones;
    @FXML private Label lblMensaje;

    private RadioService radioService;
    private Timeline timeline; // Para actualizar la UI periódicamente

    @FXML
    public void initialize() {
        radioService = AppContext.getRadioService();
        
        // Configurar listeners del reproductor
        configurarListeners();
        
        // Timeline para actualizar la barra de progreso
        timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> {
            actualizarProgreso();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        // Listener del slider de volumen
        sliderVolumen.valueProperty().addListener((obs, oldVal, newVal) -> {
            radioService.setVolumen(newVal.doubleValue());
            lblVolumen.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
        });
        
        // Cargar estado inicial
        actualizarInterfaz();
        
        // ⭐ CARGAR AUTOMÁTICAMENTE TODAS LAS CANCIONES AL INICIAR ⭐
        cargarCancionesAutomaticamente();
    }
    
    /**
     * Carga automáticamente todas las canciones disponibles al iniciar el radio.
     */
    private void cargarCancionesAutomaticamente() {
        try {
            System.out.println("🎵 Iniciando carga automática de canciones...");
            
            var todasCanciones = AppContext.getCancionRepo().findAll();
            System.out.println("🎵 Total canciones en repo: " + todasCanciones.size());
            
            List<Cancion> cancionesConAudio = todasCanciones.stream()
                    .filter(c -> {
                        boolean tieneAudio = c.getArchivoAudio() != null && !c.getArchivoAudio().isEmpty();
                        if (!tieneAudio) {
                            System.out.println("⚠ Canción sin audio: " + c.getTitulo());
                        }
                        return tieneAudio;
                    })
                    .toList();
            
            System.out.println("🎵 Canciones con audio: " + cancionesConAudio.size());
            
            if (cancionesConAudio.isEmpty()) {
                mostrarMensaje("⚠ No hay canciones con archivos de audio. Agrega canciones desde el CRUD primero.");
                System.out.println("⚠ No hay canciones con archivos de audio válidos");
                return;
            }
            
            radioService.cargarPlaylist(cancionesConAudio);
            System.out.println("✓ Playlist cargada en RadioService: " + radioService.getPlaylistSize() + " canciones");
            
            // Actualizar lista visual
            listCanciones.getItems().clear();
            for (Cancion c : cancionesConAudio) {
                listCanciones.getItems().add(
                    String.format("🎵 %s - %s (%s)", 
                        c.getTitulo(), c.getArtista(), c.getDuracionFormateada())
                );
            }
            
            lblCantidadCanciones.setText(cancionesConAudio.size() + " canciones cargadas");
            mostrarMensaje("✨ ¡Radio listo! " + cancionesConAudio.size() + " canciones disponibles. Presiona ▶ para comenzar.");
            
        } catch (Exception e) {
            System.err.println("⚠ Error al cargar canciones: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("⚠ Error al cargar canciones: " + e.getMessage());
        }
    }

    /**
     * Configura los listeners del servicio de radio.
     */
    private void configurarListeners() {
        radioService.addCancionChangeListener(cancion -> {
            Platform.runLater(() -> {
                actualizarInfoCancion(cancion);
            });
        });

        radioService.addPlaybackStateListener(state -> {
            Platform.runLater(() -> {
                switch (state) {
                    case PLAYING:
                        btnPlayPause.setText("⏸");
                        break;
                    case PAUSED:
                    case STOPPED:
                        btnPlayPause.setText("▶");
                        break;
                }
            });
        });
    }

    /**
     * Actualiza la información de la canción en la UI.
     */
    private void actualizarInfoCancion(Cancion cancion) {
        if (cancion != null) {
            txtTitulo.setText(cancion.getTitulo());
            txtArtista.setText(cancion.getArtista());
            txtGenero.setText("🎸 " + cancion.getGenero());
            lblTiempoTotal.setText(formatearTiempo(cancion.getDuracionSeg()));
        } else {
            txtTitulo.setText("Sin canción");
            txtArtista.setText("-");
            txtGenero.setText("-");
            lblTiempoTotal.setText("0:00");
        }
    }

    /**
     * Actualiza la barra de progreso y tiempo actual.
     */
    private void actualizarProgreso() {
        if (radioService.isPlaying()) {
            double posicion = radioService.getPosicionActual();
            double duracion = radioService.getDuracionTotal();
            
            if (duracion > 0) {
                sliderProgreso.setValue((posicion / duracion) * 100);
                lblTiempoActual.setText(formatearTiempo((int) posicion));
            }
        }
    }

    /**
     * Actualiza toda la interfaz.
     */
    private void actualizarInterfaz() {
        // Actualizar información de canción actual
        Cancion actual = radioService.getCancionActual();
        actualizarInfoCancion(actual);
        
        // Actualizar estados de los botones
        btnShuffle.setStyle(radioService.isShuffle() ? 
            "-fx-background-color: #1DB954; -fx-text-fill: white;" : "");
        btnRepeat.setStyle(radioService.isRepeat() ? 
            "-fx-background-color: #1DB954; -fx-text-fill: white;" : "");
        
        // Actualizar volumen
        sliderVolumen.setValue(radioService.getVolumen());
        lblVolumen.setText(String.format("%.0f%%", radioService.getVolumen() * 100));
        
        // Actualizar contadores
        lblCantidadCanciones.setText(radioService.getPlaylistSize() + " canciones");
    }

    @FXML
    private void onPlayPause() {
        System.out.println("🎵 onPlayPause - Playlist size: " + radioService.getPlaylistSize());
        System.out.println("🎵 onPlayPause - Is playing: " + radioService.isPlaying());
        
        if (radioService.getPlaylistSize() == 0) {
            mostrarMensaje("⚠ No hay canciones en la playlist. Usa el botón 'Recargar'");
            return;
        }
        
        if (radioService.isPlaying()) {
            radioService.pause();
            mostrarMensaje("⏸ Pausado");
        } else {
            radioService.play();
            mostrarMensaje("▶ Reproduciendo");
        }
    }

    @FXML
    private void onNext() {
        radioService.next();
        mostrarMensaje("⏭ Siguiente canción");
    }

    @FXML
    private void onPrevious() {
        radioService.previous();
        mostrarMensaje("⏮ Canción anterior");
    }

    @FXML
    private void onShuffle() {
        boolean nuevoEstado = !radioService.isShuffle();
        radioService.setShuffle(nuevoEstado);
        btnShuffle.setStyle(nuevoEstado ? 
            "-fx-background-color: #1DB954; -fx-text-fill: white;" : "");
        mostrarMensaje(nuevoEstado ? "🔀 Modo aleatorio activado" : "🔀 Modo aleatorio desactivado");
    }

    @FXML
    private void onRepeat() {
        boolean nuevoEstado = !radioService.isRepeat();
        radioService.setRepeat(nuevoEstado);
        btnRepeat.setStyle(nuevoEstado ? 
            "-fx-background-color: #1DB954; -fx-text-fill: white;" : "");
        mostrarMensaje(nuevoEstado ? "🔁 Repetición activada" : "🔁 Repetición desactivada");
    }

    @FXML
    private void onSeek() {
        double porcentaje = sliderProgreso.getValue();
        double duracion = radioService.getDuracionTotal();
        double nuevaPosicion = (porcentaje / 100.0) * duracion;
        radioService.seek(nuevaPosicion);
    }

    @FXML
    private void onCargarTodo() {
        try {
            System.out.println("🔃 Recargando playlist...");
            
            var todasCanciones = AppContext.getCancionRepo().findAll();
            List<Cancion> cancionesConAudio = todasCanciones.stream()
                    .filter(c -> c.getArchivoAudio() != null && !c.getArchivoAudio().isEmpty())
                    .toList();
            
            if (cancionesConAudio.isEmpty()) {
                mostrarMensaje("⚠ No hay canciones con archivos de audio");
                return;
            }
            
            radioService.cargarPlaylist(cancionesConAudio);
            System.out.println("✓ Recargadas " + cancionesConAudio.size() + " canciones");
            
            // Actualizar lista visual
            listCanciones.getItems().clear();
            for (Cancion c : cancionesConAudio) {
                listCanciones.getItems().add(
                    String.format("🎵 %s - %s (%s)", 
                        c.getTitulo(), c.getArtista(), c.getDuracionFormateada())
                );
            }
            
            lblCantidadCanciones.setText(cancionesConAudio.size() + " canciones");
            mostrarMensaje("✓ Playlist recargada: " + cancionesConAudio.size() + " canciones");
            
        } catch (Exception e) {
            System.err.println("⚠ Error al cargar playlist: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("⚠ Error al cargar playlist: " + e.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        try {
            // Detener el timeline
            if (timeline != null) {
                timeline.stop();
            }
            
            Stage stage = (Stage) txtTitulo.getScene().getWindow();
            var url = HelloApplication.class.getResource("/view/MainView.fxml");
            var scene = new Scene(new FXMLLoader(url).load(), 900, 600);
            scene.getStylesheets().add(
                HelloApplication.class.getResource("/styles/app.css").toExternalForm()
            );
            stage.setTitle("Inicio");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Formatea segundos a formato MM:SS.
     */
    private String formatearTiempo(int segundos) {
        int minutos = segundos / 60;
        int secs = segundos % 60;
        return String.format("%d:%02d", minutos, secs);
    }

    /**
     * Muestra un mensaje temporal en la UI.
     */
    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
        
        // Limpiar mensaje después de 3 segundos
        Timeline clearTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            lblMensaje.setText("");
        }));
        clearTimeline.play();
    }
}
