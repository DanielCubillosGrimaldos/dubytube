package org.dubytube.dubytube.viewController;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.CancionIndice;
import org.dubytube.dubytube.services.Session;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador unificado de búsqueda tipo Spotify con:
 * - Autocompletado en tiempo real usando Trie
 * - Filtros avanzados (género, año)
 * - Funcionalidad de "Me Gusta" (favoritos)
 * - Búsqueda combinada por título, artista y género
 */
public class BuscarController {

    // === Search Components ===
    @FXML private TextField txtBusqueda;
    @FXML private ListView<String> listSugerencias;
    
    // === Advanced Filters ===
    @FXML private ComboBox<String> cmbGenero;
    @FXML private TextField txtAnioDesde;
    @FXML private TextField txtAnioHasta;
    @FXML private Label lblResultados;
    
    // === Results Table ===
    @FXML private TableView<Cancion> tblResultados;
    @FXML private TableColumn<Cancion, Void> colFavorito;
    @FXML private TableColumn<Cancion, String> colTitulo;
    @FXML private TableColumn<Cancion, String> colArtista;
    @FXML private TableColumn<Cancion, String> colGenero;
    @FXML private TableColumn<Cancion, Integer> colAnio;
    @FXML private TableColumn<Cancion, String> colDuracion;
    @FXML private TableColumn<Cancion, String> colAudio;
    
    // === Services ===
    private final CancionRepo repo = AppContext.getCancionRepo();
    private final CancionIndice indice = AppContext.indice();
    
    // === State ===
    private ObservableList<Cancion> todasLasCanciones;
    private ObservableList<Cancion> resultadosFiltrados;
    
    @FXML
    public void initialize() {
        // Cargar todas las canciones
        todasLasCanciones = FXCollections.observableArrayList(repo.findAll());
        resultadosFiltrados = FXCollections.observableArrayList(todasLasCanciones);
        
        // Asegurar que el Trie esté cargado
        indice.indexarExistentes();
        
        // Configurar tabla
        configurarTabla();
        
        // Configurar autocompletado en tiempo real
        configurarAutocompletado();
        
        // Cargar géneros en ComboBox
        cargarGeneros();
        
        // Mostrar todas las canciones inicialmente
        actualizarResultados(todasLasCanciones);
    }
    
    /**
     * Configura las columnas de la tabla y la columna de favoritos.
     */
    private void configurarTabla() {
        // Columnas normales
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        
        // Columna de duración formateada
        colDuracion.setCellValueFactory(cellData -> {
            int segundos = cellData.getValue().getDuracionSeg();
            int minutos = segundos / 60;
            int segs = segundos % 60;
            return new SimpleStringProperty(String.format("%d:%02d", minutos, segs));
        });
        
        // Columna de audio (indicador)
        colAudio.setCellValueFactory(cellData -> {
            Cancion c = cellData.getValue();
            String simbolo = (c.getArchivoAudio() != null && !c.getArchivoAudio().isEmpty()) ? "✓" : "✗";
            return new SimpleStringProperty(simbolo);
        });
        
        // Columna de favoritos con botón de "Me Gusta"
        configurarColumnaFavoritos();
    }
    
