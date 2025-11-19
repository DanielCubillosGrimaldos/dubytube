package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Genero;
import org.dubytube.dubytube.repo.GeneroRepo;

import java.util.UUID;

public class AdminGenerosController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    
    @FXML private TableView<Genero> tableGeneros;
    @FXML private TableColumn<Genero, String> colId;
    @FXML private TableColumn<Genero, String> colNombre;
    @FXML private TableColumn<Genero, String> colDescripcion;
    
    private GeneroRepo repo;
    private Genero generoSeleccionado;
    
    @FXML
    public void initialize() {
        repo = AppContext.getGeneroRepo();
        
        // Configurar columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        // Evento de selección en la tabla
        tableGeneros.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarGeneroEnFormulario(newVal);
            }
        });
        
        // Cargar datos
        refreshTable();
    }
    
    @FXML
    private void onCreate() {
        try {
            // Validaciones
            if (txtNombre.getText().isBlank()) {
                alertError("El nombre del género es obligatorio");
                return;
            }
            
            // Verificar si el género ya existe por nombre
            if (repo.findByNombre(txtNombre.getText().trim()).isPresent()) {
                alertError("Ya existe un género con ese nombre");
                return;
            }
            
            // Crear nuevo género con ID único
            String id = UUID.randomUUID().toString();
            Genero nuevo = new Genero(id, txtNombre.getText().trim(), txtDescripcion.getText().trim());
            
            repo.save(nuevo);
            alertInfo("Género creado exitosamente");
            refreshTable();
            onClear();
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al crear género: " + e.getMessage());
        }
    }
    
    @FXML
    private void onUpdate() {
        try {
            // Validar que haya un género seleccionado
            if (generoSeleccionado == null) {
                alertError("Selecciona un género de la tabla para actualizar");
                return;
            }
            
            // Validaciones
            if (txtNombre.getText().isBlank()) {
                alertError("El nombre del género es obligatorio");
                return;
            }
            
            // Verificar si otro género ya tiene ese nombre
            var existenteOpt = repo.findByNombre(txtNombre.getText().trim());
            if (existenteOpt.isPresent() && !existenteOpt.get().getId().equals(generoSeleccionado.getId())) {
                alertError("Ya existe otro género con ese nombre");
                return;
            }
            
            // Actualizar datos
            generoSeleccionado.setNombre(txtNombre.getText().trim());
            generoSeleccionado.setDescripcion(txtDescripcion.getText().trim());
            
            repo.save(generoSeleccionado);
            alertInfo("Género actualizado exitosamente");
            refreshTable();
            onClear();
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al actualizar género: " + e.getMessage());
        }
    }
    
    @FXML
    private void onDelete() {
        try {
            if (generoSeleccionado == null) {
                alertError("Selecciona un género de la tabla para eliminar");
                return;
            }
            
            // Confirmación
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText("¿Eliminar género?");
            confirm.setContentText("Se eliminará el género: " + generoSeleccionado.getNombre());
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                repo.delete(generoSeleccionado.getId());
                alertInfo("Género eliminado exitosamente");
                refreshTable();
                onClear();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al eliminar género: " + e.getMessage());
        }
    }
    
    @FXML
    private void onClear() {
        txtNombre.clear();
        txtDescripcion.clear();
        tableGeneros.getSelectionModel().clearSelection();
        generoSeleccionado = null;
    }
    
    @FXML
    private void onBack() {
        try {
            Stage stage = (Stage) txtNombre.getScene().getWindow();
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
            alertError("Error al volver al menú principal");
        }
    }
    
    private void refreshTable() {
        var generos = repo.findAll();
        tableGeneros.setItems(FXCollections.observableArrayList(generos));
    }
    
    private void cargarGeneroEnFormulario(Genero g) {
        generoSeleccionado = g;
        txtNombre.setText(g.getNombre());
        txtDescripcion.setText(g.getDescripcion());
    }
    
    private void alertInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
    
    private void alertError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
