package org.dubytube.dubytube.viewController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.dubytube.dubytube.AppContext;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador de la vista de métricas y estadísticas del sistema.
 * 
 * <p>RF-014: Visualización de métricas con JavaFX Charts</p>
 * <ul>
 *   <li>Pie Chart: Distribución de géneros musicales</li>
 *   <li>Bar Chart: Artistas más populares (por cantidad de canciones)</li>
 *   <li>Line Chart: Canciones por década</li>
 *   <li>Area Chart: Duración promedio por género</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class MetricasController {

    @FXML private PieChart chartGeneros;
    @FXML private BarChart<String, Number> chartArtistas;
    @FXML private LineChart<String, Number> chartDecadas;
    @FXML private AreaChart<String, Number> chartDuraciones;
    
    @FXML private Label lblTotalCanciones;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblTotalGeneros;
    @FXML private Label lblTotalArtistas;

    @FXML
    public void initialize() {
        cargarMetricas();
    }

    /**
     * Carga todas las métricas y actualiza los gráficos.
     */
    private void cargarMetricas() {
        var canciones = AppContext.canciones().findAll();
        var usuarios = AppContext.usuarios().findAll();
        
        // Actualizar contadores generales
        if (lblTotalCanciones != null) {
            lblTotalCanciones.setText(String.valueOf(canciones.size()));
        }
        if (lblTotalUsuarios != null) {
            lblTotalUsuarios.setText(String.valueOf(usuarios.size()));
        }
        
        // Generar gráficos
        cargarPieChartGeneros(canciones);
        cargarBarChartArtistas(canciones);
        cargarLineChartDecadas(canciones);
        cargarAreaChartDuraciones(canciones);
        
        System.out.println("✓ Métricas cargadas: " + canciones.size() + " canciones, " + usuarios.size() + " usuarios");
    }

    /**
     * PieChart: Distribución de géneros musicales.
     */
    private void cargarPieChartGeneros(Collection<Cancion> canciones) {
        if (chartGeneros == null) return;
        
        chartGeneros.getData().clear();
        chartGeneros.setTitle("Distribución por Género");
        
        // Contar canciones por género
        Map<String, Long> generos = canciones.stream()
                .filter(c -> c.getGenero() != null && !c.getGenero().isBlank())
                .collect(Collectors.groupingBy(
                    Cancion::getGenero, 
                    Collectors.counting()
                ));
        
        // Ordenar por cantidad (descendente)
        generos.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10) // Top 10 géneros
                .forEach(entry -> {
                    PieChart.Data slice = new PieChart.Data(
                        entry.getKey() + " (" + entry.getValue() + ")", 
                        entry.getValue()
                    );
                    chartGeneros.getData().add(slice);
                });
        
        if (lblTotalGeneros != null) {
            lblTotalGeneros.setText(String.valueOf(generos.size()));
        }
        
        // Estilo
        chartGeneros.setLegendVisible(true);
        chartGeneros.setLabelsVisible(false); // No mostrar valores en las slices
    }

    /**
     * BarChart: Artistas con más canciones en el catálogo.
     */
    private void cargarBarChartArtistas(Collection<Cancion> canciones) {
        if (chartArtistas == null) return;
        
        chartArtistas.getData().clear();
        chartArtistas.setTitle("Top 10 Artistas más Populares");
        
        // Configurar ejes
        CategoryAxis xAxis = (CategoryAxis) chartArtistas.getXAxis();
        NumberAxis yAxis = (NumberAxis) chartArtistas.getYAxis();
        xAxis.setLabel("Artista");
        yAxis.setLabel("Cantidad de Canciones");
        
        // Contar canciones por artista
        Map<String, Long> artistas = canciones.stream()
                .filter(c -> c.getArtista() != null && !c.getArtista().isBlank())
                .collect(Collectors.groupingBy(
                    Cancion::getArtista, 
                    Collectors.counting()
                ));
        
        if (lblTotalArtistas != null) {
            lblTotalArtistas.setText(String.valueOf(artistas.size()));
        }
        
        // Crear serie
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Canciones");
        
        // Top 10 artistas
        artistas.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });
        
        chartArtistas.getData().add(serie);
        chartArtistas.setLegendVisible(false);
    }

    /**
     * LineChart: Canciones por década.
     */
    private void cargarLineChartDecadas(Collection<Cancion> canciones) {
        if (chartDecadas == null) return;
        
        chartDecadas.getData().clear();
        chartDecadas.setTitle("Canciones por Década");
        
        // Configurar ejes
        CategoryAxis xAxis = (CategoryAxis) chartDecadas.getXAxis();
        NumberAxis yAxis = (NumberAxis) chartDecadas.getYAxis();
        xAxis.setLabel("Década");
        yAxis.setLabel("Cantidad de Canciones");
        
        // Agrupar por década
        Map<String, Long> decadas = canciones.stream()
                .collect(Collectors.groupingBy(
                    c -> {
                        int decada = (c.getAnio() / 10) * 10;
                        return decada + "s";
                    },
                    Collectors.counting()
                ));
        
        // Crear serie
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Canciones");
        
        // Ordenar por década
        decadas.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });
        
        chartDecadas.getData().add(serie);
        chartDecadas.setCreateSymbols(true);
        chartDecadas.setLegendVisible(false);
    }

    /**
     * AreaChart: Duración promedio por género.
     */
    private void cargarAreaChartDuraciones(Collection<Cancion> canciones) {
        if (chartDuraciones == null) return;
        
        chartDuraciones.getData().clear();
        chartDuraciones.setTitle("Duración Promedio por Género");
        
        // Configurar ejes
        CategoryAxis xAxis = (CategoryAxis) chartDuraciones.getXAxis();
        NumberAxis yAxis = (NumberAxis) chartDuraciones.getYAxis();
        xAxis.setLabel("Género");
        yAxis.setLabel("Duración Promedio (seg)");
        
        // Calcular duración promedio por género
        Map<String, Double> duraciones = canciones.stream()
                .filter(c -> c.getGenero() != null && !c.getGenero().isBlank())
                .collect(Collectors.groupingBy(
                    Cancion::getGenero,
                    Collectors.averagingInt(Cancion::getDuracionSeg)
                ));
        
        // Crear serie
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Duración Promedio");
        
        // Top 10 géneros por duración
        duraciones.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    serie.getData().add(new XYChart.Data<>(
                        entry.getKey(), 
                        Math.round(entry.getValue())
                    ));
                });
        
        chartDuraciones.getData().add(serie);
        chartDuraciones.setLegendVisible(false);
        chartDuraciones.setCreateSymbols(false);
    }

    @FXML
    private void onRefrescar() {
        cargarMetricas();
        alertInfo("Métricas actualizadas correctamente.");
    }

    @FXML
    private void onVolver() {
        try {
            Stage st = (Stage) chartGeneros.getScene().getWindow();
            var scene = new Scene(
                new FXMLLoader(HelloApplication.class.getResource("/view/MainView.fxml")).load(), 
                900, 600
            );
            scene.getStylesheets().add(
                HelloApplication.class.getResource("/styles/app.css").toExternalForm()
            );
            st.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            alertError("No se pudo volver al menú.");
        }
    }

    private void alertInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    private void alertError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
