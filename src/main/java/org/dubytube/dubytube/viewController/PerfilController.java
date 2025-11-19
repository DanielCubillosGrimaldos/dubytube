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
import org.dubytube.dubytube.services.ExportarServices;
import org.dubytube.dubytube.services.Session;

import java.io.IOException;
import java.nio.file.Path;

public class PerfilController {

    @FXML private TableView<Cancion> tblFav;
    @FXML private TableColumn<Cancion, String>  colTitulo;
    @FXML private TableColumn<Cancion, String>  colArtista;
    @FXML private TableColumn<Cancion, String>  colGenero;
    @FXML private TableColumn<Cancion, Integer> colAnio;
    @FXML private TableColumn<Cancion, Void>    colAccion;

    @FXML
    public void initialize() {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));

        var u = Session.get();
        if (u != null) {
            // Convertir MyLinkedList a una lista observable
            var favoritos = new java.util.ArrayList<Cancion>();
            for (Cancion c : u.getFavoritos()) {
                favoritos.add(c);
            }
            tblFav.setItems(FXCollections.observableArrayList(favoritos));
        }

        addRemoveButtonColumn();
    }

    private void addRemoveButtonColumn() {
        colAccion.setCellFactory(tv -> new TableCell<>() {
            private final Button btn = new Button("Quitar");

            {
                btn.setOnAction(e -> {
                    Cancion c = getTableView().getItems().get(getIndex());
                    var u = Session.get();
                    if (u != null && c != null && u.removeFavoritoById(c.getId())) {
                        getTableView().getItems().remove(c);
                        getTableView().refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    @FXML
    private void onExport() {
        try {
            var u = Session.get();
            if (u == null) return;
            ExportarServices.exportFavoritos(u, Path.of("favoritos.csv"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onVolver() {
        try {
            Stage stage = (Stage) tblFav.getScene().getWindow();
            Scene scene = HelloApplication.createScene("MainView.fxml", 900, 600);
            stage.setTitle("Inicio");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
