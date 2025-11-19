package org.dubytube.dubytube;

import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.BusquedaAvanzada;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la Búsqueda Avanzada concurrente.
 * 
 * <p>Cubre los siguientes casos:</p>
 * <ul>
 *   <li>Búsqueda por artista</li>
 *   <li>Búsqueda por género</li>
 *   <li>Búsqueda por rango de años</li>
 *   <li>Búsqueda combinada con lógica AND</li>
 *   <li>Búsqueda combinada con lógica OR</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
@DisplayName("Pruebas de Búsqueda Avanzada")
class BusquedaAvanzadaTest {

    private BusquedaAvanzada busqueda;
    private CancionRepo repo;

    /**
     * Configuración inicial antes de cada prueba.
     * Crea un repositorio con canciones de prueba.
     */
    @BeforeEach
    void setUp() {
        repo = new CancionRepo();
        
        // Limpiar repositorio (eliminar canciones precargadas)
        List<String> idsParaEliminar = new ArrayList<>();
        repo.findAll().forEach(c -> idsParaEliminar.add(c.getId()));
        idsParaEliminar.forEach(id -> repo.delete(id));
        
        // Agregar canciones de prueba
        repo.save(new Cancion("test1", "Bohemian Rhapsody", "Queen", "Rock", 1975, 354));
        repo.save(new Cancion("test2", "Imagine", "John Lennon", "Rock", 1971, 183));
        repo.save(new Cancion("test3", "Thriller", "Michael Jackson", "Pop", 1982, 357));
        repo.save(new Cancion("test4", "Billie Jean", "Michael Jackson", "Pop", 1982, 294));
        repo.save(new Cancion("test5", "Hotel California", "Eagles", "Rock", 1976, 391));
        repo.save(new Cancion("test6", "Take Five", "Dave Brubeck", "Jazz", 1959, 324));
        
        busqueda = new BusquedaAvanzada(repo);
    }

    /**
     * Limpia recursos después de cada prueba.
     */
    @AfterEach
    void tearDown() {
        busqueda.shutdown();
    }

    /**
     * RF-004: Verifica la búsqueda por artista.
     */
    @Test
    @DisplayName("Búsqueda por artista")
    void testBusquedaPorArtista() {
        List<Cancion> resultado = busqueda.buscar("Michael Jackson", null, null, null, BusquedaAvanzada.Logica.AND);
        
        assertEquals(2, resultado.size(), "Debe encontrar 2 canciones de Michael Jackson");
        assertTrue(resultado.stream().allMatch(c -> c.getArtista().equalsIgnoreCase("Michael Jackson")),
                "Todas las canciones deben ser de Michael Jackson");
    }

    /**
     * RF-004: Verifica la búsqueda por género.
     */
    @Test
    @DisplayName("Búsqueda por género")
    void testBusquedaPorGenero() {
        List<Cancion> resultado = busqueda.buscar(null, "Rock", null, null, BusquedaAvanzada.Logica.AND);
        
        assertEquals(3, resultado.size(), "Debe encontrar 3 canciones de Rock");
        assertTrue(resultado.stream().allMatch(c -> c.getGenero().equalsIgnoreCase("Rock")),
                "Todas las canciones deben ser de género Rock");
    }

    /**
     * RF-004: Verifica la búsqueda por rango de años.
     */
    @Test
    @DisplayName("Búsqueda por rango de años")
    void testBusquedaPorRangoAnios() {
        List<Cancion> resultado = busqueda.buscar(null, null, 1980, 1985, BusquedaAvanzada.Logica.AND);
        
        assertEquals(2, resultado.size(), "Debe encontrar 2 canciones entre 1980 y 1985");
        assertTrue(resultado.stream().allMatch(c -> c.getAnio() >= 1980 && c.getAnio() <= 1985),
                "Todas las canciones deben estar en el rango 1980-1985");
    }

    /**
     * RF-004: Verifica la búsqueda combinada con lógica AND.
     */
    @Test
    @DisplayName("Búsqueda combinada con AND")
    void testBusquedaCombinandaAND() {
        List<Cancion> resultado = busqueda.buscar("Michael Jackson", "Pop", null, null, BusquedaAvanzada.Logica.AND);
        
        assertEquals(2, resultado.size(), "Debe encontrar 2 canciones de Michael Jackson que sean Pop");
        assertTrue(resultado.stream().allMatch(c -> 
                c.getArtista().equalsIgnoreCase("Michael Jackson") && 
                c.getGenero().equalsIgnoreCase("Pop")),
                "Todas las canciones deben cumplir ambos criterios");
    }

    /**
     * RF-004: Verifica la búsqueda combinada con lógica OR.
     */
    @Test
    @DisplayName("Búsqueda combinada con OR")
    void testBusquedaCombinandaOR() {
        List<Cancion> resultado = busqueda.buscar(null, "Jazz", null, 1959, BusquedaAvanzada.Logica.OR);
        
        // Debe encontrar canciones de Jazz O del año 1959
        assertFalse(resultado.isEmpty(), "Debe encontrar al menos una canción");
        assertTrue(resultado.stream().anyMatch(c -> c.getGenero().equalsIgnoreCase("Jazz") || c.getAnio() == 1959),
                "Al menos una canción debe cumplir alguno de los criterios");
    }

    /**
     * RF-004: Verifica que búsqueda sin filtros devuelve todas las canciones.
     */
    @Test
    @DisplayName("Búsqueda sin filtros devuelve todo")
    void testBusquedaSinFiltros() {
        List<Cancion> resultado = busqueda.buscar(null, null, null, null, BusquedaAvanzada.Logica.AND);
        
        assertEquals(6, resultado.size(), "Debe devolver todas las canciones cuando no hay filtros");
    }

    /**
     * RF-004: Verifica búsqueda con criterio parcial (substring).
     */
    @Test
    @DisplayName("Búsqueda con substring en artista")
    void testBusquedaSubstring() {
        List<Cancion> resultado = busqueda.buscar("Jack", null, null, null, BusquedaAvanzada.Logica.AND);
        
        assertEquals(2, resultado.size(), "Debe encontrar canciones cuyo artista contiene 'Jack'");
        assertTrue(resultado.stream().allMatch(c -> c.getArtista().toLowerCase().contains("jack")),
                "Todos los artistas deben contener 'jack'");
    }

    /**
     * RF-004: Verifica búsqueda con rango de años solo mínimo.
     */
    @Test
    @DisplayName("Búsqueda con año mínimo solamente")
    void testBusquedaAnioMinimo() {
        List<Cancion> resultado = busqueda.buscar(null, null, 1980, null, BusquedaAvanzada.Logica.AND);
        
        assertFalse(resultado.isEmpty(), "Debe encontrar canciones de 1980 en adelante");
        assertTrue(resultado.stream().allMatch(c -> c.getAnio() >= 1980),
                "Todas las canciones deben ser de 1980 o posterior");
    }
}
