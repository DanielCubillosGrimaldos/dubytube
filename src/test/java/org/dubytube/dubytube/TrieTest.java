package org.dubytube.dubytube;

import org.dubytube.dubytube.ds.Trie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el Trie (Árbol de Prefijos).
 * 
 * <p>Cubre los siguientes casos:</p>
 * <ul>
 *   <li>Inserción de palabras</li>
 *   <li>Búsqueda exacta de palabras</li>
 *   <li>Autocompletado por prefijo</li>
 *   <li>Conteo de palabras</li>
 *   <li>Manejo de palabras duplicadas</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
@DisplayName("Pruebas del Trie (Árbol de Prefijos)")
class TrieTest {

    private Trie trie;

    /**
     * Configuración inicial antes de cada prueba.
     * Crea un Trie vacío.
     */
    @BeforeEach
    void setUp() {
        trie = new Trie();
    }

    /**
     * RF-025: Verifica que se pueden insertar palabras en el Trie.
     */
    @Test
    @DisplayName("Insertar palabras en el Trie")
    void testInsertarPalabras() {
        trie.insert("Bohemian Rhapsody");
        trie.insert("Born to Run");
        trie.insert("Billie Jean");
        
        assertEquals(3, trie.size(), "Debe haber 3 palabras en el Trie");
    }

    /**
     * RF-025: Verifica que se puede buscar una palabra exacta.
     */
    @Test
    @DisplayName("Búsqueda exacta de palabra")
    void testBusquedaExacta() {
        trie.insert("Bohemian Rhapsody");
        
        assertTrue(trie.search("Bohemian Rhapsody"), "Debe encontrar la palabra exacta");
        assertTrue(trie.search("bohemian rhapsody"), "Debe ser case-insensitive");
        assertFalse(trie.search("Bohemian"), "No debe encontrar prefijos como palabra completa");
    }

    /**
     * RF-026: Verifica el autocompletado por prefijo.
     */
    @Test
    @DisplayName("Autocompletado por prefijo")
    void testAutocompletadoPorPrefijo() {
        trie.insert("Bohemian Rhapsody");
        trie.insert("Born to Run");
        trie.insert("Billie Jean");
        trie.insert("Beat It");
        
        List<String> resultados = trie.searchByPrefix("bo");
        
        assertEquals(2, resultados.size(), "Debe encontrar 2 palabras que empiezan con 'bo'");
        assertTrue(resultados.stream().anyMatch(s -> s.toLowerCase().contains("bohemian")),
                "Debe incluir 'Bohemian Rhapsody'");
        assertTrue(resultados.stream().anyMatch(s -> s.toLowerCase().contains("born")),
                "Debe incluir 'Born to Run'");
    }

    /**
     * RF-026: Verifica que prefijo vacío devuelve lista vacía (comportamiento por diseño).
     */
    @Test
    @DisplayName("Prefijo vacío devuelve lista vacía")
    void testPrefijoVacio() {
        trie.insert("Bohemian Rhapsody");
        trie.insert("Born to Run");
        trie.insert("Billie Jean");
        
        List<String> resultados = trie.searchByPrefix("");
        
        assertTrue(resultados.isEmpty(), "Prefijo vacío debe devolver lista vacía (requiere al menos 1 carácter)");
    }

    /**
     * RF-025: Verifica que palabras duplicadas no incrementan el tamaño.
     */
    @Test
    @DisplayName("Palabras duplicadas no se cuentan dos veces")
    void testPalabrasDuplicadas() {
        trie.insert("Bohemian Rhapsody");
        trie.insert("Bohemian Rhapsody"); // Duplicada
        
        assertEquals(1, trie.size(), "Palabras duplicadas no deben incrementar el tamaño");
    }

    /**
     * RF-025: Verifica el manejo de palabras con caracteres especiales.
     */
    @Test
    @DisplayName("Soporte para caracteres especiales")
    void testCaracteresEspeciales() {
        trie.insert("Don't Stop Believin'");
        trie.insert("Smells Like Teen Spirit");
        
        assertTrue(trie.search("Don't Stop Believin'"), "Debe soportar apóstrofes");
        assertTrue(trie.search("Smells Like Teen Spirit"), "Debe soportar espacios");
    }

    /**
     * RF-026: Verifica que prefijo sin coincidencias devuelve lista vacía.
     */
    @Test
    @DisplayName("Prefijo sin coincidencias devuelve lista vacía")
    void testPrefijoSinCoincidencias() {
        trie.insert("Bohemian Rhapsody");
        trie.insert("Born to Run");
        
        List<String> resultados = trie.searchByPrefix("xyz");
        
        assertTrue(resultados.isEmpty(), "Prefijo sin coincidencias debe devolver lista vacía");
    }

    /**
     * RF-025: Verifica que no se puede insertar palabra null o vacía.
     */
    @Test
    @DisplayName("No se puede insertar palabra null o vacía")
    void testInsertarPalabraNulaOVacia() {
        assertThrows(IllegalArgumentException.class, () -> trie.insert(null),
                "Insertar null debe lanzar IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> trie.insert(""),
                "Insertar cadena vacía debe lanzar IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> trie.insert("   "),
                "Insertar solo espacios debe lanzar IllegalArgumentException");
    }

    /**
     * RF-026: Verifica autocompletado case-insensitive.
     */
    @Test
    @DisplayName("Autocompletado es case-insensitive")
    void testAutocompletadoCaseInsensitive() {
        trie.insert("Bohemian Rhapsody");
        
        List<String> resultados1 = trie.searchByPrefix("BOH");
        List<String> resultados2 = trie.searchByPrefix("boh");
        List<String> resultados3 = trie.searchByPrefix("Boh");
        
        assertEquals(1, resultados1.size(), "Debe encontrar con prefijo en mayúsculas");
        assertEquals(1, resultados2.size(), "Debe encontrar con prefijo en minúsculas");
        assertEquals(1, resultados3.size(), "Debe encontrar con prefijo mixto");
    }
}
