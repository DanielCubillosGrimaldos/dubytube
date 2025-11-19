package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.CancionIndice;
import org.dubytube.dubytube.services.Session;
import org.dubytube.dubytube.AppContext;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

import static org.dubytube.dubytube.AppContext.canciones;

public class BuscarController {

    @FXML private TextField txtPrefijo;
    @FXML private TableView<Cancion> tblResultados;

    // columnas (deben existir en el FXML con esos fx:id)
    @FXML private TableColumn<Cancion, String>  colTitulo;
    @FXML private TableColumn<Cancion, String>  colArtista;
    @FXML private TableColumn<Cancion, String>  colGenero;
    @FXML private TableColumn<Cancion, Integer> colAnio;

    // Repos/índice compartidos (una sola fuente de verdad)
    private final CancionRepo repo     = canciones();
    private final CancionIndice indice = AppContext.indice();

    @FXML
    public void initialize() {
        // Enlazar propiedades -> columnas
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));

        // Asegurar que el trie esté cargado (idempotente)
        indice.indexarExistentes();

        // Columna de acción para favoritos
        addFavoritosButtonColumn();

        // Mostrar todo al abrir (opcional)
        tblResultados.setItems(FXCollections.observableArrayList(repo.findAll()));
        tblResultados.refresh();
    }

    @FXML
    private void onBuscar() {
        String q = txtPrefijo.getText();
        if (q == null) q = "";
        q = q.trim();

        // Si está vacío -> muestro todo
        if (q.isEmpty()) {
            tblResultados.setItems(FXCollections.observableArrayList(repo.findAll()));
            tblResultados.refresh();
            return;
        }

        // 1) Prefijo por título usando el trie (conserva orden)
        LinkedHashMap<String, Cancion> orden = new LinkedHashMap<>();
        for (Cancion c : indice.sugerirPorTitulo(q, 50)) {
            orden.put(c.getId(), c);
        }

        // 2) Luego: contains (case-insensitive) sobre título, artista y género
        String qLower = q.toLowerCase(Locale.ROOT);
        for (Cancion c : repo.findAll()) {
            if (contains(c.getTitulo(), qLower)
                    || contains(c.getArtista(), qLower)
                    || contains(c.getGenero(), qLower)) {
                orden.putIfAbsent(c.getId(), c);
            }
        }

        tblResultados.setItems(FXCollections.observableArrayList(orden.values()));
        tblResultados.refresh();
    }

    private boolean contains(String s, String qLower){
        return s != null && s.toLowerCase(Locale.ROOT).contains(qLower);
    }

    // ---- Columna "Añadir ♥" ----
    private void addFavoritosButtonColumn() {
        TableColumn<Cancion, Void> colAcciones = new TableColumn<>("Acción");
        colAcciones.setPrefWidth(120);

        colAcciones.setCellFactory(tv -> new TableCell<Cancion, Void>() {
            private final Button btn = new Button("Añadir ♥");

            {
                btn.setOnAction(e -> {
                    Cancion c = getTableView().getItems().get(getIndex());
                    var u = Session.get();
                    if (u != null && c != null && u.addFavorito(c)) {
                        btn.setText("Añadida");
                        btn.setDisable(true);
                    } else if (u == null) {
                        new Alert(Alert.AlertType.INFORMATION, "Inicia sesión para añadir favoritos.").showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Cancion c = getTableView().getItems().get(getIndex());
                    var u = Session.get();
                    boolean ya = (u != null && c != null && u.hasFavorito(c.getId()));
                    btn.setText(ya ? "Añadida" : "Añadir ♥");
                    btn.setDisable(ya || u == null);
                    setGraphic(btn);
                }
            }
        });

        // Evitar duplicarla si ya existe (por recargas)
        boolean existe = tblResultados.getColumns().stream()
                .anyMatch(tc -> Objects.equals(tc.getText(), "Acción"));
        if (!existe) {
            tblResultados.getColumns().add(colAcciones);
        }
    }

    // ---- Volver al feed ----
    @FXML
    private void onVolver() {
        try {
            Stage stage = (Stage) tblResultados.getScene().getWindow();
            var url = HelloApplication.class.getResource("/view/MainView.fxml");
            var root = new FXMLLoader(url).load();
            var scene = new Scene((Parent) root, 900, 600);
            var css = HelloApplication.class.getResource("/styles/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setTitle("Inicio");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
