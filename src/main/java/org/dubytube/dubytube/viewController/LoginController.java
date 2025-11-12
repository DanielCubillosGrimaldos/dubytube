package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.UsuarioRepo;
import org.dubytube.dubytube.services.AuthService;
import org.dubytube.dubytube.services.Session;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Button btnLogin;           // opcional (por si quieres deshabilitarlo)
    @FXML private Label lblError;            // opcional: agrega un Label en la vista y dale este fx:id

    private final UsuarioRepo repo = new UsuarioRepo();
    private final AuthService auth = new AuthService(repo);

    @FXML
    public void initialize() {
        // Usuarios de demo para probar:
        repo.save(new Usuario("admin", "123", "Administrador")); // si manejas Role, marca admin en otra parte
        repo.save(new Usuario("daniel", "123", "Daniel"));

        // Que el botón "Login" se dispare con ENTER
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
            var loader = new FXMLLoader(HelloApplication.class.getResource(fxml));
            var scene  = new Scene(loader.load(), 900, 600);
            Stage stage = (Stage) txtUser.getScene().getWindow(); // toma la ventana actual
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("No se pudo cargar la vista destino.");
        }
    }
}
