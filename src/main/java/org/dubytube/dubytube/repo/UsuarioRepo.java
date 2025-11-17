package org.dubytube.dubytube.repo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UsuarioRepo {

    // Puedes usar tu ruta absoluta o una local. Ambas funcionan.
    // RUTA LOCAL RECOMENDADA:
    private static final String FILE_PATH = "src/main/resources/data/usuarios.json";

    // Si quieres mantener tu ruta absoluta, úsala aquí:
    // private static final String FILE_PATH = "C:\\UQ\\Estructura de datos\\dubytube\\src\\main\\resources\\data\\usuarios.json";

    private final Map<String, Usuario> idx = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public UsuarioRepo() {
        loadFromJson();

        // Si no existen usuarios, crear los iniciales
        if (idx.isEmpty()) {
            Usuario admin = new Usuario("admin", "123", "Administrador");
            admin.setRole(Role.ADMIN);

            Usuario demo = new Usuario("daniel", "123", "Daniel");
            demo.setRole(Role.USER);

            idx.put(admin.getUsername(), admin);
            idx.put(demo.getUsername(), demo);

            saveToJson();
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
                System.out.println("Archivo JSON no existe. Se creará uno nuevo.");
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
            }

        } catch (Exception e) {
            System.out.println("Error cargando JSON: " + e.getMessage());
        }
    }

    private void saveToJson() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {

            List<Usuario> lista = new ArrayList<>(idx.values());
            gson.toJson(lista, writer);

        } catch (Exception e) {
            System.out.println("Error guardando JSON: " + e.getMessage());
        }
    }
}
