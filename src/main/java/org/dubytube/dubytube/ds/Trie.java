package org.dubytube.dubytube.ds;

import java.util.*;

/**
 * Implementación de un Árbol de Prefijos (Trie) para autocompletado eficiente.
 * Esta estructura se utiliza para búsquedas de canciones por título con prefijos.
 * 
 * <p>Características principales:</p>
 * <ul>
 *   <li>Inserción en O(m) donde m es la longitud de la palabra</li>
 *   <li>Búsqueda en O(m)</li>
 *   <li>Autocompletado eficiente con prefijos</li>
 *   <li>Case-insensitive (convierte a minúsculas)</li>
 *   <li>Soporta caracteres especiales y espacios</li>
 * </ul>
 * 
 * <p>Ejemplo de uso:</p>
 * <pre>
 * Trie trie = new Trie();
 * trie.insert("Bohemian Rhapsody");
 * trie.insert("Born to Run");
 * 
 * List&lt;String&gt; results = trie.searchByPrefix("bo");
 * // Retorna: ["bohemian rhapsody", "born to run"]
 * </pre>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class Trie {

    /**
     * Nodo interno del Trie.
     * Cada nodo representa un carácter y contiene referencias a sus hijos.
     */
    private static class TrieNode {
        /**
         * Mapa de caracteres a nodos hijos.
         * Usamos HashMap para soportar cualquier carácter Unicode.
         */
        Map<Character, TrieNode> children;
        
        /**
         * Indica si este nodo marca el final de una palabra válida.
         */
        boolean isEndOfWord;
        
        /**
         * Palabra completa almacenada en este nodo (solo si isEndOfWord = true).
         * Permite recuperar la palabra original sin reconstruirla.
         */
        String word;
        
        /**
         * Contador de palabras que pasan por este nodo.
         * Útil para estadísticas y ranking de búsquedas.
         */
        int frequency;

        /**
         * Constructor del nodo Trie.
         */
        TrieNode() {
            this.children = new HashMap<>();
            this.isEndOfWord = false;
            this.word = null;
            this.frequency = 0;
        }
    }

    private final TrieNode root;
    private int totalWords;

    /**
     * Constructor por defecto del Trie.
     * Inicializa la estructura con un nodo raíz vacío.
     */
    public Trie() {
        this.root = new TrieNode();
        this.totalWords = 0;
    }

    /**
     * Inserta una palabra en el Trie.
     * La palabra se convierte a minúsculas para búsquedas case-insensitive.
     * Complejidad: O(m) donde m es la longitud de la palabra
     * 
     * @param word Palabra a insertar
     * @throws IllegalArgumentException si la palabra es null o vacía
     */
    public void insert(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw new IllegalArgumentException("La palabra no puede ser null o vacía");
        }

        String normalizedWord = normalize(word);
        TrieNode current = root;

        // Recorrer cada carácter de la palabra
        for (char ch : normalizedWord.toCharArray()) {
            // Si el carácter no existe, crear nuevo nodo
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
            current.frequency++; // Incrementar frecuencia de paso
        }

        // Marcar el final de la palabra y guardar la original
        if (!current.isEndOfWord) {
            current.isEndOfWord = true;
            current.word = word; // Guardar palabra original (con mayúsculas/minúsculas)
            totalWords++;
        }
    }

    /**
     * Inserta múltiples palabras en el Trie.
     * Complejidad: O(n * m) donde n es el número de palabras y m el promedio de longitud
     * 
     * @param words Colección de palabras a insertar
     */
    public void insertAll(Collection<String> words) {
        if (words == null) {
            return;
        }
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                insert(word);
            }
        }
    }

    /**
     * Busca si una palabra existe en el Trie.
     * Complejidad: O(m) donde m es la longitud de la palabra
     * 
     * @param word Palabra a buscar
     * @return true si la palabra existe, false en caso contrario
     */
    public boolean search(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        TrieNode node = searchNode(normalize(word));
        return node != null && node.isEndOfWord;
    }

    /**
     * Busca si existe alguna palabra que comience con el prefijo dado.
     * Complejidad: O(m) donde m es la longitud del prefijo
     * 
     * @param prefix Prefijo a buscar
     * @return true si existe al menos una palabra con ese prefijo
     */
    public boolean startsWith(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return false;
        }

        return searchNode(normalize(prefix)) != null;
    }

    /**
     * Busca y retorna todas las palabras que comienzan con el prefijo dado.
     * Este es el método principal para el autocompletado.
     * Complejidad: O(m + k) donde m es la longitud del prefijo y k el número de resultados
     * 
     * @param prefix Prefijo a buscar
     * @return Lista de palabras que comienzan con el prefijo (ordenadas alfabéticamente)
     */
    public List<String> searchByPrefix(String prefix) {
        List<String> results = new ArrayList<>();

        if (prefix == null || prefix.trim().isEmpty()) {
            return results;
        }

        String normalizedPrefix = normalize(prefix);
        TrieNode node = searchNode(normalizedPrefix);

        if (node == null) {
            return results;
        }

        // Realizar DFS para encontrar todas las palabras
        collectAllWords(node, results);
        
        // Ordenar alfabéticamente
        Collections.sort(results);

        return results;
    }

    /**
     * Busca y retorna un máximo de N palabras que comienzan con el prefijo dado.
     * Útil para limitar resultados de autocompletado.
     * Complejidad: O(m + k) donde m es la longitud del prefijo y k = min(limit, total_results)
     * 
     * @param prefix Prefijo a buscar
     * @param limit Número máximo de resultados a retornar
     * @return Lista de palabras (máximo 'limit' elementos)
     */
    public List<String> searchByPrefix(String prefix, int limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }

        List<String> allResults = searchByPrefix(prefix);
        
        if (allResults.size() <= limit) {
            return allResults;
        }

        return allResults.subList(0, limit);
    }

    /**
     * Busca las palabras más populares que comienzan con el prefijo dado.
     * Ordena por frecuencia de búsqueda descendente.
     * 
     * @param prefix Prefijo a buscar
     * @param limit Número máximo de resultados
     * @return Lista de palabras ordenadas por popularidad
     */
    public List<String> searchByPrefixMostPopular(String prefix, int limit) {
        List<String> results = new ArrayList<>();

        if (prefix == null || prefix.trim().isEmpty() || limit <= 0) {
            return results;
        }

        String normalizedPrefix = normalize(prefix);
        TrieNode node = searchNode(normalizedPrefix);

        if (node == null) {
            return results;
        }

        // Recolectar palabras con sus frecuencias
        List<WordFrequency> wordFreqs = new ArrayList<>();
        collectAllWordsWithFrequency(node, wordFreqs);

        // Ordenar por frecuencia descendente
        wordFreqs.sort((a, b) -> Integer.compare(b.frequency, a.frequency));

        // Tomar los primeros 'limit' elementos
        int count = Math.min(limit, wordFreqs.size());
        for (int i = 0; i < count; i++) {
            results.add(wordFreqs.get(i).word);
        }

        return results;
    }

    /**
     * Elimina una palabra del Trie.
     * Complejidad: O(m) donde m es la longitud de la palabra
     * 
     * @param word Palabra a eliminar
     * @return true si la palabra fue eliminada, false si no existía
     */
    public boolean delete(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        String normalizedWord = normalize(word);
        return deleteHelper(root, normalizedWord, 0);
    }

    /**
     * Método auxiliar recursivo para eliminar una palabra.
     * 
     * @param current Nodo actual
     * @param word Palabra a eliminar
     * @param index Índice actual en la palabra
     * @return true si el nodo actual debe ser eliminado
     */
    private boolean deleteHelper(TrieNode current, String word, int index) {
        if (index == word.length()) {
            // Si no es fin de palabra, no hay nada que eliminar
            if (!current.isEndOfWord) {
                return false;
            }

            current.isEndOfWord = false;
            current.word = null;
            totalWords--;

            // Retornar true si el nodo no tiene hijos (se puede eliminar)
            return current.children.isEmpty();
        }

        char ch = word.charAt(index);
        TrieNode node = current.children.get(ch);

        if (node == null) {
            return false;
        }

        boolean shouldDeleteChild = deleteHelper(node, word, index + 1);

        if (shouldDeleteChild) {
            current.children.remove(ch);
            // Retornar true si el nodo actual no tiene hijos y no es fin de palabra
            return current.children.isEmpty() && !current.isEndOfWord;
        }

        return false;
    }

    /**
     * Retorna el número total de palabras almacenadas en el Trie.
     * Complejidad: O(1)
     * 
     * @return Cantidad de palabras
     */
    public int size() {
        return totalWords;
    }

    /**
     * Verifica si el Trie está vacío.
     * Complejidad: O(1)
     * 
     * @return true si no hay palabras almacenadas
     */
    public boolean isEmpty() {
        return totalWords == 0;
    }

    /**
     * Elimina todas las palabras del Trie.
     * Complejidad: O(1)
     */
    public void clear() {
        root.children.clear();
        totalWords = 0;
    }

    /**
     * Retorna todas las palabras almacenadas en el Trie.
     * Complejidad: O(n) donde n es el número total de palabras
     * 
     * @return Lista con todas las palabras (ordenadas alfabéticamente)
     */
    public List<String> getAllWords() {
        List<String> allWords = new ArrayList<>();
        collectAllWords(root, allWords);
        Collections.sort(allWords);
        return allWords;
    }

    /**
     * Cuenta cuántas palabras comienzan con el prefijo dado.
     * Complejidad: O(m + k) donde m es la longitud del prefijo y k el número de palabras
     * 
     * @param prefix Prefijo a contar
     * @return Cantidad de palabras con ese prefijo
     */
    public int countWordsWithPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return 0;
        }

        String normalizedPrefix = normalize(prefix);
        TrieNode node = searchNode(normalizedPrefix);

        if (node == null) {
            return 0;
        }

        return countWords(node);
    }

    /**
     * Cuenta recursivamente todas las palabras en un subárbol.
     * 
     * @param node Nodo raíz del subárbol
     * @return Cantidad de palabras
     */
    private int countWords(TrieNode node) {
        int count = node.isEndOfWord ? 1 : 0;

        for (TrieNode child : node.children.values()) {
            count += countWords(child);
        }

        return count;
    }

    /**
     * Busca el nodo correspondiente a una palabra o prefijo.
     * Método auxiliar interno.
     * 
     * @param word Palabra o prefijo a buscar
     * @return Nodo correspondiente, o null si no existe
     */
    private TrieNode searchNode(String word) {
        TrieNode current = root;

        for (char ch : word.toCharArray()) {
            TrieNode node = current.children.get(ch);
            if (node == null) {
                return null;
            }
            current = node;
        }

        return current;
    }

    /**
     * Recolecta todas las palabras en un subárbol mediante DFS.
     * Método auxiliar interno.
     * 
     * @param node Nodo raíz del subárbol
     * @param results Lista donde se agregan las palabras encontradas
     */
    private void collectAllWords(TrieNode node, List<String> results) {
        if (node == null) {
            return;
        }

        if (node.isEndOfWord) {
            results.add(node.word);
        }

        for (TrieNode child : node.children.values()) {
            collectAllWords(child, results);
        }
    }

    /**
     * Recolecta todas las palabras con sus frecuencias en un subárbol.
     * Método auxiliar interno.
     * 
     * @param node Nodo raíz del subárbol
     * @param results Lista donde se agregan las palabras con frecuencias
     */
    private void collectAllWordsWithFrequency(TrieNode node, List<WordFrequency> results) {
        if (node == null) {
            return;
        }

        if (node.isEndOfWord) {
            results.add(new WordFrequency(node.word, node.frequency));
        }

        for (TrieNode child : node.children.values()) {
            collectAllWordsWithFrequency(child, results);
        }
    }

    /**
     * Normaliza una cadena para búsqueda (convierte a minúsculas y elimina espacios extra).
     * 
     * @param str Cadena a normalizar
     * @return Cadena normalizada
     */
    private String normalize(String str) {
        return str.toLowerCase().trim();
    }

    /**
     * Retorna la palabra más larga almacenada en el Trie.
     * Complejidad: O(n) donde n es el número total de palabras
     * 
     * @return Palabra más larga, o null si el Trie está vacío
     */
    public String getLongestWord() {
        if (isEmpty()) {
            return null;
        }

        List<String> allWords = getAllWords();
        return allWords.stream()
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    /**
     * Retorna la palabra más corta almacenada en el Trie.
     * Complejidad: O(n) donde n es el número total de palabras
     * 
     * @return Palabra más corta, o null si el Trie está vacío
     */
    public String getShortestWord() {
        if (isEmpty()) {
            return null;
        }

        List<String> allWords = getAllWords();
        return allWords.stream()
                .min(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    /**
     * Clase auxiliar para almacenar palabras con su frecuencia.
     */
    private static class WordFrequency {
        String word;
        int frequency;

        WordFrequency(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }
    }

    /**
     * Genera estadísticas del Trie.
     * 
     * @return String con información sobre el Trie
     */
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Estadísticas del Trie ===\n");
        sb.append("Total de palabras: ").append(totalWords).append("\n");
        
        if (!isEmpty()) {
            sb.append("Palabra más larga: ").append(getLongestWord()).append("\n");
            sb.append("Palabra más corta: ").append(getShortestWord()).append("\n");
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "Trie vacío";
        }
        return "Trie con " + totalWords + " palabras";
    }
}
