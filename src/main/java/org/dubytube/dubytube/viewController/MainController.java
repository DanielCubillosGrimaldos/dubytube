package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.services.Session;
import org.dubytube.dubytube.services.ExportarServices;

import java.nio.file.Path;

public class MainController {

    @FXML private Label lblUsuario;
    @FXML private Button btnImport;  // Admin only
    @FXML private Button btnCrud;    // Admin only

    @FXML
    public void initialize() {
        // Mostrar saludo y ocultar botones admin si no corresponde
        if (Session.isLogged()) {
            var u = Session.get();
            lblUsuario.setText("Hola, " + u.getNombre() + (u.getRole()!=null? " ("+u.getRole()+")" : ""));
            boolean isAdmin = (u.getRole() == Role.ADMIN);
            if (btnImport != null) btnImport.setVisible(isAdmin);
            if (btnCrud   != null) btnCrud.setVisible(isAdmin);
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
            var u = Session.get();
            // Ajusta al nombre real de tu clase de exportación:
            ExportarServices.exportFavoritos(u, Path.of("favoritos.csv"));
            // Si no tienes Label para mensajes, puedes usar Alert:
            // new Alert(AlertType.INFORMATION, "Exportado a favoritos.csv").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // new Alert(AlertType.ERROR, "Error exportando").showAndWait();
        }
    }

    @FXML
    private void onLogout() {
        Session.clear();
        go("/view/LoginView.fxml", "Login");
    }

    private void go(String fxml, String title){
        try{
            var stage = (Stage) lblUsuario.getScene().getWindow();
            var scene = new Scene(new FXMLLoader(HelloApplication.class.getResource(fxml)).load(), 900, 600);
            stage.setTitle(title);
            stage.setScene(scene);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}