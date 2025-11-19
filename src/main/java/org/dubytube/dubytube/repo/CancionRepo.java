package org.dubytube.dubytube.repo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.dubytube.dubytube.domain.Cancion;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Repositorio de canciones con persistencia en JSON.
 * 
 * <p>Guarda las canciones en un archivo JSON para mantener la persistencia
 * entre reinicios de la aplicación. Los archivos de audio se guardan
 * físicamente en src/main/resources/audio/</p>
 * 
 * @author DubyTube Team
 * @version 2.0
 * @since 2025-11-18
 */
public class CancionRepo {
    
    private static final String FILE_PATH = "src/main/resources/data/canciones.json";
    
    private final Map<String, Cancion> idx = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public CancionRepo() {
        loadFromJson();
        System.out.println("✓ CancionRepo inicializado: " + idx.size() + " canciones cargadas");
    }
    
    // =====================================================
    // CRUD PÚBLICO
    // =====================================================
    
    public Optional<Cancion> find(String id) {
        return Optional.ofNullable(idx.get(id));
    }
    
    public Cancion save(Cancion c) {
        idx.put(c.getId(), c);
        saveToJson();
        System.out.println("✓ Canción guardada: " + c.getTitulo());
        return c;
    }
    
    public boolean delete(String id) {
        boolean removed = idx.remove(id) != null;
        if (removed) {
            saveToJson();
            System.out.println("✓ Canción eliminada: " + id);
        }
        return removed;
    }
    
    public Collection<Cancion> findAll() {
        return idx.values();
    }
    
    public void saveAll() {
        saveToJson();
        System.out.println("✓ Todas las canciones guardadas en JSON");
    }
    
    // =====================================================
    // MANEJO DE JSON
    // =====================================================
    
    private void loadFromJson() {
        try {
            if (!Files.exists(Paths.get(FILE_PATH))) {
                System.out.println("⚠ Archivo canciones.json no existe. Se creará uno nuevo al guardar.");
                // Crear directorio si no existe
                Files.createDirectories(Paths.get(FILE_PATH).getParent());
                return;
            }
            
            FileReader reader = new FileReader(FILE_PATH);
            
            Type listType = new TypeToken<List<Cancion>>() {}.getType();
            List<Cancion> lista = gson.fromJson(reader, listType);
            reader.close();
            
            if (lista != null) {
                for (Cancion c : lista) {
                    idx.put(c.getId(), c);
                }
                System.out.println("✓ Cargadas " + lista.size() + " canciones desde JSON");
            }
            
        } catch (Exception e) {
            System.err.println("⚠ Error cargando canciones.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveToJson() {
        try {
            // Crear directorio si no existe
            Files.createDirectories(Paths.get(FILE_PATH).getParent());
            
            FileWriter writer = new FileWriter(FILE_PATH);
            
            List<Cancion> lista = new ArrayList<>(idx.values());
            gson.toJson(lista, writer);
            
            writer.close();
            
        } catch (Exception e) {
            System.err.println("⚠ Error guardando canciones.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
