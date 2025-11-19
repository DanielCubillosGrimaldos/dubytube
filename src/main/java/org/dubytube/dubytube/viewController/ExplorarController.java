package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.ds.GrafoSocial;
import org.dubytube.dubytube.services.Session;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para explorar usuarios nuevos en la red social.
 * 
 * <p>Funcionalidades:</p>
 * <ul>
 *   <li>Mostrar usuarios aleatorios que NO son amigos del usuario actual</li>
 *   <li>Permitir seguir/agregar como amigo</li>
 *   <li>Mostrar sugerencias basadas en amigos de amigos (GrafoSocial BFS)</li>
 *   <li>Refrescar lista de usuarios</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class ExplorarController {

    @FXML private VBox containerUsuarios;
    @FXML private Label lblSugerencias;
    @FXML private Label lblAleatorios;
    @FXML private VBox containerSugerencias;
    @FXML private VBox containerAleatorios;
    @FXML private Label lblMensaje;

    private final GrafoSocial grafo = AppContext.social();
    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        usuarioActual = Session.get();
        
        if (usuarioActual == null) {
            mostrarMensaje("⚠️ Debes iniciar sesión para explorar usuarios", "error");
            return;
        }

        // Asegurar que el usuario está en el grafo
        grafo.agregarUsuario(usuarioActual);
        
        cargarUsuarios();
    }

    /**
     * Carga usuarios sugeridos (amigos de amigos) y aleatorios.
     */
    private void cargarUsuarios() {
        containerSugerencias.getChildren().clear();
        containerAleatorios.getChildren().clear();

        // 1. Sugerencias: Amigos de amigos (BFS nivel 2)
        Set<Usuario> sugerencias = grafo.encontrarAmigosDeAmigos(usuarioActual);
        lblSugerencias.setText("SUGERENCIAS PARA TI (" + sugerencias.size() + ")");
        
        if (sugerencias.isEmpty()) {
            Label noSugerencias = new Label("No hay sugerencias disponibles");
            noSugerencias.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 14px;");
            containerSugerencias.getChildren().add(noSugerencias);
        } else {
            for (Usuario sugerido : sugerencias.stream().limit(5).collect(Collectors.toList())) {
                containerSugerencias.getChildren().add(crearCardUsuario(sugerido, true));
            }
        }

        // 2. Usuarios aleatorios: Todos los usuarios que NO son amigos
        Set<Usuario> amigos = grafo.getAmigos(usuarioActual);
        List<Usuario> noAmigos = AppContext.usuarios().findAll().stream()
                .filter(u -> !u.equals(usuarioActual)) // No incluir al usuario actual
                .filter(u -> !amigos.contains(u)) // No incluir amigos actuales
                .filter(u -> !sugerencias.contains(u)) // No duplicar con sugerencias
                .collect(Collectors.toList());

        // Mezclar aleatoriamente
        Collections.shuffle(noAmigos);
        
        lblAleatorios.setText("DESCUBRE PERSONAS (" + noAmigos.size() + ")");
        
        if (noAmigos.isEmpty()) {
            Label noAleatorios = new Label("No hay más usuarios disponibles");
            noAleatorios.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 14px;");
            containerAleatorios.getChildren().add(noAleatorios);
        } else {
            for (Usuario usuario : noAmigos.stream().limit(10).collect(Collectors.toList())) {
                containerAleatorios.getChildren().add(crearCardUsuario(usuario, false));
            }
        }

        mostrarMensaje("✓ Usuarios cargados", "success");
    }

    /**
     * Crea una card de usuario con información y botón de seguir/dejar de seguir.
     */
    private HBox crearCardUsuario(Usuario usuario, boolean esSugerencia) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: #282828; -fx-padding: 15px 20px; " +
                     "-fx-background-radius: 8px; -fx-alignment: CENTER_LEFT;");

        // Ícono de usuario
        Label icono = new Label(esSugerencia ? "👥" : "👤");
        icono.setStyle("-fx-font-size: 28px;");

        // Información del usuario
        VBox info = new VBox(4);
        Label nombre = new Label(usuario.getNombre());
        nombre.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        
        Label username = new Label("@" + usuario.getUsername());
        username.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
        
        // Mostrar conexión mutua si es sugerencia
        if (esSugerencia) {
            Set<Usuario> misAmigos = grafo.getAmigos(usuarioActual);
            Set<Usuario> susAmigos = grafo.getAmigos(usuario);
            
            long amigosEnComun = misAmigos.stream()
                    .filter(susAmigos::contains)
                    .count();
            
            if (amigosEnComun > 0) {
                Label conexion = new Label("🔗 " + amigosEnComun + " amigo" + (amigosEnComun > 1 ? "s" : "") + " en común");
                conexion.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 12px; -fx-font-weight: bold;");
                info.getChildren().addAll(nombre, username, conexion);
            } else {
                info.getChildren().addAll(nombre, username);
            }
        } else {
            info.getChildren().addAll(nombre, username);
        }

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Verificar si ya es amigo
        boolean yaEsAmigo = grafo.getAmigos(usuarioActual).contains(usuario);

        // Botón de seguir/dejar de seguir
        Button btnAccion = new Button(yaEsAmigo ? "✓ Siguiendo" : "+ Seguir");
        btnAccion.setStyle(yaEsAmigo 
            ? "-fx-background-color: #535353; -fx-text-fill: white; " +
              "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 24px; " +
              "-fx-background-radius: 20px; -fx-cursor: hand;"
            : "-fx-background-color: #1DB954; -fx-text-fill: white; " +
              "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 24px; " +
              "-fx-background-radius: 20px; -fx-cursor: hand;");
        
        if (yaEsAmigo) {
            // Efecto hover para mostrar "Dejar de seguir"
            btnAccion.setOnMouseEntered(e -> {
                btnAccion.setText("✗ Dejar de seguir");
                btnAccion.setStyle("-fx-background-color: #b91d1d; -fx-text-fill: white; " +
                                  "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 24px; " +
                                  "-fx-background-radius: 20px; -fx-cursor: hand;");
            });
            
            btnAccion.setOnMouseExited(e -> {
                btnAccion.setText("✓ Siguiendo");
                btnAccion.setStyle("-fx-background-color: #535353; -fx-text-fill: white; " +
                                  "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 24px; " +
                                  "-fx-background-radius: 20px; -fx-cursor: hand;");
            });
            
            btnAccion.setOnAction(e -> dejarDeSeguirUsuario(usuario, btnAccion));
        } else {
            btnAccion.setOnAction(e -> seguirUsuario(usuario, btnAccion));
        }

        card.getChildren().addAll(icono, info, spacer, btnAccion);
        return card;
    }

    /**
     * Agrega una amistad entre el usuario actual y el usuario seleccionado.
     */
    private void seguirUsuario(Usuario usuario, Button btn) {
        boolean exito = grafo.agregarAmistad(usuarioActual, usuario);
        
        if (exito) {
            mostrarMensaje("✓ Ahora sigues a @" + usuario.getUsername(), "success");
            
            // Cambiar botón a "Siguiendo" con funcionalidad de dejar de seguir
            btn.setText("✓ Siguiendo");
            btn.setStyle("-fx-background-color: #535353; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 24px; " +
                        "-fx-background-radius: 20px; -fx-cursor: hand;");
            
            // Agregar efecto hover
            btn.setOnMouseEntered(e -> {
                btn.setText("✗ Dejar de seguir");
                btn.setStyle("-fx-background-color: #b91d1d; -fx-text-fill: white; " +
                            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 24px; " +
                            "-fx-background-radius: 20px; -fx-cursor: hand;");
            });
            
            btn.setOnMouseExited(e -> {
                btn.setText("✓ Siguiendo");
                btn.setStyle("-fx-background-color: #535353; -fx-text-fill: white; " +
                            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 24px; " +
                            "-fx-background-radius: 20px; -fx-cursor: hand;");
            });
            
            btn.setOnAction(e -> dejarDeSeguirUsuario(usuario, btn));
            
        } else {
            mostrarMensaje("⚠️ No se pudo seguir a " + usuario.getUsername(), "error");
        }
    }

    /**
     * Elimina la amistad entre el usuario actual y el usuario seleccionado.
     */
    private void dejarDeSeguirUsuario(Usuario usuario, Button btn) {
        boolean exito = grafo.eliminarAmistad(usuarioActual, usuario);
        
        if (exito) {
            mostrarMensaje("✓ Ya no sigues a @" + usuario.getUsername(), "success");
            
            // Cambiar botón a "Seguir"
            btn.setText("+ Seguir");
            btn.setStyle("-fx-background-color: #1DB954; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 24px; " +
                        "-fx-background-radius: 20px; -fx-cursor: hand;");
            
            // Remover efectos hover
            btn.setOnMouseEntered(null);
            btn.setOnMouseExited(null);
            
            btn.setOnAction(e -> seguirUsuario(usuario, btn));
            
        } else {
            mostrarMensaje("⚠️ No se pudo dejar de seguir a " + usuario.getUsername(), "error");
        }
    }

    /**
     * Refresca la lista de usuarios.
     */
    @FXML
    private void onRefrescar() {
        cargarUsuarios();
        mostrarMensaje("🔄 Lista actualizada", "success");
    }

    /**
     * Vuelve al menú principal.
     */
    @FXML
    private void onVolver() {
        try {
            Stage st = (Stage) containerUsuarios.getScene().getWindow();
            var scene = new Scene(new FXMLLoader(HelloApplication.class.getResource("/view/MainView.fxml")).load(), 900, 600);
            scene.getStylesheets().add(HelloApplication.class.getResource("/styles/app.css").toExternalForm());
            st.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("⚠️ Error al volver al menú", "error");
        }
    }

    /**
     * Muestra un mensaje temporal en la UI.
     */
    private void mostrarMensaje(String mensaje, String tipo) {
        if (lblMensaje == null) return;
        
        lblMensaje.setText(mensaje);
        
        if ("success".equals(tipo)) {
            lblMensaje.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            lblMensaje.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
        
        lblMensaje.setVisible(true);
    }
}
