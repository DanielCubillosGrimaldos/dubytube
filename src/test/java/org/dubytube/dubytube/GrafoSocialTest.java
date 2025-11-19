package org.dubytube.dubytube;

import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.ds.GrafoSocial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el Grafo Social.
 * 
 * <p>Cubre los siguientes casos:</p>
 * <ul>
 *   <li>Agregar usuarios al grafo</li>
 *   <li>Crear amistades bidireccionales</li>
 *   <li>Verificar relaciones de amistad</li>
 *   <li>Eliminar amistades</li>
 *   <li>Encontrar amigos de amigos (BFS)</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
@DisplayName("Pruebas del Grafo Social")
class GrafoSocialTest {

    private GrafoSocial grafo;
    private Usuario usuario1;
    private Usuario usuario2;
    private Usuario usuario3;
    private Usuario usuario4;

    /**
     * Configuración inicial antes de cada prueba.
     * Crea usuarios de prueba.
     */
    @BeforeEach
    void setUp() {
        grafo = new GrafoSocial();
        
        usuario1 = new Usuario("user1", "pass1", "Usuario 1");
        usuario2 = new Usuario("user2", "pass2", "Usuario 2");
        usuario3 = new Usuario("user3", "pass3", "Usuario 3");
        usuario4 = new Usuario("user4", "pass4", "Usuario 4");
    }

    /**
     * RF-023: Verifica que se pueden agregar usuarios al grafo.
     */
    @Test
    @DisplayName("Agregar usuarios al grafo")
    void testAgregarUsuarios() {
        grafo.agregarUsuario(usuario1);
        grafo.agregarUsuario(usuario2);
        
        Set<Usuario> usuarios = grafo.getUsuarios();
        
        assertEquals(2, usuarios.size(), "Debe haber 2 usuarios en el grafo");
        assertTrue(usuarios.contains(usuario1), "El grafo debe contener a usuario1");
        assertTrue(usuarios.contains(usuario2), "El grafo debe contener a usuario2");
    }

    /**
     * RF-023: Verifica que se pueden crear amistades bidireccionales.
     */
    @Test
    @DisplayName("Crear amistad bidireccional")
    void testCrearAmistad() {
        grafo.agregarUsuario(usuario1);
        grafo.agregarUsuario(usuario2);
        
        boolean resultado = grafo.agregarAmistad(usuario1, usuario2);
        
        assertTrue(resultado, "La amistad debe crearse exitosamente");
        assertTrue(grafo.sonAmigos(usuario1, usuario2), "usuario1 y usuario2 deben ser amigos");
        assertTrue(grafo.sonAmigos(usuario2, usuario1), "La amistad debe ser bidireccional");
    }

    /**
     * RF-023: Verifica que se pueden obtener los amigos de un usuario.
     */
    @Test
    @DisplayName("Obtener amigos de un usuario")
    void testObtenerAmigos() {
        grafo.agregarUsuario(usuario1);
        grafo.agregarUsuario(usuario2);
        grafo.agregarUsuario(usuario3);
        
        grafo.agregarAmistad(usuario1, usuario2);
        grafo.agregarAmistad(usuario1, usuario3);
        
        Set<Usuario> amigos = grafo.getAmigos(usuario1);
        
        assertEquals(2, amigos.size(), "usuario1 debe tener 2 amigos");
        assertTrue(amigos.contains(usuario2), "usuario2 debe estar en la lista de amigos");
        assertTrue(amigos.contains(usuario3), "usuario3 debe estar en la lista de amigos");
    }

    /**
     * RF-023: Verifica que se pueden eliminar amistades.
     */
    @Test
    @DisplayName("Eliminar amistad")
    void testEliminarAmistad() {
        grafo.agregarUsuario(usuario1);
        grafo.agregarUsuario(usuario2);
        grafo.agregarAmistad(usuario1, usuario2);
        
        boolean resultado = grafo.eliminarAmistad(usuario1, usuario2);
        
        assertTrue(resultado, "La amistad debe eliminarse exitosamente");
        assertFalse(grafo.sonAmigos(usuario1, usuario2), "usuario1 y usuario2 ya no deben ser amigos");
    }

    /**
     * RF-024: Verifica el algoritmo BFS para encontrar amigos de amigos.
     */
    @Test
    @DisplayName("Encontrar amigos de amigos (BFS)")
    void testEncontrarAmigosDeAmigos() {
        // Crear estructura: usuario1 - usuario2 - usuario3
        //                              - usuario4
        grafo.agregarUsuario(usuario1);
        grafo.agregarUsuario(usuario2);
        grafo.agregarUsuario(usuario3);
        grafo.agregarUsuario(usuario4);
        
        grafo.agregarAmistad(usuario1, usuario2);
        grafo.agregarAmistad(usuario2, usuario3);
        grafo.agregarAmistad(usuario2, usuario4);
        
        Set<Usuario> sugerencias = grafo.encontrarAmigosDeAmigos(usuario1);
        
        assertEquals(2, sugerencias.size(), "usuario1 debe tener 2 sugerencias (amigos de amigos)");
        assertTrue(sugerencias.contains(usuario3), "usuario3 debe estar en las sugerencias");
        assertTrue(sugerencias.contains(usuario4), "usuario4 debe estar en las sugerencias");
        assertFalse(sugerencias.contains(usuario1), "usuario1 no debe estar en sus propias sugerencias");
        assertFalse(sugerencias.contains(usuario2), "usuario2 ya es amigo directo, no debe estar en sugerencias");
    }

    /**
     * RF-023: Verifica que no se puede crear amistad con uno mismo.
     */
    @Test
    @DisplayName("No se puede crear amistad consigo mismo")
    void testNoAmistadConsigoMismo() {
        grafo.agregarUsuario(usuario1);
        
        boolean resultado = grafo.agregarAmistad(usuario1, usuario1);
        
        assertFalse(resultado, "No debe poder crear amistad consigo mismo");
    }

    /**
     * RF-023: Verifica que agregar la misma amistad dos veces no duplica la relación.
     */
    @Test
    @DisplayName("Amistad duplicada no se agrega dos veces")
    void testAmistadDuplicada() {
        grafo.agregarUsuario(usuario1);
        grafo.agregarUsuario(usuario2);
        
        boolean primera = grafo.agregarAmistad(usuario1, usuario2);
        boolean segunda = grafo.agregarAmistad(usuario1, usuario2);
        
        assertTrue(primera, "La primera amistad debe crearse");
        assertFalse(segunda, "La segunda amistad no debe crearse (ya existe)");
        
        Set<Usuario> amigos = grafo.getAmigos(usuario1);
        assertEquals(1, amigos.size(), "usuario1 debe tener solo 1 amigo (sin duplicados)");
    }
}
