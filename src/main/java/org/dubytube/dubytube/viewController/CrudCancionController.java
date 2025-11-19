package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.CancionIndice;
import org.dubytube.dubytube.services.Session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CrudCancionController {

    // === Tabla y columnas (mantengo tus fx:id) ===
    @FXML private TableView<Cancion> tbl;
    @FXML private TableColumn<Cancion,String>  colId, colTitulo, colArtista, colGenero, colAudio;
    @FXML private TableColumn<Cancion,Integer> colAnio, colDuracion;

    // === Campos de formulario (mantengo tus fx:id) ===
    @FXML private TextField txtId, txtTitulo, txtArtista, txtGenero, txtAnio, txtDuracion;
    @FXML private TextField txtArchivoAudio;
    @FXML private Button btnSeleccionarAudio;

    // Repos/índice COMPARTIDOS (una sola fuente de verdad)
    private final CancionRepo repo        = AppContext.canciones();
    private final CancionIndice indice    = AppContext.indice();
    
    // Archivo de audio seleccionado temporalmente
    private File archivoAudioSeleccionado = null;

    @FXML
    public void initialize() {
        // Enlaces de propiedades -> columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        colDuracion.setCellValueFactory(new PropertyValueFactory<>("duracionSeg"));
        
        // Columna de audio: muestra ✓ si tiene audio, ✗ si no
        colAudio.setCellValueFactory(cellData -> {
            Cancion c = cellData.getValue();
            String simbolo = (c.getArchivoAudio() != null && !c.getArchivoAudio().isEmpty()) ? "✓" : "✗";
            return new javafx.beans.property.SimpleStringProperty(simbolo);
        });

        refrescarTabla();

        // Rellenar los campos al seleccionar
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel == null) {
                limpiarFormulario();
                return;
            }
            txtId.setText(sel.getId());
            txtTitulo.setText(sel.getTitulo());
            txtArtista.setText(sel.getArtista());
            txtGenero.setText(sel.getGenero());
            txtAnio.setText(String.valueOf(sel.getAnio()));
            txtDuracion.setText(String.valueOf(sel.getDuracionSeg()));
            
            // Mostrar archivo de audio si existe
            if (sel.getArchivoAudio() != null && !sel.getArchivoAudio().isEmpty()) {
                txtArchivoAudio.setText(sel.getArchivoAudio());
            } else {
                txtArchivoAudio.setText("");
            }
            archivoAudioSeleccionado = null; // Reset archivo temporal
        });
    }

    private void refrescarTabla() {
        tbl.setItems(FXCollections.observableArrayList(repo.findAll()));
        // Mantener el trie sincronizado para que Buscar/Avanzada vean los cambios
        indice.indexarExistentes();
    }

    private void limpiarFormulario() {
        txtId.clear(); 
        txtTitulo.clear(); 
        txtArtista.clear(); 
        txtGenero.clear(); 
        txtAnio.clear(); 
        txtDuracion.clear();
        txtArchivoAudio.clear();
        archivoAudioSeleccionado = null;
    }

    @FXML private void onNuevo() {
        tbl.getSelectionModel().clearSelection();
        limpiarFormulario();
        txtId.setPromptText("Se generará automáticamente al guardar");
        txtDuracion.setPromptText("Se detectará del archivo de audio");
        txtTitulo.requestFocus();
    }
    
    @FXML
    private void onSeleccionarAudio() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de audio");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos de Audio", "*.mp3", "*.wav", "*.m4a", "*.flac", "*.ogg"),
            new FileChooser.ExtensionFilter("MP3", "*.mp3"),
            new FileChooser.ExtensionFilter("WAV", "*.wav"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        
        File archivo = fileChooser.showOpenDialog(btnSeleccionarAudio.getScene().getWindow());
        
        if (archivo != null) {
            archivoAudioSeleccionado = archivo;
            txtArchivoAudio.setText(archivo.getName());
            
            // Extraer duración automáticamente del archivo de audio
            try {
                int duracion = obtenerDuracionAudio(archivo);
                txtDuracion.setText(String.valueOf(duracion));
            } catch (Exception e) {
                System.err.println("No se pudo obtener la duración del audio: " + e.getMessage());
                txtDuracion.setText("180"); // Valor por defecto si falla
            }
        }
    }

    @FXML private void onGuardar() {
        try {
            String titulo = safe(txtTitulo.getText());
            String artista = safe(txtArtista.getText());
            String genero = safe(txtGenero.getText());
            String sAnio = safe(txtAnio.getText());
            String sDur  = safe(txtDuracion.getText());

            if (titulo.isEmpty() || artista.isEmpty() || genero.isEmpty() || sAnio.isEmpty()) {
                alertError("Completa todos los campos obligatorios (*).");
                return;
            }
            
            int anio = Integer.parseInt(sAnio);
            
            // La duración se obtiene del audio, si no hay, usar valor por defecto
            int dur = 180; // Valor por defecto
            if (!sDur.isEmpty()) {
                dur = Integer.parseInt(sDur);
            }

            // Genera ID automáticamente
            Cancion seleccionada = tbl.getSelectionModel().getSelectedItem();
            String id;
            if (seleccionada != null) {
                // Si estamos editando, mantener el ID existente
                id = seleccionada.getId();
            } else {
                // Nueva canción, generar ID automáticamente
                id = UUID.randomUUID().toString();
            }

            boolean cambiaId = false; // Ya no permitimos cambiar IDs

            // Si cambió el ID y ya existe otro registro con ese ID -> error
            if (cambiaId && repo.find(id).isPresent()) {
                alertError("Ya existe una canción con ID: " + id);
                return;
            }

            Cancion nueva = new Cancion(id, titulo, artista, genero, anio, dur);
            
            // Procesar archivo de audio si se seleccionó uno nuevo
            if (archivoAudioSeleccionado != null) {
                String nombreArchivo = copiarArchivoAudio(archivoAudioSeleccionado, id);
                nueva.setArchivoAudio(nombreArchivo);
            } else if (seleccionada != null && seleccionada.getArchivoAudio() != null) {
                // Mantener el archivo de audio existente si no se seleccionó uno nuevo
                nueva.setArchivoAudio(seleccionada.getArchivoAudio());
            }
            
            // Registrar quién subió la canción
            if (Session.isLogged()) {
                nueva.setSubidaPor(Session.get().getUsername());
            }

            if (seleccionada != null) {
                // Actualiza: si cambia el ID, elimina la vieja; luego guarda la nueva
                if (cambiaId) repo.delete(seleccionada.getId());
            }
            repo.save(nueva);

            // Índice de títulos y grafo de similitud
            AppContext.indice().registrarCancion(nueva);
            conectarSimilitudesHeuristica(nueva);

            refrescarTabla();
            seleccionarEnTabla(id);
            archivoAudioSeleccionado = null; // Limpiar archivo temporal
        } catch (NumberFormatException e) {
            alertError("Año y duración deben ser números enteros.");
        } catch (IOException e) {
            e.printStackTrace();
            alertError("Error al copiar el archivo de audio: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            alertError("No se pudo guardar.");
        }
    }

    @FXML private void onEliminar() {
        Cancion sel = tbl.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alertError("Selecciona una canción.");
            return;
        }
        
        // Verificar permisos: solo el creador o admin pueden eliminar
        if (Session.isLogged()) {
            var usuario = Session.get();
            boolean esCreador = sel.getSubidaPor() != null && sel.getSubidaPor().equals(usuario.getUsername());
            boolean esAdmin = usuario.getRole() == org.dubytube.dubytube.domain.Role.ADMIN;
            
            if (!esCreador && !esAdmin) {
                alertError("Solo el creador de la canción o un administrador pueden eliminarla.");
                return;
            }
        }
        
        if (confirm("¿Eliminar \"" + sel.getTitulo() + "\"?")) {
            // Eliminar archivo de audio si existe
            if (sel.getArchivoAudio() != null && !sel.getArchivoAudio().isEmpty()) {
                eliminarArchivoAudio(sel.getArchivoAudio());
            }
            
            repo.delete(sel.getId());
            // (Opcional) TODO: remover aristas del grafo si implementas un método específico
            refrescarTabla();
            limpiarFormulario();
        }
    }

    @FXML private void onVolver() {
        try {
            Stage st = (Stage) tbl.getScene().getWindow();
            var scene = new Scene(new FXMLLoader(HelloApplication.class.getResource("/view/MainView.fxml")).load(), 900, 600);
            var css = HelloApplication.class.getResource("/styles/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            st.setTitle("Dubytube");
            st.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            alertError("No se pudo volver al menú.");
        }
    }

    // ===== helpers =====
    private void seleccionarEnTabla(String id) {
        Optional<Cancion> x = repo.find(id);
        x.ifPresent(c -> {
            tbl.getSelectionModel().select(c);
            tbl.scrollTo(c);
        });
    }

    private String safe(String s){ return s == null ? "" : s.trim(); }

    private boolean confirm(String msg) {
        var a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private void alertError(String m) { new Alert(Alert.AlertType.ERROR, m).showAndWait(); }

    /**
     * Conecta la canción guardada con el resto aplicando una métrica simple:
     * +5 si coincide artista, +3 si coincide género, +2 si año dif <= 2, +1 si dif <=5.
     * Distancia = 10 - score (acotada a mínimo 0.5). Menor distancia = más similar.
     */
    private void conectarSimilitudesHeuristica(Cancion c) {
        var grafo = AppContext.similitud();
        var todas = AppContext.canciones().findAll().stream()
                .filter(o -> !o.getId().equals(c.getId()))
                .collect(Collectors.toList());

        for (var o : todas) {
            double score = 0;
            if (c.getArtista()!=null && o.getArtista()!=null &&
                    c.getArtista().equalsIgnoreCase(o.getArtista())) score += 5.0;

            if (c.getGenero()!=null && o.getGenero()!=null &&
                    c.getGenero().equalsIgnoreCase(o.getGenero()))   score += 3.0;

            int diff = Math.abs(c.getAnio() - o.getAnio());
            if (diff <= 2)      score += 2.0;
            else if (diff <= 5) score += 1.0;

            double distancia = Math.max(0.5, 10.0 - score);
            grafo.agregarSimilitud(c.getId(), o.getId(), distancia); // no dirigido (u<->v) dentro del grafo
        }
    }
    
    /**
     * Copia el archivo de audio al directorio del proyecto y retorna el nombre del archivo guardado.
     */
    private String copiarArchivoAudio(File archivoOrigen, String idCancion) throws IOException {
        // Crear directorio de audio si no existe
        Path directorioAudio = Paths.get("src/main/resources/audio");
        if (!Files.exists(directorioAudio)) {
            Files.createDirectories(directorioAudio);
        }
        
        // Obtener extensión del archivo original
        String nombreOriginal = archivoOrigen.getName();
        String extension = "";
        int i = nombreOriginal.lastIndexOf('.');
        if (i > 0) {
            extension = nombreOriginal.substring(i);
        }
        
        // Crear nombre único para el archivo (usando ID de la canción)
        String nombreArchivo = idCancion + extension;
        Path destino = directorioAudio.resolve(nombreArchivo);
        
        // Copiar archivo
        Files.copy(archivoOrigen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        
        return nombreArchivo;
    }
    
    /**
     * Elimina el archivo de audio del directorio de recursos.
     */
    private void eliminarArchivoAudio(String nombreArchivo) {
        try {
            Path archivoPath = Paths.get("src/main/resources/audio", nombreArchivo);
            if (Files.exists(archivoPath)) {
                Files.delete(archivoPath);
            }
        } catch (IOException e) {
            System.err.println("No se pudo eliminar el archivo de audio: " + e.getMessage());
            // No lanzar error para no interrumpir la eliminación de la canción
        }
    }
    
    /**
     * Obtiene la duración del archivo de audio en segundos usando JavaFX Media.
     */
    private int obtenerDuracionAudio(File archivo) throws Exception {
        try {
            // Crear Media desde el archivo
            javafx.scene.media.Media media = new javafx.scene.media.Media(archivo.toURI().toString());
            
            // Esperar a que se carguen los metadatos (con timeout)
            final int[] duracionSegundos = {180}; // Valor por defecto
            final boolean[] cargado = {false};
            
            media.durationProperty().addListener((obs, oldDuration, newDuration) -> {
                if (newDuration != null && !newDuration.isUnknown() && !newDuration.isIndefinite()) {
                    duracionSegundos[0] = (int) newDuration.toSeconds();
                    cargado[0] = true;
                }
            });
            
            // Esperar hasta 3 segundos para que se carguen los metadatos
            int intentos = 0;
            while (!cargado[0] && intentos < 30) {
                Thread.sleep(100);
                intentos++;
                
                // Verificar si ya está disponible sin listener
                if (media.getDuration() != null && 
                    !media.getDuration().isUnknown() && 
                    !media.getDuration().isIndefinite()) {
                    duracionSegundos[0] = (int) media.getDuration().toSeconds();
                    break;
                }
            }
            
            return duracionSegundos[0];
            
        } catch (Exception e) {
            System.err.println("Error al obtener duración: " + e.getMessage());
            throw e;
        }
    }
}
