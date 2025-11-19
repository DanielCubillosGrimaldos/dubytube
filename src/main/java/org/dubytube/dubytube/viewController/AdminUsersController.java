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
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.UsuarioRepo;

public class AdminUsersController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Role> cmbRole;
    
    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, Role> colRole;
    @FXML private TableColumn<Usuario, Integer> colFavoritos;
    @FXML private TableColumn<Usuario, Integer> colAmigos;
    
    private UsuarioRepo repo;
    
    @FXML
    public void initialize() {
        repo = AppContext.getUsuarioRepo();
        
        // Configurar ComboBox de roles
        cmbRole.setItems(FXCollections.observableArrayList(Role.values()));
        
        // Configurar columnas de la tabla
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        colFavoritos.setCellValueFactory(cellData -> {
            Usuario u = cellData.getValue();
            int count = (u.getFavoritos() != null) ? u.getFavoritos().size() : 0;
            return new javafx.beans.property.SimpleObjectProperty<>(count);
        });
        
        colAmigos.setCellValueFactory(cellData -> {
            // Placeholder para conteo de amigos (implementar cuando exista la funcionalidad)
            return new javafx.beans.property.SimpleObjectProperty<>(0);
        });
        
        // Evento de selección en la tabla
        tableUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarUsuarioEnFormulario(newVal);
            }
        });
        
        // Cargar datos
        refreshTable();
    }
    
    @FXML
    private void onCreate() {
        try {
            // Validaciones
            if (txtUsername.getText().isBlank()) {
                alertError("El nombre de usuario es obligatorio");
                return;
            }
            if (txtPassword.getText().isBlank()) {
                alertError("La contraseña es obligatoria");
                return;
            }
            if (txtNombre.getText().isBlank()) {
                alertError("El nombre es obligatorio");
                return;
            }
            if (cmbRole.getValue() == null) {
                alertError("Debes seleccionar un rol");
                return;
            }
            
            // Verificar si el usuario ya existe
            if (repo.exists(txtUsername.getText())) {
                alertError("Ya existe un usuario con ese nombre");
                return;
            }
            
            // Crear nuevo usuario
            Usuario nuevo = new Usuario(
                txtUsername.getText().trim(),
                txtPassword.getText(),
                txtNombre.getText().trim()
            );
            nuevo.setRole(cmbRole.getValue());
            
            repo.save(nuevo);
            alertInfo("Usuario creado exitosamente");
            refreshTable();
            onClear();
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al crear usuario: " + e.getMessage());
        }
    }
    
    @FXML
    private void onUpdate() {
        try {
            // Validar que haya un usuario seleccionado
            Usuario selected = tableUsuarios.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alertError("Selecciona un usuario de la tabla para actualizar");
                return;
            }
            
            // Validaciones
            if (txtNombre.getText().isBlank()) {
                alertError("El nombre es obligatorio");
                return;
            }
            if (cmbRole.getValue() == null) {
                alertError("Debes seleccionar un rol");
                return;
            }
            
            // Actualizar datos (username no se modifica)
            selected.setNombre(txtNombre.getText().trim());
            selected.setRole(cmbRole.getValue());
            
            // Actualizar contraseña solo si se ingresó una nueva
            if (!txtPassword.getText().isBlank()) {
                selected.setPassword(txtPassword.getText());
            }
            
            repo.save(selected);
            alertInfo("Usuario actualizado exitosamente");
            refreshTable();
            onClear();
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al actualizar usuario: " + e.getMessage());
        }
    }
    
    @FXML
    private void onDelete() {
        try {
            Usuario selected = tableUsuarios.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alertError("Selecciona un usuario de la tabla para eliminar");
                return;
            }
            
            // Confirmación
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText("¿Eliminar usuario?");
            confirm.setContentText("Se eliminará el usuario: " + selected.getUsername());
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                repo.delete(selected.getUsername());
                alertInfo("Usuario eliminado exitosamente");
                refreshTable();
                onClear();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al eliminar usuario: " + e.getMessage());
        }
    }
    
    @FXML
    private void onClear() {
        txtUsername.clear();
        txtPassword.clear();
        txtNombre.clear();
        cmbRole.setValue(null);
        tableUsuarios.getSelectionModel().clearSelection();
        txtUsername.setDisable(false); // Reactivar username para crear nuevos
    }
    
    @FXML
    private void onBack() {
        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();
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
        var usuarios = repo.findAll();
        tableUsuarios.setItems(FXCollections.observableArrayList(usuarios));
    }
    
    private void cargarUsuarioEnFormulario(Usuario u) {
        txtUsername.setText(u.getUsername());
        txtPassword.clear(); // No mostrar la contraseña por seguridad
        txtNombre.setText(u.getNombre());
        cmbRole.setValue(u.getRole());
        txtUsername.setDisable(true); // No permitir cambiar el username
    }
    
    private void alertInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
    
    private void alertError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
