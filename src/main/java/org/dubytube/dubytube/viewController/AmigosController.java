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
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.ds.GrafoSocial;
import org.dubytube.dubytube.repo.UsuarioRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AmigosController {

    @FXML private TextField txtUser;
    @FXML private TextField txtAmigo;
    @FXML private ListView<String> lstDirectos;
    @FXML private ListView<String> lstSugerencias;
    @FXML private Label lblMsg;

    private final GrafoSocial g = new GrafoSocial();
    private final UsuarioRepo usuarioRepo = new UsuarioRepo();

    @FXML
    public void initialize() {
        // Crear usuarios de prueba
        Usuario daniel = new Usuario("daniel", "1234", "Daniel García");
        Usuario admin = new Usuario("admin", "admin", "Administrador");
        Usuario ana = new Usuario("ana", "1234", "Ana López");
        Usuario luis = new Usuario("luis", "1234", "Luis Martínez");
        Usuario maria = new Usuario("maria", "1234", "María Fernández");
        Usuario sofia = new Usuario("sofia", "1234", "Sofía Rodríguez");
        Usuario carlos = new Usuario("carlos", "1234", "Carlos Pérez");

        // Configurar roles
        admin.setRole(Role.ADMIN);

        // Agregar al grafo
        g.agregarUsuario(daniel);
        g.agregarUsuario(admin);
        g.agregarUsuario(ana);
        g.agregarUsuario(luis);
        g.agregarUsuario(maria);
        g.agregarUsuario(sofia);
        g.agregarUsuario(carlos);

        // Crear amistades
        g.agregarAmistad(daniel, ana);
        g.agregarAmistad(ana, maria);
        g.agregarAmistad(maria, luis);
        g.agregarAmistad(daniel, admin);
        g.agregarAmistad(sofia, carlos);
    }

    @FXML
    private void onCargar() {
        String username = safe(txtUser.getText());
        if (username.isEmpty()) { 
            setMsg("Indica un usuario."); 
            return; 
        }

        // Buscar el usuario en el grafo o crear uno temporal
        Usuario usuario = buscarUsuario(username);
        if (usuario == null) {
            setMsg("Usuario no encontrado en el grafo.");
            return;
        }

        // Directos
        Set<Usuario> directos = g.getAmigos(usuario);
        List<String> directosNombres = directos.stream()
                .map(Usuario::getUsername)
                .sorted()
                .collect(Collectors.toList());
        lstDirectos.setItems(FXCollections.observableArrayList(directosNombres));

        // Amigos de amigos (distancia 2)
        Set<Usuario> sugerencias = g.encontrarAmigosDeAmigos(usuario);
        List<String> sugerenciasNombres = sugerencias.stream()
                .map(Usuario::getUsername)
                .sorted()
                .collect(Collectors.toList());
        lstSugerencias.setItems(FXCollections.observableArrayList(sugerenciasNombres));

        setMsg("Cargado: " + username + " | directos=" + directos.size() + ", sugerencias=" + sugerencias.size());
    }

    @FXML
    private void onAgregar() {
        String u = safe(txtUser.getText());
        String v = safe(txtAmigo.getText());
        
        if (u.isEmpty() || v.isEmpty()) { 
            setMsg("Completa usuario y amigo."); 
            return; 
        }

        Usuario usuario1 = buscarOCrearUsuario(u);
        Usuario usuario2 = buscarOCrearUsuario(v);

        g.agregarAmistad(usuario1, usuario2);

        onCargar(); // refresca listas
        txtAmigo.clear();
        setMsg("Ahora " + u + " y " + v + " son amigos.");
    }

    /**
     * Busca un usuario en el grafo por nombre de usuario.
     */
    private Usuario buscarUsuario(String username) {
        for (Usuario u : g.getUsuarios()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Busca un usuario en el grafo o lo crea si no existe.
     */
    private Usuario buscarOCrearUsuario(String username) {
        Usuario existente = buscarUsuario(username);
        if (existente != null) {
            return existente;
        }

        // Crear nuevo usuario
        Usuario nuevo = new Usuario(username, "default", "Usuario " + username);
        g.agregarUsuario(nuevo);
        return nuevo;
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