    /**
     * Configura la columna de favoritos con botones de "Me Gusta".
     */
    private void configurarColumnaFavoritos() {
        colFavorito.setCellFactory(param -> new TableCell<>() {
            private final Button btnFavorito = new Button();
            
            {
                btnFavorito.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 18px;");
                btnFavorito.setOnAction(event -> {
                    Cancion cancion = getTableView().getItems().get(getIndex());
                    toggleFavorito(cancion);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || !Session.isLogged()) {
                    setGraphic(null);
                } else {
                    Cancion cancion = getTableView().getItems().get(getIndex());
                    Usuario usuario = Session.get();
                    
                    boolean esFavorito = usuario.getFavoritos().contains(cancion);
                    btnFavorito.setText(esFavorito ? "❤️" : "🤍");
                    btnFavorito.setTooltip(new Tooltip(esFavorito ? "Quitar de favoritos" : "Añadir a favoritos"));
                    
                    setGraphic(btnFavorito);
                }
            }
        });
    }
    
    /**
     * Alterna el estado de favorito de una canción.
     */
    private void toggleFavorito(Cancion cancion) {
        if (!Session.isLogged()) {
            mostrarAlerta("Error", "Debes iniciar sesión para añadir favoritos", Alert.AlertType.ERROR);
            return;
        }
        
        Usuario usuario = Session.get();
        boolean esFavorito = usuario.getFavoritos().contains(cancion);
        
        if (esFavorito) {
            usuario.getFavoritos().remove(cancion);
            mostrarInfo("Canción eliminada de favoritos");
        } else {
            usuario.getFavoritos().add(cancion);
            mostrarInfo("Canción añadida a favoritos ❤️");
        }
        
        // Guardar cambios en el repositorio
        AppContext.getUsuarioRepo().save(usuario);
        
        // Refrescar tabla para actualizar el icono
        tblResultados.refresh();
    }
    
    /**
     * Configura el autocompletado en tiempo real usando el Trie.
     */
    private void configurarAutocompletado() {
        // Configurar lista de sugerencias
        listSugerencias.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String seleccion = listSugerencias.getSelectionModel().getSelectedItem();
                if (seleccion != null) {
                    txtBusqueda.setText(seleccion);
                    ocultarSugerencias();
                    buscarEnTiempoReal();
                }
            }
        });
        
        // Listener en el campo de búsqueda para autocompletado en tiempo real
        txtBusqueda.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                ocultarSugerencias();
                actualizarResultados(todasLasCanciones);
            } else {
                mostrarSugerencias(newValue.trim());
                buscarEnTiempoReal();
            }
        });
        
        // Ocultar sugerencias cuando pierde el foco
        txtBusqueda.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(200); // Delay para permitir clic en sugerencia
                        ocultarSugerencias();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        });
    }
    
    /**
     * Muestra sugerencias del Trie basadas en el prefijo ingresado.
     */
    private void mostrarSugerencias(String prefijo) {
        List<Cancion> sugerencias = indice.sugerirPorTitulo(prefijo, 10);
        
        if (sugerencias.isEmpty()) {
            ocultarSugerencias();
            return;
        }
        
        List<String> titulos = sugerencias.stream()
                .map(Cancion::getTitulo)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
        
        listSugerencias.setItems(FXCollections.observableArrayList(titulos));
        listSugerencias.setPrefHeight(Math.min(titulos.size() * 35, 300));
        listSugerencias.setVisible(true);
        listSugerencias.setManaged(true);
    }
    
    /**
     * Oculta la lista de sugerencias.
     */
    private void ocultarSugerencias() {
        listSugerencias.setVisible(false);
        listSugerencias.setManaged(false);
        listSugerencias.setPrefHeight(0);
    }
    
    /**
     * Realiza búsqueda en tiempo real mientras el usuario escribe.
     */
    private void buscarEnTiempoReal() {
        String query = txtBusqueda.getText();
        if (query == null || query.trim().isEmpty()) {
            actualizarResultados(todasLasCanciones);
            return;
        }
        
        query = query.trim().toLowerCase();
        
        // Búsqueda combinada usando Trie y búsqueda por coincidencia
        LinkedHashMap<String, Cancion> resultadosMap = new LinkedHashMap<>();
        
        // 1. Primero: sugerencias del Trie (por prefijo de título)
        for (Cancion c : indice.sugerirPorTitulo(query, 100)) {
            resultadosMap.put(c.getId(), c);
        }
        
        // 2. Segundo: búsqueda por coincidencia en título, artista y género
        for (Cancion c : todasLasCanciones) {
            if (contiene(c.getTitulo(), query) ||
                contiene(c.getArtista(), query) ||
                contiene(c.getGenero(), query)) {
                resultadosMap.putIfAbsent(c.getId(), c);
            }
        }
        
        actualizarResultados(new ArrayList<>(resultadosMap.values()));
    }
    
    /**
     * Aplica filtros avanzados (género y rango de años).
     */
    @FXML
    private void onAplicarFiltros() {
        String generoSeleccionado = cmbGenero.getValue();
        String anioDesdeStr = txtAnioDesde.getText();
        String anioHastaStr = txtAnioHasta.getText();
        
        List<Cancion> resultados = new ArrayList<>(todasLasCanciones);
        
        // Filtrar por género
        if (generoSeleccionado != null && !generoSeleccionado.equals("Todos")) {
            resultados = resultados.stream()
                    .filter(c -> c.getGenero() != null && c.getGenero().equalsIgnoreCase(generoSeleccionado))
                    .collect(Collectors.toList());
        }
        
        // Filtrar por rango de años
        try {
            Integer anioDesde = null;
            Integer anioHasta = null;
            
            if (anioDesdeStr != null && !anioDesdeStr.trim().isEmpty()) {
                anioDesde = Integer.parseInt(anioDesdeStr.trim());
            }
            
            if (anioHastaStr != null && !anioHastaStr.trim().isEmpty()) {
                anioHasta = Integer.parseInt(anioHastaStr.trim());
            }
            
            if (anioDesde != null || anioHasta != null) {
                final Integer desde = anioDesde;
                final Integer hasta = anioHasta;
                
                resultados = resultados.stream()
                        .filter(c -> {
                            int anio = c.getAnio();
                            boolean cumpleDesde = (desde == null || anio >= desde);
                            boolean cumpleHasta = (hasta == null || anio <= hasta);
                            return cumpleDesde && cumpleHasta;
                        })
                        .collect(Collectors.toList());
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un número válido", Alert.AlertType.ERROR);
            return;
        }
        
        // Aplicar búsqueda de texto si hay algo en el campo
        String query = txtBusqueda.getText();
        if (query != null && !query.trim().isEmpty()) {
            String queryLower = query.trim().toLowerCase();
            resultados = resultados.stream()
                    .filter(c -> 
                        contiene(c.getTitulo(), queryLower) ||
                        contiene(c.getArtista(), queryLower) ||
                        contiene(c.getGenero(), queryLower))
                    .collect(Collectors.toList());
        }
        
        actualizarResultados(resultados);
    }
    
    /**
     * Limpia todos los filtros y muestra todas las canciones.
     */
    @FXML
    private void onLimpiarFiltros() {
        cmbGenero.setValue("Todos");
        txtAnioDesde.clear();
        txtAnioHasta.clear();
        txtBusqueda.clear();
        actualizarResultados(todasLasCanciones);
    }
    

    
    /**
     * Vuelve al menú principal.
     */
    @FXML
    private void onVolver() {
        try {
            Stage stage = (Stage) txtBusqueda.getScene().getWindow();
            var url = HelloApplication.class.getResource("/view/MainView.fxml");
            var root = new FXMLLoader(url).load();
            var scene = new Scene((Parent) root, 1000, 650);
            scene.getStylesheets().add(
                HelloApplication.class.getResource("/styles/app.css").toExternalForm()
            );
            stage.setTitle("DubyTube - Menú Principal");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo volver al menú principal", Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Carga los géneros únicos en el ComboBox.
     */
    private void cargarGeneros() {
        Set<String> generosUnicos = todasLasCanciones.stream()
                .map(Cancion::getGenero)
                .filter(g -> g != null && !g.isEmpty())
                .collect(Collectors.toSet());
        
        List<String> generos = new ArrayList<>();
        generos.add("Todos");
        generos.addAll(generosUnicos.stream().sorted().collect(Collectors.toList()));
        
        cmbGenero.setItems(FXCollections.observableArrayList(generos));
        cmbGenero.setValue("Todos");
    }
    
    /**
     * Actualiza la tabla con los resultados y el contador.
     */
    private void actualizarResultados(List<Cancion> resultados) {
        resultadosFiltrados = FXCollections.observableArrayList(resultados);
        tblResultados.setItems(resultadosFiltrados);
        lblResultados.setText(resultados.size() + " resultado" + (resultados.size() != 1 ? "s" : ""));
    }
    
    /**
     * Verifica si un texto contiene una consulta (case-insensitive).
     */
    private boolean contiene(String texto, String query) {
        return texto != null && texto.toLowerCase().contains(query);
    }
    
    /**
     * Muestra una alerta informativa.
     */
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Muestra una alerta.
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
