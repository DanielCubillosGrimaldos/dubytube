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
import org.dubytube.dubytube.services.CancionIndice;

import java.util.Optional;
import java.util.UUID;

public class CrudCancionController {

    // === Tabla y columnas (mantengo tus fx:id) ===
    @FXML private TableView<Cancion> tbl;
    @FXML private TableColumn<Cancion,String>  colId, colTitulo, colArtista, colGenero;
    @FXML private TableColumn<Cancion,Integer> colAnio, colDuracion;

    // === Campos de formulario (mantengo tus fx:id) ===
    @FXML private TextField txtId, txtTitulo, txtArtista, txtGenero, txtAnio, txtDuracion;

    // Usa el repositorio COMPARTIDO del contexto
    private final CancionRepo repo = AppContext.canciones();
    private final CancionIndice indice = new CancionIndice(repo);

    @FXML
    public void initialize() {
        // Enlaces de propiedades -> columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        colDuracion.setCellValueFactory(new PropertyValueFactory<>("duracionSeg"));

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
        });
    }

    private void refrescarTabla() {
        tbl.setItems(FXCollections.observableArrayList(repo.findAll()));
        indice.indexarExistentes();                        // <- para que Buscar/Avanzada vean los cambios
    }

    private void limpiarFormulario() {
        txtId.clear(); txtTitulo.clear(); txtArtista.clear(); txtGenero.clear(); txtAnio.clear(); txtDuracion.clear();
    }

    @FXML private void onNuevo() {
        tbl.getSelectionModel().clearSelection();
        limpiarFormulario();
        txtTitulo.requestFocus();
    }

    @FXML private void onGuardar() {
        try {
            String id = safe(txtId.getText());
            String titulo = safe(txtTitulo.getText());
            String artista = safe(txtArtista.getText());
            String genero = safe(txtGenero.getText());
            String sAnio = safe(txtAnio.getText());
            String sDur  = safe(txtDuracion.getText());

            if (titulo.isEmpty() || artista.isEmpty() || genero.isEmpty() || sAnio.isEmpty() || sDur.isEmpty()) {
                alertError("Completa todos los campos (ID puede quedar vacío).");
                return;
            }
            int anio = Integer.parseInt(sAnio);
            int dur  = Integer.parseInt(sDur);

            // Genera ID si está vacío
            if (id.isEmpty()) id = UUID.randomUUID().toString();

            Cancion seleccionada = tbl.getSelectionModel().getSelectedItem();
            boolean cambiaId = (seleccionada != null && !id.equals(seleccionada.getId()));

            // Si cambió el ID y ya existe otro registro con ese ID -> error
            if (cambiaId && repo.find(id).isPresent()) {
                alertError("Ya existe una canción con ID: " + id);
                return;
            }

            Cancion nueva = new Cancion(id, titulo, artista, genero, anio, dur);

            if (seleccionada != null) {
                // Actualiza: si cambia el ID, elimina la vieja; luego guarda la nueva
                if (cambiaId) repo.delete(seleccionada.getId());
            }
            repo.save(nueva);

            refrescarTabla();
            seleccionarEnTabla(id);
        } catch (NumberFormatException e) {
            alertError("Año y duración deben ser números enteros.");
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
        if (confirm("¿Eliminar \"" + sel.getTitulo() + "\"?")) {
            repo.delete(sel.getId());
            refrescarTabla();
            limpiarFormulario();
        }
    }

    @FXML private void onVolver() {
        try {
            Stage st = (Stage) tbl.getScene().getWindow();
            var scene = new Scene(new FXMLLoader(HelloApplication.class.getResource("/view/MainView.fxml")).load(), 900, 600);
            scene.getStylesheets().add(HelloApplication.class.getResource("/styles/app.css").toExternalForm());
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
}
