// src/main/java/org/dubytube/dubytube/viewController/ImportController.java
package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;

import java.io.File;

/**
 * Controlador para la vista de exportación de datos en CSV.
 * 
 * Funcionalidades:
 * - Exportar catálogo completo de canciones
 * - Exportar lista de usuarios (sin contraseñas)
 * - Exportar estadísticas de géneros musicales
 * 
 * @author DubyTube Team
 * @version 3.0 - Solo exportación (importación removida)
 * @since 2025-11-18
 */
public class ImportController {

    // ==================== MÉTODOS DE EXPORTACIÓN ====================

    @FXML
    private void onExportarCanciones() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Exportar Catálogo de Canciones");
            fc.setInitialFileName("canciones_" + System.currentTimeMillis() + ".csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File f = fc.showSaveDialog(null);
            if (f == null) return;

            var canciones = AppContext.canciones().findAll();
            org.dubytube.dubytube.services.ExportarServices.exportCatalogoCanciones(
                canciones, 
                f.toPath()
            );
            
            alertInfo("✓ Catálogo exportado exitosamente\n\n" + 
                     "Canciones: " + canciones.size() + "\n" +
                     "Archivo: " + f.getAbsolutePath());
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al exportar canciones: " + e.getMessage());
        }
    }
    
    @FXML
    private void onExportarUsuarios() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Exportar Lista de Usuarios");
            fc.setInitialFileName("usuarios_" + System.currentTimeMillis() + ".csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File f = fc.showSaveDialog(null);
            if (f == null) return;

            var usuarios = AppContext.usuarios().findAll();
            org.dubytube.dubytube.services.ExportarServices.exportUsuarios(
                usuarios, 
                f.toPath()
            );
            
            alertInfo("✓ Usuarios exportados exitosamente\n\n" + 
                     "Usuarios: " + usuarios.size() + "\n" +
                     "Archivo: " + f.getAbsolutePath());
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al exportar usuarios: " + e.getMessage());
        }
    }
    
    @FXML
    private void onExportarGeneros() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Exportar Estadísticas de Géneros");
            fc.setInitialFileName("generos_" + System.currentTimeMillis() + ".csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File f = fc.showSaveDialog(null);
            if (f == null) return;

            var canciones = AppContext.canciones().findAll();
            int totalGeneros = org.dubytube.dubytube.services.ExportarServices.exportGeneros(
                canciones, 
                f.toPath()
            );
            
            alertInfo("✓ Estadísticas de géneros exportadas exitosamente\n\n" + 
                     "Géneros únicos: " + totalGeneros + "\n" +
                     "Total canciones: " + canciones.size() + "\n" +
                     "Archivo: " + f.getAbsolutePath());
            
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error al exportar géneros: " + e.getMessage());
        }
    }

    /**
     * Vuelve al menú principal.
     */
    @FXML
    private void onVolver(javafx.event.ActionEvent event) {
        try {
            Stage st = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            var scene = new Scene(new FXMLLoader(HelloApplication.class.getResource("/view/MainView.fxml")).load(), 900, 600);
            scene.getStylesheets().add(HelloApplication.class.getResource("/styles/app.css").toExternalForm());
            st.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            alertError("No se pudo volver al menú: " + e.getMessage());
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    
    private void alertInfo(String msg) { 
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); 
    }
    
    private void alertError(String msg) { 
        new Alert(Alert.AlertType.ERROR, msg).showAndWait(); 
    }
}
