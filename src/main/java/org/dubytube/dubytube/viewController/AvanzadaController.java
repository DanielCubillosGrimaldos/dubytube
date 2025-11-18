package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.BusquedaAvanzada;

import java.util.List;

public class AvanzadaController {

    @FXML private TextField txtArtista, txtGenero, txtAnioMin, txtAnioMax;
    @FXML private ChoiceBox<BusquedaAvanzada.Logica> choiceLogica;

    @FXML private TableView<Cancion> tblResultados;
    @FXML private TableColumn<Cancion, String>  colTitulo, colArtista, colGenero;
    @FXML private TableColumn<Cancion, Integer> colAnio;

    // Repo compartido + servicio concurrente
    private final CancionRepo repo = AppContext.canciones();
    private final BusquedaAvanzada servicio = new BusquedaAvanzada(repo);

    @FXML
    public void initialize() {
        // Garantiza datos de demo + índice listos al abrir
        AppContext.bootstrapIfEmpty();

        // Tabla
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));

        // AND/OR
        choiceLogica.getItems().setAll(BusquedaAvanzada.Logica.AND, BusquedaAvanzada.Logica.OR);
        choiceLogica.setValue(BusquedaAvanzada.Logica.AND);

        // Mostrar todas al entrar
        mostrarTodo();
    }

    @FXML
    private void onBuscar() {
        String artista = t(txtArtista);
        String genero  = t(txtGenero);
        Integer min    = parseIntOrNull(txtAnioMin.getText());
        Integer max    = parseIntOrNull(txtAnioMax.getText());
        var logica     = (choiceLogica.getValue() == null)
                ? BusquedaAvanzada.Logica.AND
                : choiceLogica.getValue();

        // Si no hay filtros, muestra todo
        if (artista.isEmpty() && genero.isEmpty() && min == null && max == null) {
            mostrarTodo();
            return;
        }

        List<Cancion> result = servicio.buscar(artista, genero, min, max, logica);
        tblResultados.setItems(FXCollections.observableArrayList(result));
        tblResultados.refresh();
    }

    @FXML
    private void onLimpiar() {
        txtArtista.clear();
        txtGenero.clear();
        txtAnioMin.clear();
        txtAnioMax.clear();
        choiceLogica.setValue(BusquedaAvanzada.Logica.AND);
        mostrarTodo();
    }

    private void mostrarTodo() {
        tblResultados.setItems(FXCollections.observableArrayList(repo.findAll()));
        tblResultados.refresh();
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private String t(TextField tf) {
        return (tf == null || tf.getText() == null) ? "" : tf.getText().trim();
    }

    @FXML
    private void onVolver() {
        try {
            Stage stage = (Stage) tblResultados.getScene().getWindow();
            var url = HelloApplication.class.getResource("/view/MainView.fxml");
            var scene = new Scene(new FXMLLoader(url).load(), 900, 600);
            var css = HelloApplication.class.getResource("/styles/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setTitle("Dubytube");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo volver al menú.").showAndWait();
        }
    }
}
