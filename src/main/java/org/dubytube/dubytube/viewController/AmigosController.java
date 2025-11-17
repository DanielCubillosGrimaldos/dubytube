package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.ds.GrafoSocial;

import java.util.List;
import java.util.Set;

public class AmigosController {

    @FXML private TextField txtUser;
    @FXML private TextField txtAmigo;
    @FXML private ListView<String> lstDirectos;
    @FXML private ListView<String> lstSugerencias;
    @FXML private Label lblMsg;

    private final GrafoSocial g = new GrafoSocial();

    @FXML
    public void initialize() {
        // Semilla mínima de usuarios y amistades para probar
        addUsuarios("daniel", "admin", "ana", "luis", "maria", "sofia", "carlos");
        g.amistad("daniel", "ana");
        g.amistad("ana", "maria");
        g.amistad("maria", "luis");
        g.amistad("daniel", "admin");
        g.amistad("sofia", "carlos");
        // Puedes ajustar libremente estas relaciones.
    }

    // Helpers
    private void addUsuarios(String... ids) {
        for (var id : ids) g.agregarUsuario(id);
    }

    @FXML
    private void onCargar() {
        var u = safe(txtUser.getText());
        if (u.isEmpty()) { setMsg("Indica un usuario."); return; }
        g.agregarUsuario(u); // asegura existencia

        // Directos
        Set<String> directos = g.amigosDe(u); // requiere método en GrafoSocial (abajo te muestro)
        lstDirectos.setItems(FXCollections.observableArrayList(directos));

        // Distancia 2
        List<String> sug = List.copyOf(g.sugerenciasAmigos(u));
        lstSugerencias.setItems(FXCollections.observableArrayList(sug));

        setMsg("Cargado: " + u + " | directos=" + directos.size() + ", sugerencias=" + sug.size());
    }

    @FXML
    private void onAgregar() {
        var u = safe(txtUser.getText());
        var v = safe(txtAmigo.getText());
        if (u.isEmpty() || v.isEmpty()) { setMsg("Completa usuario y amigo."); return; }

        g.agregarUsuario(u);
        g.agregarUsuario(v);
        g.amistad(u, v);

        onCargar(); // refresca listas
        txtAmigo.clear();
        setMsg("Ahora " + u + " y " + v + " son amigos.");
    }

    @FXML
    private void onVolver() {
        try {
            var stage = (Stage) txtUser.getScene().getWindow();
            var url   = HelloApplication.class.getResource("/view/MainView.fxml");
            var scene = new Scene(new FXMLLoader(url).load(), 900, 600);
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("/styles/app.css").toExternalForm()
            );
            stage.setTitle("Inicio");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
    private void setMsg(String m) { if (lblMsg != null) lblMsg.setText(m); }
}
