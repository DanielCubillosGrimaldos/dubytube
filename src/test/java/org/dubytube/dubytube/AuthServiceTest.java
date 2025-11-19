package org.dubytube.dubytube;

import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.UsuarioRepo;
import org.dubytube.dubytube.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio de autenticación.
 * 
 * <p>Cubre los siguientes casos:</p>
 * <ul>
 *   <li>Login exitoso con credenciales correctas</li>
 *   <li>Login fallido con contraseña incorrecta</li>
 *   <li>Login fallido con usuario inexistente</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
@DisplayName("Pruebas del Servicio de Autenticación")
class AuthServiceTest {

    private AuthService authService;
    private UsuarioRepo usuarioRepo;

    /**
     * Configuración inicial antes de cada prueba.
     * Crea un repositorio con usuarios de prueba.
     */
    @BeforeEach
    void setUp() {
        usuarioRepo = new UsuarioRepo();
        
        // Crear usuarios de prueba
        Usuario admin = new Usuario("admin", "admin123", "Administrador");
        admin.setRole(Role.ADMIN);
        usuarioRepo.save(admin);
        
        Usuario user = new Usuario("user1", "pass123", "Usuario Normal");
        user.setRole(Role.USER);
        usuarioRepo.save(user);
        
        authService = new AuthService(usuarioRepo);
    }

    /**
     * RF-001: Verifica que el login funciona con credenciales correctas.
     */
    @Test
    @DisplayName("Login exitoso con credenciales correctas")
    void testLoginExitoso() {
        Optional<Usuario> resultado = authService.login("admin", "admin123");
        
        assertTrue(resultado.isPresent(), "El login debe ser exitoso con credenciales correctas");
        assertEquals("admin", resultado.get().getUsername(), "El username debe coincidir");
        assertEquals(Role.ADMIN, resultado.get().getRole(), "El rol debe ser ADMIN");
    }

    /**
     * RF-001: Verifica que el login falla con contraseña incorrecta.
     */
    @Test
    @DisplayName("Login fallido con contraseña incorrecta")
    void testLoginConPasswordIncorrecta() {
        Optional<Usuario> resultado = authService.login("admin", "wrongpassword");
        
        assertFalse(resultado.isPresent(), "El login debe fallar con contraseña incorrecta");
    }

    /**
     * RF-001: Verifica que el login falla con usuario inexistente.
     */
    @Test
    @DisplayName("Login fallido con usuario inexistente")
    void testLoginConUsuarioInexistente() {
        Optional<Usuario> resultado = authService.login("noexiste", "password");
        
        assertFalse(resultado.isPresent(), "El login debe fallar con usuario inexistente");
    }

    /**
     * RF-001: Verifica que el login funciona para usuario normal.
     */
    @Test
    @DisplayName("Login exitoso para usuario normal")
    void testLoginUsuarioNormal() {
        Optional<Usuario> resultado = authService.login("user1", "pass123");
        
        assertTrue(resultado.isPresent(), "El login debe ser exitoso");
        assertEquals("user1", resultado.get().getUsername());
        assertEquals(Role.USER, resultado.get().getRole(), "El rol debe ser USER");
    }
}
