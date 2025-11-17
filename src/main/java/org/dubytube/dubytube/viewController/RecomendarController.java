package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.dubytube.dubytube.HelloApplication;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.RecomendacionService;

import java.util.List;

public class RecomendarController {

    @FXML private ComboBox<Cancion> cmbSemilla;
    @FXML private Spinner<Integer> spK;
    @FXML private TableView<RecomendacionService.Rec> tblResultados;

    @FXML private TableColumn<RecomendacionService.Rec, String>  colTitulo;
    @FXML private TableColumn<RecomendacionService.Rec, String>  colArtista;
    @FXML private TableColumn<RecomendacionService.Rec, String>  colGenero;
    @FXML private TableColumn<RecomendacionService.Rec, Integer> colAnio;
    @FXML private TableColumn<RecomendacionService.Rec, String>  colDist;

    private final CancionRepo repo = new CancionRepo();
    private RecomendacionService svc;

    @FXML
    public void initialize() {
        // Dataset de ejemplo (puedes unificar con tu repo global si lo prefieres)
        repo.save(new Cancion("1","Love Song","Adele","Pop",2015,210));
        repo.save(new Cancion("2","Lobo Hombre","La Unión","Rock",1984,190));
        repo.save(new Cancion("3","Ave Maria","Schubert","Clásica",1825,150));
        repo.save(new Cancion("4","Rolling in the Deep","Adele","Pop",2011,228));
        repo.save(new Cancion("5","Someone Like You","Adele","Pop",2011,285));
        repo.save(new Cancion("6","Bohemian Rhapsody","Queen","Rock",1975,354));

        // Servicio
        svc = new RecomendacionService(repo);

        // Semillas
        cmbSemilla.setItems(FXCollections.observableArrayList(repo.findAll()));
        cmbSemilla.setConverter(new StringConverter<>() {
            @Override public String toString(Cancion c) {
                return c==null ? "" : c.getTitulo()+" — "+c.getArtista()+" ("+c.getGenero()+", "+c.getAnio()+")";
            }
            @Override public Cancion fromString(String s) { return null; }
        });

        // k
        spK.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5));

        // Columnas de la tabla
        colTitulo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().cancion.getTitulo()));
        colArtista.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().cancion.getArtista()));
        colGenero.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().cancion.getGenero()));
        colAnio.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().cancion.getAnio()).asObject());
        colDist.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.3f", c.getValue().distancia)));
    }

    @FXML
    private void onRecomendar() {
        Cancion seed = cmbSemilla.getValue();
        if (seed == null) return;
        int k = spK.getValue();
        List<RecomendacionService.Rec> out = svc.recomendar(seed.getId(), k);
        tblResultados.setItems(FXCollections.observableArrayList(out));
        tblResultados.refresh();
    }

    @FXML
    private void onVolver() {
        try {
            Stage stage = (Stage) tblResultados.getScene().getWindow();
            var url = HelloApplication.class.getResource("/view/MainView.fxml");
            var scene = new Scene(new FXMLLoader(url).load(), 900, 600);
            stage.setTitle("Inicio");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
