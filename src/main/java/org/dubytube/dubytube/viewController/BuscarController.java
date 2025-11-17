package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.CancionIndice;
import org.dubytube.dubytube.services.Session;

import java.util.*;

import static org.dubytube.dubytube.AppContext.canciones;

public class BuscarController {

    @FXML private TextField txtPrefijo;
    @FXML private TableView<Cancion> tblResultados;

    // columnas (deben existir en el FXML con esos fx:id)
    @FXML private TableColumn<Cancion, String>  colTitulo;
    @FXML private TableColumn<Cancion, String>  colArtista;
    @FXML private TableColumn<Cancion, String>  colGenero;
    @FXML private TableColumn<Cancion, Integer> colAnio;

    private final CancionRepo repo = canciones();
    private final CancionIndice indice = new CancionIndice(repo);

    @FXML
    public void initialize() {
        // Enlazar propiedades -> columnas
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));

        // Datos de prueba (luego reemplazar por carga real)
        indice.indexarExistentes();

        // Columna de acción para favoritos
        addFavoritosButtonColumn();
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

        // 1) Primero: prefijo por título usando el trie
        LinkedHashMap<String, Cancion> orden = new LinkedHashMap<>();
        for (Cancion c : indice.sugerirPorTitulo(q, 50)) {
            orden.put(c.getId(), c);
        }

        // 2) Luego: contains (case-insensitive) sobre título, artista y género
        String qLower = q.toLowerCase(Locale.ROOT);
        for (Cancion c : repo.findAll()) {
            if ( contains(c.getTitulo(), qLower)
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

        colAcciones.setCellFactory(tv -> new TableCell<>() {
            private final Button btn = new Button("Añadir ♥");

            {
                btn.setOnAction(e -> {
                    Cancion c = getTableView().getItems().get(getIndex());
                    var u = Session.get();
                    if (u != null && c != null && u.addFavorito(c)) {
                        btn.setText("Añadida");
                        btn.setDisable(true);
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
                    btn.setDisable(ya);
                    setGraphic(btn);
                }
            }
        });

        tblResultados.getColumns().add(colAcciones);
    }

    // ---- Volver al feed ----
    @FXML
    private void onVolver() {
        try {
            Stage stage = (Stage) tblResultados.getScene().getWindow();
            var url = HelloApplication.class.getResource("/view/MainView.fxml");
            var scene = new Scene(new FXMLLoader(url).load(), 900, 600);
            stage.setTitle("Inicio");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            // Puedes mostrar un Alert si quieres
        }
    }
}
