package org.dubytube.dubytube;

import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.services.RecomendacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el Servicio de Recomendaciones.
 * 
 * <p>Cubre los siguientes casos:</p>
 * <ul>
 *   <li>Recomendaciones basadas en similitud de género</li>
 *   <li>Recomendaciones basadas en similitud de artista</li>
 *   <li>Exclusión de la canción fuente</li>
 *   <li>Ordenamiento por similitud</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
@DisplayName("Pruebas del Servicio de Recomendaciones")
class RecomendacionServiceTest {

    private RecomendacionService servicio;
    private CancionRepo repo;

    /**
     * Configuración inicial antes de cada prueba.
     * Crea un repositorio con canciones de prueba.
     */
    @BeforeEach
    void setUp() {
        repo = new CancionRepo();
        
        // Agregar canciones de prueba con similitudes conocidas
        repo.save(new Cancion("1", "Bohemian Rhapsody", "Queen", "Rock", 1975, 354));
        repo.save(new Cancion("2", "We Will Rock You", "Queen", "Rock", 1977, 122));
        repo.save(new Cancion("3", "Another One Bites the Dust", "Queen", "Rock", 1980, 215));
        repo.save(new Cancion("4", "Stairway to Heaven", "Led Zeppelin", "Rock", 1971, 482));
        repo.save(new Cancion("5", "Thriller", "Michael Jackson", "Pop", 1982, 357));
        repo.save(new Cancion("6", "Billie Jean", "Michael Jackson", "Pop", 1982, 294));
        
        servicio = new RecomendacionService(repo);
    }

    /**
     * RF-005: Verifica que las recomendaciones excluyen la canción fuente.
     */
    @Test
    @DisplayName("Recomendaciones no incluyen la canción fuente")
    void testRecomendacionesExcluyenFuente() {
        List<RecomendacionService.Rec> recomendaciones = servicio.recomendar("1", 5);
        
        assertFalse(recomendaciones.isEmpty(), "Debe haber recomendaciones");
        assertTrue(recomendaciones.stream().noneMatch(r -> r.cancion.getId().equals("1")),
                "Las recomendaciones no deben incluir la canción fuente");
    }

    /**
     * RF-005: Verifica recomendaciones por similitud de artista.
     */
    @Test
    @DisplayName("Recomendaciones priorizan mismo artista")
    void testRecomendacionesMismoArtista() {
        List<RecomendacionService.Rec> recomendaciones = servicio.recomendar("1", 3);
        
        // Las primeras recomendaciones deben ser de Queen (mismo artista)
        assertFalse(recomendaciones.isEmpty());
        
        long cancionesDeQueen = recomendaciones.stream()
                .filter(r -> "Queen".equals(r.cancion.getArtista()))
                .count();
        
        assertTrue(cancionesDeQueen >= 2, "Debe recomendar al menos 2 canciones de Queen");
    }

    /**
     * RF-005: Verifica que devuelve el número correcto de recomendaciones.
     */
    @Test
    @DisplayName("Devuelve el número correcto de recomendaciones")
    void testNumeroRecomendaciones() {
        List<RecomendacionService.Rec> recomendaciones = servicio.recomendar("1", 3);
        
        assertEquals(3, recomendaciones.size(), "Debe devolver exactamente 3 recomendaciones");
    }

    /**
     * RF-005: Verifica recomendaciones con ID inexistente.
     */
    @Test
    @DisplayName("Recomendaciones con ID inexistente devuelve lista vacía")
    void testRecomendacionesIDInexistente() {
        List<RecomendacionService.Rec> recomendaciones = servicio.recomendar("999", 5);
        
        assertTrue(recomendaciones.isEmpty(), "ID inexistente debe devolver lista vacía");
    }

    /**
     * RF-005: Verifica recomendaciones con ID null.
     */
    @Test
    @DisplayName("Recomendaciones con ID null devuelve lista vacía")
    void testRecomendacionesIDNull() {
        List<RecomendacionService.Rec> recomendaciones = servicio.recomendar(null, 5);
        
        assertTrue(recomendaciones.isEmpty(), "ID null debe devolver lista vacía");
    }

    /**
     * RF-005: Verifica que recomendaciones están ordenadas por similitud.
     */
    @Test
    @DisplayName("Recomendaciones ordenadas por similitud")
    void testRecomendacionesOrdenadas() {
        List<RecomendacionService.Rec> recomendaciones = servicio.recomendar("1", 4);
        
        assertFalse(recomendaciones.isEmpty(), "Debe haber recomendaciones");
        
        // Verificar que las distancias están en orden ascendente (menor distancia = más similar)
        for (int i = 0; i < recomendaciones.size() - 1; i++) {
            double dist1 = recomendaciones.get(i).distancia;
            double dist2 = recomendaciones.get(i + 1).distancia;
            
            assertTrue(dist1 <= dist2 || Double.isNaN(dist1) || Double.isNaN(dist2),
                    "Las recomendaciones deben estar ordenadas por similitud (distancia ascendente)");
        }
    }

    /**
     * RF-005: Verifica que recomendaciones consideran género similar.
     */
    @Test
    @DisplayName("Recomendaciones consideran género similar")
    void testRecomendacionesGeneroSimilar() {
        List<RecomendacionService.Rec> recomendaciones = servicio.recomendar("5", 2);
        
        assertFalse(recomendaciones.isEmpty());
        
        // Al menos una recomendación debe ser del mismo género (Pop)
        boolean hayPopEnRecomendaciones = recomendaciones.stream()
                .anyMatch(r -> "Pop".equals(r.cancion.getGenero()));
        
        assertTrue(hayPopEnRecomendaciones, "Debe recomendar canciones del mismo género");
    }
}
