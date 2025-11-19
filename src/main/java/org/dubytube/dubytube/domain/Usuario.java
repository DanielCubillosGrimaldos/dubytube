package org.dubytube.dubytube.domain;

import org.dubytube.dubytube.ds.MyLinkedList;
import java.util.Objects;

/**
 * Entidad que representa a un usuario de la plataforma DubyTube.
 * 
 * <p>Requisitos cumplidos:</p>
 * <ul>
 *   <li>RF-015: Almacena username (único), password, nombre, y lista de favoritos usando LinkedList personalizada</li>
 *   <li>RF-016: Es indexado en HashMap para acceso O(1)</li>
 *   <li>RF-017: Implementa hashCode() y equals() basado en username</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 2.0
 * @since 2025-11-18
 */
public class Usuario {
    private String username;      // Username único del usuario
    private String password;       // Contraseña del usuario
    private String nombre;         // Nombre completo del usuario

    /**
     * Lista de canciones favoritas usando implementación personalizada de LinkedList.
     * Permite inserción/eliminación eficiente en O(1) al inicio/final.
     */
    private MyLinkedList<Cancion> favoritos;

    private Role role;            // Rol del usuario (USER o ADMIN)

    /**
     * Constructor vacío OBLIGATORIO para deserialización JSON.
     * Inicializa la lista de favoritos vacía y rol USER por defecto.
     */
    public Usuario() {
        this.favoritos = new MyLinkedList<>();
        this.role = Role.USER;
    }

    /**
     * Constructor completo del usuario.
     * 
     * @param username Username único (no puede ser null)
     * @param password Contraseña del usuario
     * @param nombre Nombre completo del usuario
     * @throws NullPointerException si username es null
     */
    public Usuario(String username, String password, String nombre) {
        this.username = Objects.requireNonNull(username, "Username no puede ser null");
        this.password = password;
        this.nombre = nombre;
        this.favoritos = new MyLinkedList<>();
        this.role = Role.USER;
    }

    // -------- Getters / Setters --------
    
    /**
     * Obtiene el username del usuario.
     * @return Username único del usuario
     */
    public String getUsername() { 
        return username; 
    }
    
    /**
     * Establece el username del usuario.
     * @param username Nuevo username
     */
    public void setUsername(String username) { 
        this.username = username; 
    }

    /**
     * Obtiene el nombre completo del usuario.
     * @return Nombre del usuario
     */
    public String getNombre() { 
        return nombre; 
    }
    
    /**
     * Establece el nombre del usuario.
     * @param nombre Nuevo nombre
     */
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }

    /**
     * Obtiene la contraseña del usuario.
     * @return Contraseña
     */
    public String getPassword() { 
        return password; 
    }
    
    /**
     * Establece la contraseña del usuario.
     * @param p Nueva contraseña
     */
    public void setPassword(String p) { 
        this.password = p; 
    }

    /**
     * Obtiene el rol del usuario.
     * @return Rol (USER o ADMIN)
     */
    public Role getRole() { 
        return role; 
    }
    
    /**
     * Establece el rol del usuario.
     * @param r Nuevo rol
     */
    public void setRole(Role r) { 
        this.role = r; 
    }

    /**
     * Obtiene la lista de canciones favoritas.
     * @return LinkedList personalizada con los favoritos
     */
    public MyLinkedList<Cancion> getFavoritos() { 
        return favoritos; 
    }
    
    /**
     * Establece la lista de favoritos.
     * @param favoritos Nueva lista de favoritos
     */
    public void setFavoritos(MyLinkedList<Cancion> favoritos) { 
        this.favoritos = favoritos; 
    }

    // -------- Métodos para gestión de favoritos --------
    
    /**
     * Agrega una canción a la lista de favoritos.
     * No permite duplicados (basado en el ID de la canción).
     * Complejidad: O(n) por la verificación de existencia
     * 
     * @param c Canción a agregar
     * @return true si se agregó exitosamente, false si era null o ya existía
     */
    public boolean addFavorito(Cancion c) {
        if (c == null) return false;
        
        // Verificar si ya existe
        for (Cancion fav : favoritos) {
            if (fav.getId().equals(c.getId())) {
                return false; // Ya existe
            }
        }
        
        return favoritos.add(c);
    }

    /**
     * Elimina una canción de la lista de favoritos.
     * Complejidad: O(n)
     * 
     * @param c Canción a eliminar
     * @return true si se eliminó exitosamente, false si no existía
     */
    public boolean removeFavorito(Cancion c) { 
        return favoritos.remove(c); 
    }

    /**
     * Elimina una canción de favoritos por su ID.
     * Complejidad: O(n)
     * 
     * @param id ID de la canción a eliminar
     * @return true si se eliminó exitosamente, false si no existía
     */
    public boolean removeFavoritoById(String id) {
        if (id == null) return false;
        
        // Buscar y eliminar por ID
        for (Cancion fav : favoritos) {
            if (id.equals(fav.getId())) {
                return favoritos.remove(fav);
            }
        }
        
        return false;
    }

    /**
     * Verifica si una canción está en favoritos (por ID).
     * Complejidad: O(n)
     * 
     * @param id ID de la canción a buscar
     * @return true si la canción está en favoritos
     */
    public boolean hasFavorito(String id) {
        if (id == null) return false;
        
        for (Cancion fav : favoritos) {
            if (id.equals(fav.getId())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Obtiene el número de canciones favoritas.
     * Complejidad: O(1)
     * 
     * @return Cantidad de favoritos
     */
    public int getFavoritosCount() {
        return favoritos.size();
    }

    /**
     * Limpia toda la lista de favoritos.
     * Complejidad: O(1)
     */
    public void clearFavoritos() {
        favoritos.clear();
    }

    // -------- Igualdad y Hash basados en username (RF-017) --------
    
    /**
     * Compara dos usuarios por su username.
     * Cumple RF-017: equals() basado en username
     * 
     * @param o Objeto a comparar
     * @return true si tienen el mismo username
     */
    @Override 
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario other = (Usuario) o;
        return username != null && username.equals(other.username);
    }

    /**
     * Calcula el hash basado en el username.
     * Cumple RF-017: hashCode() basado en username
     * 
     * @return Hash del username
     */
    @Override 
    public int hashCode() { 
        return username != null ? username.hashCode() : 0; 
    }

    /**
     * Representación en String del usuario.
     * 
     * @return String con información básica del usuario
     */
    @Override
    public String toString() {
        return "Usuario{" +
                "username='" + username + '\'' +
                ", nombre='" + nombre + '\'' +
                ", role=" + role +
                ", favoritos=" + favoritos.size() +
                '}';
    }
}
