// src/main/java/org/dubytube/dubytube/viewController/ImportController.java
package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ImportController {

    @FXML private Label lblArchivo;
    @FXML private Label lblResumen;
    @FXML private TextArea txtLog;

    @FXML private void onElegirArchivo() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Selecciona CSV de canciones");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv", "*.txt"));
            File f = fc.showOpenDialog(null);
            if (f == null) return;

            if (lblArchivo != null) lblArchivo.setText(f.getAbsolutePath());
            importarCSV(f);
        } catch (Exception e) {
            e.printStackTrace();
            alertError("No se pudo abrir el selector.");
        }
    }

    // Alias por si tu FXML antiguo quedó con onPickFile
    @FXML private void onPickFile() { onElegirArchivo(); }

    private void importarCSV(File file) {
        int ok = 0, bad = 0, duplicados = 0;
        var repo = AppContext.canciones();

        try (var br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                String raw = line.trim();
                if (raw.isEmpty()) continue;
                if (lineNo == 1 && raw.toLowerCase(Locale.ROOT).contains("titulo")) continue;

                try {
                    String[] t = parseCSV(raw);
                    String id, titulo, artista, genero;
                    int anio, dur;

                    if (t.length == 6) {
                        id      = nvl(t[0]);
                        titulo  = nvl(t[1]);
                        artista = nvl(t[2]);
                        genero  = nvl(t[3]);
                        anio    = Integer.parseInt(nvl(t[4]));
                        dur     = Integer.parseInt(nvl(t[5]));
                        if (id.isBlank()) id = UUID.randomUUID().toString();
                    } else if (t.length == 5) {
                        id      = UUID.randomUUID().toString();
                        titulo  = nvl(t[0]);
                        artista = nvl(t[1]);
                        genero  = nvl(t[2]);
                        anio    = Integer.parseInt(nvl(t[3]));
                        dur     = Integer.parseInt(nvl(t[4]));
                    } else {
                        bad++;
                        log("Línea " + lineNo + ": columnas inválidas -> " + raw);
                        continue;
                    }

                    if (repo.find(id).isPresent()) {
                        duplicados++;
                        log("Línea " + lineNo + ": duplicado id=" + id);
                        continue;
                    }

                    var c = new Cancion(id, titulo, artista, genero, anio, dur);
                    repo.save(c);
                    ok++;

                    AppContext.indice().registrarCancion(c);
                    conectarSimilitudesHeuristica(c);

                } catch (Exception ex) {
                    bad++;
                    log("Línea " + lineNo + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error leyendo el archivo.");
            return;
        }

        if (lblResumen != null) {
            lblResumen.setText("Importadas: " + ok + " | Duplicadas: " + duplicados + " | Erróneas: " + bad);
        }
        alertInfo("Importación terminada.\nOK=" + ok + ", duplicados=" + duplicados + ", errores=" + bad);
    }

    private void conectarSimilitudesHeuristica(Cancion c) {
        var grafo = AppContext.similitud();
        var todas = AppContext.canciones().findAll().stream()
                .filter(o -> !o.getId().equals(c.getId()))
                .collect(Collectors.toList());

        for (var o : todas) {
            double score = 0;
            if (safeEq(c.getArtista(), o.getArtista())) score += 5.0;
            if (safeEq(c.getGenero(),  o.getGenero()))  score += 3.0;
            int diff = Math.abs(c.getAnio() - o.getAnio());
            if (diff <= 2)      score += 2.0;
            else if (diff <= 5) score += 1.0;

            double distancia = Math.max(0.5, 10.0 - score);
            grafo.agregarSimilitud(c.getId(), o.getId(), distancia);
        }
    }

    private static boolean safeEq(String a, String b) { return a != null && b != null && a.equalsIgnoreCase(b); }
    private static String[] parseCSV(String s) {
        List<String> out = new ArrayList<>();
        boolean inQ = false; StringBuilder cur = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch == '"') { inQ = !inQ; continue; }
            if (ch == ',' && !inQ) { out.add(cur.toString()); cur.setLength(0); continue; }
            cur.append(ch);
        }
        out.add(cur.toString());
        return out.stream().map(String::trim).toArray(String[]::new);
    }
    private static String nvl(String s){ return s == null ? "" : s.trim(); }

    @FXML
    private void onVolver() {
        try {
            Stage st = (Stage) (lblArchivo != null ? lblArchivo.getScene().getWindow() : txtLog.getScene().getWindow());
            var scene = new Scene(new FXMLLoader(HelloApplication.class.getResource("/view/MainView.fxml")).load(), 900, 600);
            scene.getStylesheets().add(HelloApplication.class.getResource("/styles/app.css").toExternalForm());
            st.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            alertError("No se pudo volver al menú.");
        }
    }

    private void log(String msg){ if (txtLog != null) txtLog.appendText(msg + "\n"); }
    private void alertInfo(String msg){ new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void alertError(String msg){ new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
