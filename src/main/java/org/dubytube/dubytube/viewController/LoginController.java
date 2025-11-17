package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.UsuarioRepo;
import org.dubytube.dubytube.services.Session;
import org.dubytube.dubytube.AppContext; // <- asegúrate que existe (contexto único)

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    // Usa el repo compartido del contexto (no crear 'new' para no perder datos entre escenas)
    private final UsuarioRepo repo = AppContext.usuarios();


    @FXML
    public void initialize() {
        // Crea usuarios de demo si no existen
        if (repo.find("admin").isEmpty()) {
            var admin = new Usuario("admin", "123", "Administrador");
            admin.setRole(Role.ADMIN);
            repo.register(admin);
        }
        if (repo.find("daniel").isEmpty()) {
            var dan = new Usuario("daniel", "123", "Daniel");
            repo.register(dan);
        }
    }

    @FXML
    private void onLogin() {
        String user = (txtUser.getText() == null) ? "" : txtUser.getText().trim();
        String pass = (txtPass.getText() == null) ? "" : txtPass.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            alertError("Debe ingresar usuario y contraseña.");
            return;
        }

        repo.find(user).ifPresentOrElse(u -> {
            if (u.getPassword() != null && u.getPassword().equals(pass)) {
                Session.set(u); // guarda el usuario en sesión
                go("/view/MainView.fxml", "Dubytube");
            } else {
                alertError("Contraseña incorrecta.");
            }
        }, () -> alertError("El usuario no existe."));
    }

    @FXML
    private void onRegister() {
        String user = (txtUser.getText() == null) ? "" : txtUser.getText().trim();
        String pass = (txtPass.getText() == null) ? "" : txtPass.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            alertError("Debe ingresar usuario y contraseña.");
            return;
        }

        if (repo.find(user).isPresent()) {
            alertError("El usuario ya existe.");
            return;
        }

        Usuario nuevo = new Usuario(user, pass, user); // nombre = username de forma simple
        boolean ok = repo.register(nuevo);
        if (!ok) {
            alertError("No fue posible registrar el usuario.");
            return;
        }

        alertInfo("Usuario registrado exitosamente.");
        txtUser.clear();
        txtPass.clear();
    }

    // ------------ Utilidades ------------
    private void go(String fxml, String title) {
        try {
            var url = HelloApplication.class.getResource(fxml);
            if (url == null) throw new IllegalStateException("FXML no encontrado: " + fxml);

            var root = new FXMLLoader(url).load();
            var scene = new Scene((Parent) root, 900, 600);

            var css = HelloApplication.class.getResource("/styles/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            alertError("No se pudo cargar la vista: " + fxml + "\n" + e.getMessage());
        }
    }

    private void alertInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    private void alertError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
