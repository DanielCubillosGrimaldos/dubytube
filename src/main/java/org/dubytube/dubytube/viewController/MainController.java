package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.services.ExportarServices;
import org.dubytube.dubytube.services.Session;

import java.io.File;
import java.nio.file.Path;

public class MainController {

    @FXML private Label lblUsuario;
    @FXML private Button btnImport;  // Admin only
    @FXML private Button btnCrud;    // Admin only

    @FXML
    public void initialize() {

        AppContext.bootstrapIfEmpty();
        // Mostrar saludo y ocultar botones admin si no corresponde
        if (Session.isLogged()) {
            var u = Session.get();
            lblUsuario.setText("Hola, " + u.getNombre() + (u.getRole()!=null? " ("+u.getRole()+")" : ""));
            boolean isAdmin = (u.getRole() == Role.ADMIN);
            if (btnImport != null) btnImport.setVisible(isAdmin);
            if (btnCrud   != null) btnCrud.setVisible(isAdmin);
        } else {
            lblUsuario.setText("Sin sesión");
        }
    }

    // --- Navegación ---
    @FXML private void goBuscar()   { go("/view/BuscarView.fxml",       "Búsqueda por título"); }
    @FXML private void goAvanzada() { go("/view/AvanzadaView.fxml",     "Búsqueda avanzada");   }
    @FXML private void goRecom()    { go("/view/RecomendarView.fxml",   "Recomendaciones");     }
    @FXML private void goAmigos()   { go("/view/AmigosView.fxml",       "Amigos de amigos");    }
    @FXML private void goPerfil()   { go("/view/PerfilView.fxml",       "Mi Perfil");           }
    @FXML private void goImport()   { go("/view/ImportView.fxml",       "Importar catálogo");   }
    @FXML private void goCrud()     { go("/view/CrudCancionView.fxml",  "CRUD Canciones");      }

    @FXML
    private void onExport() {
        try {
            Usuario u = Session.get();
            if (u == null) {
                alertError("Debes iniciar sesión para exportar.");
                return;
            }
            if (u.getFavoritos() == null || u.getFavoritos().isEmpty()) {
                alertError("No tienes favoritos para exportar.");
                return;
            }

            // Elegir ruta destino
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar favoritos como CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            fc.setInitialFileName("favoritos_" + u.getUsername() + ".csv");
            File archivo = fc.showSaveDialog(lblUsuario.getScene().getWindow());
            if (archivo == null) return; // cancelado

            ExportarServices.exportFavoritos(u, Path.of(archivo.getAbsolutePath()));
            alertInfo("Favoritos exportados en:\n" + archivo.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error exportando favoritos.");
        }
    }

    @FXML
    private void onLogout() {
        Session.clear();
        go("/view/LoginView.fxml", "Login");
    }

    // --- utilidades ---
    private void go(String fxml, String title){
        try{
            var stage = (Stage) lblUsuario.getScene().getWindow();

            var url = HelloApplication.class.getResource(fxml);
            if (url == null) {
                alertError("No se encontró la vista: " + fxml);
                return;
            }

            var root = new FXMLLoader(url).load();
            var scene = new Scene((Parent) root, 900, 600);
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("/styles/app.css").toExternalForm()
            );

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show(); // por si vienes desde otra ventana/escena
        }catch(Exception e){
            e.printStackTrace();
            alertError("No se pudo cargar " + fxml + ":\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }





    private void alertInfo(String msg){
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
    private void alertError(String msg){
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }


}
