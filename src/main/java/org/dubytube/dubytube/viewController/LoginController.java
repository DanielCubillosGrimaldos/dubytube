package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.UsuarioRepo;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    private final UsuarioRepo repo = new UsuarioRepo();

    @FXML
    private void onLogin() {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        repo.find(user).ifPresentOrElse(u -> {
            if (u.getPassword().equals(pass)) {
                show("Login correcto. Bienvenido " + u.getNombre());
            } else {
                show("Contraseña incorrecta.");
            }
        }, () -> show("El usuario no existe."));
    }

    @FXML
    private void onRegister() {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            show("Debe ingresar usuario y contraseña.");
            return;
        }

        Usuario nuevo = new Usuario(user, pass, user); // nombre = username (simples por ahora)

        boolean registrado = repo.register(nuevo);

        if (!registrado) {
            show("El usuario ya existe.");
            return;
        }

        show("Usuario registrado exitosamente.");

        // limpiar los campos
        txtUser.clear();
        txtPass.clear();
    }

    private void show(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.showAndWait();
    }
}
