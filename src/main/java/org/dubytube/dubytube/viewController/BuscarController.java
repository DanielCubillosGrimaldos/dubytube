package org.dubytube.dubytube.viewController;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.CancionIndice;

import java.util.List;

public class BuscarController {
    @FXML private TextField txtPrefijo;
    @FXML private TableView<Cancion> tblResultados;

    // columnas (deben existir en el FXML con esos fx:id)
    @FXML private TableColumn<Cancion, String> colTitulo;
    @FXML private TableColumn<Cancion, String> colArtista;
    @FXML private TableColumn<Cancion, String> colGenero;
    @FXML private TableColumn<Cancion, Integer> colAnio;

    private final CancionRepo repo = new CancionRepo();
    private final CancionIndice indice = new CancionIndice(repo);

    @FXML
    public void initialize() {
        // Enlazar propiedades -> columnas
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colArtista.setCellValueFactory(new PropertyValueFactory<>("artista"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));

        // Datos de prueba (luego lo reemplazamos por carga real)
        repo.save(new Cancion("1","Love Song","Adele","Pop",2015,210));
        repo.save(new Cancion("2","Lobo Hombre","La Unión","Rock",1984,190));
        repo.save(new Cancion("3","Ave Maria","Schubert","Clásica",1825,150));
        indice.indexarExistentes();
    }

    @FXML
    private void onBuscar() {
        String pref = txtPrefijo.getText();
        List<Cancion> lista = indice.sugerirPorTitulo(pref, 20);
        tblResultados.setItems(FXCollections.observableArrayList(lista));
    }
}

