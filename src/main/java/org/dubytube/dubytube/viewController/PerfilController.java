package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.services.ExportarServices;
import org.dubytube.dubytube.services.Session;

import java.io.File;
import java.nio.file.Path;

public class PerfilController {

    @FXML private Label lblInfo;
    @FXML private TableView<Cancion> tblFav;

    @FXML
    public void initialize(){
        Usuario u = Session.get();
        if (u == null) {
            lblInfo.setText("Sin sesión");
            return;
        }
        lblInfo.setText("Usuario: " + u.getUsername() + " | Nombre: " + u.getNombre());

        var cols = tblFav.getColumns();
        ((TableColumn<Cancion,String>)cols.get(0)).setCellValueFactory(c-> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        ((TableColumn<Cancion,String>)cols.get(1)).setCellValueFactory(c-> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitulo()));
        ((TableColumn<Cancion,String>)cols.get(2)).setCellValueFactory(c-> new javafx.beans.property.SimpleStringProperty(c.getValue().getArtista()));
        ((TableColumn<Cancion,String>)cols.get(3)).setCellValueFactory(c-> new javafx.beans.property.SimpleStringProperty(c.getValue().getGenero()));
        ((TableColumn<Cancion,Integer>)cols.get(4)).setCellValueFactory(c-> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAnio()).asObject());

        refrescar();
    }

    private void refrescar(){
        var u = Session.get();
        if (u != null) {
            tblFav.setItems(FXCollections.observableArrayList(u.getFavoritos()));
        }
    }

    @FXML
    private void onQuitar(){
        var u = Session.get();
        var sel = tblFav.getSelectionModel().getSelectedItem();
        if (u == null || sel == null) return;
        u.removeFavoritoById(sel.getId());
        refrescar();
    }

    @FXML
    private void onExportar(){
        try {
            var u = Session.get();
            if (u == null || u.getFavoritos().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "No hay favoritos para exportar.").showAndWait();
                return;
            }
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar favoritos como CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            fc.setInitialFileName("favoritos_" + u.getUsername() + ".csv");
            File archivo = fc.showSaveDialog(lblInfo.getScene().getWindow());
            if (archivo == null) return;
            ExportarServices.exportFavoritos(u, Path.of(archivo.getAbsolutePath()));
            new Alert(Alert.AlertType.INFORMATION, "Exportado en:\n" + archivo.getAbsolutePath()).showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error exportando.").showAndWait();
        }
    }
}
