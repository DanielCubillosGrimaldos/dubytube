package org.dubytube.dubytube.repo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.ds.MyLinkedList;
import org.dubytube.dubytube.util.MyLinkedListAdapter;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Repositorio de usuarios con persistencia en JSON.
 * 
 * <p>Guarda los usuarios en un archivo JSON para mantener la persistencia
 * entre reinicios de la aplicación.</p>
 * 
 * @author DubyTube Team
 * @version 2.0
 * @since 2025-11-18
 */
public class UsuarioRepo {

    private static final String FILE_PATH = "src/main/resources/data/usuarios.json";

    private final Map<String, Usuario> idx = new HashMap<>();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(MyLinkedList.class, new MyLinkedListAdapter<>())
            .create();

    public UsuarioRepo() {
        loadFromJson();

        // Si no existen usuarios, crear los iniciales
        if (idx.isEmpty()) {
            System.out.println("⚠ UsuarioRepo vacío. Creando usuarios por defecto...");
            
            Usuario admin = new Usuario("admin", "123", "Administrador");
            admin.setRole(Role.ADMIN);

            Usuario demo = new Usuario("daniel", "123", "Daniel");
            demo.setRole(Role.USER);

            idx.put(admin.getUsername(), admin);
            idx.put(demo.getUsername(), demo);

            saveToJson();
            System.out.println("✓ Usuarios por defecto creados y guardados");
        } else {
            System.out.println("✓ UsuarioRepo inicializado: " + idx.size() + " usuarios cargados");
        }
    }

    // =====================================================
    // CRUD PÚBLICO
    // =====================================================

    public boolean register(Usuario u) {
        if (exists(u.getUsername()))
            return false;

        idx.put(u.getUsername(), u);
        saveToJson();
        return true;
    }

    public Optional<Usuario> find(String username) {
        return Optional.ofNullable(idx.get(username));
    }

    public boolean exists(String username) {
        return idx.containsKey(username);
    }

    public Usuario save(Usuario u) {
        idx.put(u.getUsername(), u);
        saveToJson();
        return u;
    }

    public boolean delete(String username) {
        boolean removed = idx.remove(username) != null;
        if (removed) saveToJson();
        return removed;
    }

    public Collection<Usuario> findAll() {
        return idx.values();
    }

    // =====================================================
    // MANEJO DE JSON
    // =====================================================

    private void loadFromJson() {
        try {
            if (!Files.exists(Paths.get(FILE_PATH))) {
                System.out.println("⚠ Archivo usuarios.json no existe. Se creará uno nuevo al guardar.");
                // Crear directorio si no existe
                Files.createDirectories(Paths.get(FILE_PATH).getParent());
                return;
            }

            // Verificar si el archivo está vacío
            if (Files.size(Paths.get(FILE_PATH)) == 0) {
                System.out.println("⚠ Archivo usuarios.json está vacío. Se inicializará con usuarios por defecto.");
                return;
            }

            FileReader reader = new FileReader(FILE_PATH);

            Type listType = new TypeToken<List<Usuario>>() {}.getType();
            List<Usuario> lista = gson.fromJson(reader, listType);
            reader.close();

            if (lista != null) {
                for (Usuario u : lista) {
                    idx.put(u.getUsername(), u);
                }
                System.out.println("✓ Cargados " + lista.size() + " usuarios desde JSON");
            }

        } catch (Exception e) {
            System.err.println("⚠ Error cargando usuarios.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveToJson() {
        try {
            // Crear directorio si no existe
            Files.createDirectories(Paths.get(FILE_PATH).getParent());
            
            FileWriter writer = new FileWriter(FILE_PATH);

            List<Usuario> lista = new ArrayList<>(idx.values());
            gson.toJson(lista, writer);

            writer.close();
            System.out.println("✓ " + lista.size() + " usuarios guardados en JSON");

        } catch (Exception e) {
            System.err.println("⚠ Error guardando usuarios.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
