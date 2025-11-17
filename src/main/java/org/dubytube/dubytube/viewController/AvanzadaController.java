package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.BusquedaAvanzada;

import java.util.List;

public class AvanzadaController {

    // Filtros
    @FXML private TextField txtArtista, txtGenero, txtAnioMin, txtAnioMax;
    @FXML private RadioButton rbAND, rbOR;
    @FXML private TableView<Cancion> tblAvanzada;

    private final CancionRepo repo = new CancionRepo();
    private final BusquedaAvanzada svc = new BusquedaAvanzada(repo);

    @FXML
    public void initialize() {
        // ToggleGroup para AND/OR
        ToggleGroup logica = new ToggleGroup();
        rbAND.setToggleGroup(logica);
        rbOR.setToggleGroup(logica);
        rbOR.setSelected(true);

        // Columnas por código (no dependen de IDs en el FXML)
        TableColumn<Cancion,String> cTitulo  = new TableColumn<>("Título");
        cTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        cTitulo.setPrefWidth(240);

        TableColumn<Cancion,String> cArtista = new TableColumn<>("Artista");
        cArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        cArtista.setPrefWidth(200);

        TableColumn<Cancion,String> cGenero  = new TableColumn<>("Género");
        cGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        cGenero.setPrefWidth(140);

        TableColumn<Cancion,Integer> cAnio   = new TableColumn<>("Año");
        cAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        cAnio.setPrefWidth(80);

        tblAvanzada.getColumns().setAll(cTitulo, cArtista, cGenero, cAnio);

        // Datos de prueba (quítalos si ya cargas un catálogo real)
        repo.save(new Cancion("1","Love Song","Adele","Pop",2015,210));
        repo.save(new Cancion("2","Lobo Hombre","La Unión","Rock",1984,190));
        repo.save(new Cancion("3","Ave María","Schubert","Clásica",1825,150));
    }

    @FXML
    private void onBuscarAvanzado() {
        Integer min = parseI(txtAnioMin.getText());
        Integer max = parseI(txtAnioMax.getText());
        var logica = rbAND.isSelected() ? BusquedaAvanzada.Logica.AND : BusquedaAvanzada.Logica.OR;

        List<Cancion> r = svc.buscar(
                safe(txtArtista.getText()),
                safe(txtGenero.getText()),
                min, max, logica
        );
        tblAvanzada.setItems(FXCollections.observableArrayList(r));
    }

    private String safe(String s){ return (s==null || s.isBlank()) ? null : s.trim(); }
    private Integer parseI(String s){
        try { return (s==null || s.isBlank()) ? null : Integer.parseInt(s.trim()); }
        catch (Exception e){ return null; }
    }
}
