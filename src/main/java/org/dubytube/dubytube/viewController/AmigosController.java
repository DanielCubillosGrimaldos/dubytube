package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.ds.GrafoSocial;
import org.dubytube.dubytube.repo.UsuarioRepo;
import org.dubytube.dubytube.services.Session;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar la red social de amigos.
 * 
 * <p>Funcionalidades:</p>
 * <ul>
 *   <li>Ver amigos directos de un usuario</li>
 *   <li>Ver sugerencias de amigos (amigos de amigos - BFS nivel 2)</li>
 *   <li>Agregar nuevas amistades</li>
 *   <li>Utiliza GrafoSocial de AppContext para persistencia</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 2.0 - Integrado con AppContext y usuarios reales
 * @since 2025-11-18
 */
public class AmigosController {

    @FXML private TextField txtUser;
    @FXML private TextField txtAmigo;
    @FXML private ListView<String> lstDirectos;
    @FXML private ListView<String> lstSugerencias;
    @FXML private VBox containerSeguidos;
    @FXML private Label lblMsg;

    private final GrafoSocial g = org.dubytube.dubytube.AppContext.social();
    private final UsuarioRepo usuarioRepo = org.dubytube.dubytube.AppContext.usuarios();
    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        // Obtener usuario actual
        usuarioActual = Session.get();
        
        // Sincronizar todos los usuarios del sistema con el grafo social
        for (Usuario usuario : usuarioRepo.findAll()) {
            g.agregarUsuario(usuario);
        }
        
        // Cargar lista de seguidos (usuarios que sigue el usuario actual)
        if (usuarioActual != null) {
            g.agregarUsuario(usuarioActual);
            cargarSeguidos();
        }
        
        setMsg("✓ Red social cargada con " + g.getUsuarios().size() + " usuarios");
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

    /**
     * Carga la lista de usuarios seguidos (amigos) del usuario actual.
     */
    private void cargarSeguidos() {
        if (containerSeguidos == null || usuarioActual == null) {
            return;
        }
        
        containerSeguidos.getChildren().clear();
        Set<Usuario> amigos = g.getAmigos(usuarioActual);
        
        if (amigos.isEmpty()) {
            Label empty = new Label("Aún no sigues a nadie. Explora usuarios para comenzar 🌍");
            empty.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 14px; -fx-padding: 20px;");
            containerSeguidos.getChildren().add(empty);
            return;
        }
        
        for (Usuario amigo : amigos) {
            HBox card = crearCardSeguido(amigo);
            containerSeguidos.getChildren().add(card);
        }
    }

    /**
     * Crea una card de usuario seguido con botón de dejar de seguir.
     */
    private HBox crearCardSeguido(Usuario usuario) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: #282828; -fx-padding: 12px 16px; " +
                     "-fx-background-radius: 8px; -fx-alignment: CENTER_LEFT;");

        // Ícono de usuario
        Label icono = new Label("👤");
        icono.setStyle("-fx-font-size: 24px;");

        // Información del usuario
        VBox info = new VBox(4);
        Label nombre = new Label(usuario.getNombre());
        nombre.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label username = new Label("@" + usuario.getUsername());
        username.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12px;");
        
        info.getChildren().addAll(nombre, username);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Botón de dejar de seguir
        Button btnDejarSeguir = new Button("✓ Siguiendo");
        btnDejarSeguir.setStyle("-fx-background-color: #535353; -fx-text-fill: white; " +
                               "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6px 20px; " +
                               "-fx-background-radius: 20px; -fx-cursor: hand;");
        
        // Efecto hover para mostrar "Dejar de seguir"
        btnDejarSeguir.setOnMouseEntered(e -> {
            btnDejarSeguir.setText("✗ Dejar de seguir");
            btnDejarSeguir.setStyle("-fx-background-color: #b91d1d; -fx-text-fill: white; " +
                                   "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6px 20px; " +
                                   "-fx-background-radius: 20px; -fx-cursor: hand;");
        });
        
        btnDejarSeguir.setOnMouseExited(e -> {
            btnDejarSeguir.setText("✓ Siguiendo");
            btnDejarSeguir.setStyle("-fx-background-color: #535353; -fx-text-fill: white; " +
                                   "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6px 20px; " +
                                   "-fx-background-radius: 20px; -fx-cursor: hand;");
        });
        
        btnDejarSeguir.setOnAction(e -> dejarDeSeguir(usuario));

        card.getChildren().addAll(icono, info, spacer, btnDejarSeguir);
        return card;
    }

    /**
     * Elimina la amistad entre el usuario actual y el usuario seleccionado.
     */
    private void dejarDeSeguir(Usuario usuario) {
        boolean exito = g.eliminarAmistad(usuarioActual, usuario);
        
        if (exito) {
            setMsg("✓ Ya no sigues a @" + usuario.getUsername());
            cargarSeguidos(); // Recargar lista
        } else {
            setMsg("⚠️ No se pudo dejar de seguir a " + usuario.getUsername());
        }
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
