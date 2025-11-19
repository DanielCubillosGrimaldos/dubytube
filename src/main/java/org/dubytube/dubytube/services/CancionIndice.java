package org.dubytube.dubytube.services;

import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.ds.Trie;
import org.dubytube.dubytube.repo.CancionRepo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de indexación y autocompletado de canciones usando Trie.
 * 
 * <p>Requisitos cumplidos:</p>
 * <ul>
 *   <li>RF-003: Búsqueda por autocompletado de título usando Trie</li>
 *   <li>RF-025: Implementación de Árbol de Prefijos (Trie)</li>
 *   <li>RF-026: Devuelve todas las palabras que comienzan con un prefijo dado</li>
 * </ul>
 * 
 * <p>Este servicio mantiene un índice invertido de títulos de canciones
 * para permitir búsquedas rápidas por prefijo en O(m + k) donde m es la
 * longitud del prefijo y k el número de resultados.</p>
 * 
 * @author DubyTube Team
 * @version 2.0
 * @since 2025-11-18
 */
public class CancionIndice {
    private final CancionRepo repo;
    
    /**
     * Trie para indexación de títulos de canciones.
     * Permite autocompletado eficiente en O(m + k).
     */
    private final Trie trieTitulos;
    
    /**
     * Mapa para asociar títulos normalizados con objetos Cancion.
     * Permite recuperar la canción completa desde el resultado del Trie.
     * Key: título normalizado (lowercase), Value: Cancion
     */
    private final Map<String, Cancion> tituloToCancion;

    /**
     * Constructor del servicio de indexación.
     * 
     * @param repo Repositorio de canciones
     */
    public CancionIndice(CancionRepo repo) {
        this.repo = repo;
        this.trieTitulos = new Trie();
        this.tituloToCancion = new HashMap<>();
    }

    /**
     * Indexa todas las canciones existentes en el repositorio.
     * Este método debe ser llamado al iniciar la aplicación.
     * Complejidad: O(n * m) donde n es el número de canciones y m el promedio de longitud de títulos
     */
    public void indexarExistentes() {
        Collection<Cancion> todasCanciones = repo.findAll();
        
        for (Cancion c : todasCanciones) {
            indexarCancion(c);
        }
        
        System.out.println("✓ Indexadas " + trieTitulos.size() + " canciones en el Trie");
    }

    /**
     * Indexa una canción individual en el Trie.
     * Método auxiliar privado.
     * 
     * @param c Canción a indexar
     */
    private void indexarCancion(Cancion c) {
        if (c == null || c.getTitulo() == null || c.getTitulo().trim().isEmpty()) {
            return;
        }
        
        String titulo = c.getTitulo();
        String tituloNormalizado = titulo.toLowerCase().trim();
        
        // Insertar en el Trie
        trieTitulos.insert(titulo);
        
        // Guardar referencia a la canción
        tituloToCancion.put(tituloNormalizado, c);
    }

    /**
     * Registra una nueva canción en el repositorio y la indexa.
     * Complejidad: O(m) donde m es la longitud del título
     * 
     * @param c Canción a registrar
     */
    public void registrarCancion(Cancion c) {
        if (c == null) {
            return;
        }
        
        repo.save(c);
        indexarCancion(c);
    }

    /**
     * Elimina una canción del índice y del repositorio.
     * Complejidad: O(m) donde m es la longitud del título
     * 
     * @param c Canción a eliminar
     * @return true si se eliminó exitosamente
     */
    public boolean eliminarCancion(Cancion c) {
        if (c == null) {
            return false;
        }
        
        boolean eliminadaRepo = repo.delete(c.getId());
        
        if (eliminadaRepo) {
            String tituloNormalizado = c.getTitulo().toLowerCase().trim();
            trieTitulos.delete(c.getTitulo());
            tituloToCancion.remove(tituloNormalizado);
        }
        
        return eliminadaRepo;
    }

    /**
     * Sugiere canciones cuyo título comience con el prefijo dado.
     * Implementación principal del autocompletado (RF-003).
     * Complejidad: O(m + k) donde m es la longitud del prefijo y k el número de resultados
     * 
     * @param prefijo Prefijo a buscar (case-insensitive)
     * @param k Número máximo de sugerencias a retornar
     * @return Lista de canciones sugeridas (máximo k elementos)
     */
    public List<Cancion> sugerirPorTitulo(String prefijo, int k) {
        if (prefijo == null || prefijo.trim().isEmpty() || k <= 0) {
            return new ArrayList<>();
        }

        // Buscar títulos que coincidan con el prefijo
        List<String> titulosCoincidentes = trieTitulos.searchByPrefix(prefijo, k);
        
        // Convertir títulos a objetos Cancion
        return titulosCoincidentes.stream()
                .map(titulo -> tituloToCancion.get(titulo.toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Sobrecarga: Sugiere canciones sin límite de resultados.
     * 
     * @param prefijo Prefijo a buscar
     * @return Lista de todas las canciones que coincidan
     */
    public List<Cancion> sugerirPorTitulo(String prefijo) {
        if (prefijo == null || prefijo.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> titulosCoincidentes = trieTitulos.searchByPrefix(prefijo);
        
        return titulosCoincidentes.stream()
                .map(titulo -> tituloToCancion.get(titulo.toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Busca una canción exacta por título.
     * Complejidad: O(m) donde m es la longitud del título
     * 
     * @param titulo Título exacto a buscar
     * @return Canción si existe, null en caso contrario
     */
    public Cancion buscarPorTituloExacto(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return null;
        }

        if (!trieTitulos.search(titulo)) {
            return null;
        }

        return tituloToCancion.get(titulo.toLowerCase().trim());
    }

    /**
     * Cuenta cuántas canciones tienen títulos que comienzan con el prefijo.
     * Complejidad: O(m + k) donde m es la longitud del prefijo y k el número de coincidencias
     * 
     * @param prefijo Prefijo a contar
     * @return Cantidad de canciones con ese prefijo
     */
    public int contarCancionesPorPrefijo(String prefijo) {
        return trieTitulos.countWordsWithPrefix(prefijo);
    }

    /**
     * Re-indexa completamente el Trie.
     * Útil después de cambios masivos en el repositorio.
     * Complejidad: O(n * m) donde n es el número de canciones
     */
    public void reindexar() {
        trieTitulos.clear();
        tituloToCancion.clear();
        indexarExistentes();
    }

    /**
     * Obtiene estadísticas del índice.
     * 
     * @return String con información sobre el estado del índice
     */
    public String getEstadisticas() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Estado del Índice de Canciones ===\n");
        sb.append("Canciones indexadas: ").append(trieTitulos.size()).append("\n");
        sb.append("Canciones en repositorio: ").append(repo.findAll().size()).append("\n");
        
        if (!trieTitulos.isEmpty()) {
            sb.append("Título más largo: ").append(trieTitulos.getLongestWord()).append("\n");
            sb.append("Título más corto: ").append(trieTitulos.getShortestWord()).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * Obtiene el número total de canciones indexadas.
     * 
     * @return Cantidad de canciones en el índice
     */
    public int size() {
        return trieTitulos.size();
    }

    /**
     * Verifica si el índice está vacío.
     * 
     * @return true si no hay canciones indexadas
     */
    public boolean isEmpty() {
        return trieTitulos.isEmpty();
    }
}

