package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.UsuarioRepo;
import org.dubytube.dubytube.services.AuthService;
import org.dubytube.dubytube.services.Session;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private final UsuarioRepo repo = new UsuarioRepo();
    private final AuthService auth = new AuthService(repo);

    @FXML
    public void initialize() {
        // Usuarios demo
        var admin = new Usuario("admin", "123", "Administrador");
        admin.setRole(Role.ADMIN);
        repo.save(admin);

        repo.save(new Usuario("daniel", "123", "Daniel")); // USER por defecto

        if (btnLogin != null) btnLogin.setDefaultButton(true);
    }

    @FXML
    private void onLogin() {
        String u = txtUser.getText() == null ? "" : txtUser.getText().trim();
        String p = txtPass.getText() == null ? "" : txtPass.getText();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Completa usuario y contraseña.");
            return;
        }

        var ok = auth.login(u, p);
        if (ok.isPresent()) {
            Session.set(ok.get());
            go("/view/MainView.fxml", "Dubytube");
        } else {
            showError("Usuario o contraseña incorrectos.");
        }
    }

    private void showError(String msg){
        if (lblError != null) lblError.setText(msg);
        else new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void go(String fxml, String title) {
        try {
            var url   = HelloApplication.class.getResource(fxml);
            var root  = new FXMLLoader(url).load();
            var scene = new Scene((Parent) root, 900, 600);

            // APLICAR CSS A LA ESCENA PRINCIPAL
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("/styles/app.css").toExternalForm()
            );

            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo cargar la vista destino.");
        }
    }
}
