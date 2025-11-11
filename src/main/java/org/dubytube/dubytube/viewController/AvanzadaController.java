package org.dubytube.dubytube.viewController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.BusquedaAvanzada;
import java.util.List;
public class AvanzadaController {
    @FXML private TextField txtArtista, txtGenero, txtAnioMin, txtAnioMax;
    @FXML private RadioButton rbAND, rbOR;
    @FXML private TableView<Cancion> tblAvanzada;
    private final CancionRepo repo = new CancionRepo();
    private final BusquedaAvanzada svc = new BusquedaAvanzada(repo);
    @FXML public void initialize() {
        // mismos datos de prueba que arriba o carga real
        repo.save(new Cancion("1","Love Song","Adele","Pop",2015,210));
        repo.save(new Cancion("2","Lobo Hombre","La Unión","Rock",1984,190));
        repo.save(new Cancion("3","Ave Maria","Schubert","Clásica",1825,150));
    }
    @FXML
    public void onBuscarAvanzado() {
        Integer min = parseI(txtAnioMin.getText());
        Integer max = parseI(txtAnioMax.getText());
        var logica = rbAND.isSelected() ? BusquedaAvanzada.Logica.AND : BusquedaAvanzada.Logica.OR;

        List<Cancion> r = svc.buscar(
                txtArtista.getText(),
                txtGenero.getText(),
                min, max, logica
        );
        tblAvanzada.setItems(FXCollections.observableArrayList(r));
    }

    private Integer parseI(String s){ try { return (s==null||s.isBlank())? null: Integer.parseInt(s.trim()); } catch (Exception e){ return null; } }
}
