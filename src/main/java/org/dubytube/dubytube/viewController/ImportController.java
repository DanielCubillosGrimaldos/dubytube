package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import static org.dubytube.dubytube.AppContext.canciones;

public class ImportController {

    @FXML private Label lblFile;
    @FXML private TextArea txtLog;

    private final CancionRepo repo = canciones();

    @FXML
    private void onPickFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecciona CSV de catálogo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(lblFile.getScene().getWindow());
        if (f == null) return;

        lblFile.setText(f.getName());
        importar(f);
    }

    private void importar(File f){
        int ok = 0, fail = 0, lineNo = 0;

        try (var br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (lineNo == 1 && line.toLowerCase(Locale.ROOT).contains("id,titulo")) {
                    // es cabecera; sáltala
                    continue;
                }
                String[] p = line.split(",", -1);
                if (p.length < 6) { log("Línea " + lineNo + " inválida: " + line); fail++; continue; }

                try {
                    String id   = p[0].trim();
                    String tit  = p[1].trim();
                    String art  = p[2].trim();
                    String gen  = p[3].trim();
                    int anio    = Integer.parseInt(p[4].trim());
                    int dur     = Integer.parseInt(p[5].trim());

                    repo.save(new Cancion(id, tit, art, gen, anio, dur));
                    ok++;
                } catch (Exception ex) {
                    log("Error en línea " + lineNo + ": " + ex.getMessage());
                    fail++;
                }
            }
            log("Importación terminada. OK=" + ok + ", FALLÓ=" + fail);
        } catch (Exception e) {
            e.printStackTrace();
            alert("No se pudo leer el archivo.\n" + e.getMessage());
        }
    }

    private void log(String s){ txtLog.appendText(s + "\n"); }
    private void alert(String s){ new Alert(Alert.AlertType.ERROR, s).showAndWait(); }

    @FXML
    private void onVolver() {
        try {
            var stage = (Stage) lblFile.getScene().getWindow();
            var url   = HelloApplication.class.getResource("/view/MainView.fxml");
            var scene = new Scene(new FXMLLoader(url).load(), 900, 600);
            scene.getStylesheets().add(HelloApplication.class.getResource("/styles/app.css").toExternalForm());
            stage.setTitle("Dubytube");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            alert("No se pudo volver al inicio.");
        }
    }
}
