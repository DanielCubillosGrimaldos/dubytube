package org.dubytube.dubytube.repo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.dubytube.dubytube.domain.Genero;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Repositorio para gestionar géneros musicales.
 * Almacena los géneros en un archivo JSON.
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class GeneroRepo {
    
    private static final Path GENEROS_FILE = Paths.get("src/main/resources/data/generos.json");
    private final Gson gson;
    private final Map<String, Genero> generos; // id -> Genero

    public GeneroRepo() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.generos = new HashMap<>();
        cargar();
        inicializarGenerosDefault();
    }

    /**
     * Inicializa géneros por defecto si no existen.
     */
    private void inicializarGenerosDefault() {
        if (generos.isEmpty()) {
            save(new Genero("rock", "Rock", "Música rock clásico y moderno"));
            save(new Genero("pop", "Pop", "Música pop comercial"));
            save(new Genero("jazz", "Jazz", "Jazz y música improvisada"));
            save(new Genero("classical", "Clásica", "Música clásica orquestal"));
            save(new Genero("electronic", "Electrónica", "Música electrónica y EDM"));
            save(new Genero("hip-hop", "Hip Hop", "Rap y hip hop"));
            save(new Genero("country", "Country", "Música country y folk"));
            save(new Genero("reggae", "Reggae", "Reggae y música caribeña"));
            save(new Genero("blues", "Blues", "Blues y R&B"));
            save(new Genero("metal", "Metal", "Heavy metal y subgéneros"));
            System.out.println("✓ Géneros inicializados: " + generos.size());
        }
    }

    /**
     * Guarda un género en el repositorio.
     * 
     * @param genero Género a guardar
     * @return true si se guardó exitosamente
     */
    public boolean save(Genero genero) {
        if (genero == null || genero.getId() == null) {
            return false;
        }
        
        generos.put(genero.getId(), genero);
        return persistir();
    }

    /**
     * Elimina un género por ID.
     * 
     * @param id ID del género
     * @return true si se eliminó
     */
    public boolean delete(String id) {
        if (generos.remove(id) != null) {
            return persistir();
        }
        return false;
    }

    /**
     * Busca un género por ID.
     * 
     * @param id ID del género
     * @return Optional con el género o vacío
     */
    public Optional<Genero> findById(String id) {
        return Optional.ofNullable(generos.get(id));
    }

    /**
     * Busca un género por nombre.
     * 
     * @param nombre Nombre del género
     * @return Optional con el género o vacío
     */
    public Optional<Genero> findByNombre(String nombre) {
        return generos.values().stream()
                .filter(g -> g.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    /**
     * Retorna todos los géneros.
     * 
     * @return Colección de géneros
     */
    public Collection<Genero> findAll() {
        return new ArrayList<>(generos.values());
    }

    /**
     * Verifica si un género existe.
     * 
     * @param id ID del género
     * @return true si existe
     */
    public boolean exists(String id) {
        return generos.containsKey(id);
    }

    /**
     * Cuenta el número de géneros.
     * 
     * @return Número de géneros
     */
    public int count() {
        return generos.size();
    }

    /**
     * Persiste los géneros a disco.
     */
    private boolean persistir() {
        try {
            Files.createDirectories(GENEROS_FILE.getParent());
            String json = gson.toJson(generos.values());
            Files.writeString(GENEROS_FILE, json);
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar géneros: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga los géneros desde disco.
     */
    private void cargar() {
        if (!Files.exists(GENEROS_FILE)) {
            return;
        }

        try {
            String json = Files.readString(GENEROS_FILE);
            List<Genero> lista = gson.fromJson(json, new TypeToken<List<Genero>>(){}.getType());
            
            if (lista != null) {
                generos.clear();
                for (Genero g : lista) {
                    generos.put(g.getId(), g);
                }
                System.out.println("✓ Géneros cargados: " + generos.size());
            }
        } catch (IOException e) {
            System.err.println("Error al cargar géneros: " + e.getMessage());
        }
    }
}
