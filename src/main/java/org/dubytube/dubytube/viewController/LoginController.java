package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.UsuarioRepo;
import org.dubytube.dubytube.services.Session;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblError; // opcional (si lo tienes en el FXML)

    private final UsuarioRepo usuarios = AppContext.usuarios();
    private volatile boolean processing = false; // <- antirrebote

    @FXML
    public void initialize() {
        // Bootstrap catálogo/índice para que al entrar ya existan canciones
        AppContext.bootstrapIfEmpty();

        // Usuarios demo (solo si no existen)
        if (usuarios.find("admin").isEmpty()) {
            var a = new Usuario("admin","123","Administrador");
            a.setRole(Role.ADMIN);
            usuarios.register(a);
        }
        if (usuarios.find("daniel").isEmpty()) {
            usuarios.register(new Usuario("daniel","123","Daniel"));
        }
    }

    @FXML
    private void onLogin() {
        if (processing) return;     // evita doble disparo
        processing = true;
        try {
            String u = safe(txtUser.getText());
            String p = safe(txtPass.getText());

            if (u.isEmpty() || p.isEmpty()) { err("Completa usuario y contraseña."); return; }

            var ok = usuarios.find(u).filter(x -> p.equals(x.getPassword()));
            if (ok.isEmpty()) { err("Usuario o contraseña incorrectos."); return; }

            Session.set(ok.get());

            // Ir al feed
            var stage = (Stage) txtUser.getScene().getWindow();
            var loader = new FXMLLoader(HelloApplication.class.getResource("/view/MainView.fxml"));
            var scene  = new Scene(loader.load(), 900, 600);
            var css = HelloApplication.class.getResource("/styles/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setTitle("Dubytube");
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            err("No se pudo cargar la vista principal.");
        } finally {
            processing = false;
        }
    }

    // Si usas registro, puedes dejar tu implementación actual
    @FXML private void onRegister() {  if (processing) return;
        processing = true;

        try {
            String u = safe(txtUser.getText());
            String p = safe(txtPass.getText());

            // Validaciones básicas
            if (u.isEmpty() || p.isEmpty()) {
                err("Completa usuario y contraseña para registrarte.");
                return;
            }

            if (usuarios.exists(u)) {
                err("Ese usuario ya existe.");
                return;
            }

            // Crear nuevo usuario (uso el username como nombre para simplificar)
            Usuario nuevo = new Usuario(u, p, u);
            nuevo.setRole(Role.USER);

            boolean ok = usuarios.register(nuevo);
            if (!ok) {
                err("No se pudo registrar el usuario.");
                return;
            }

            // Opcional: iniciar sesión automáticamente
            Session.set(nuevo);

            // Ir al feed principal (igual que en onLogin)
            var stage  = (Stage) txtUser.getScene().getWindow();
            var loader = new FXMLLoader(HelloApplication.class.getResource("/view/MainView.fxml"));
            var scene  = new Scene(loader.load(), 900, 600);
            var css    = HelloApplication.class.getResource("/styles/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setTitle("Dubytube");
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            err("Error registrando usuario.");
        } finally {
            processing = false;
        } }

    private String safe(String s){ return s == null ? "" : s.trim(); }
    private void err(String m){
        if (lblError != null) lblError.setText(m);
        else new Alert(Alert.AlertType.ERROR, m).showAndWait();
    }
}
