package org.dubytube.dubytube.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Usuario {
    private final String username;
    private String password;
    private String nombre;

    // Lista de favoritos del usuario
    private final LinkedList<Cancion> favoritos = new LinkedList<>();

    // Rol (USER por defecto)
    private Role role = Role.USER;

    public Usuario(String username, String password, String nombre) {
        this.username = Objects.requireNonNull(username);
        this.password = password;
        this.nombre = nombre;
    }

    // -------- Getters / Setters básicos --------
    public String getUsername() { return username; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword(){ return password; }
    public void setPassword(String p){ this.password = p; }

    public Role getRole(){ return role; }
    public void setRole(Role r){ this.role = r; }

    // -------- Favoritos --------
    // Devuelve la lista mutables de favoritos (si prefieres inmutable, cambia el tipo de retorno)
    public List<Cancion> getFavoritos() { return favoritos; }

    // Añade evitando duplicados por ID de canción
    public boolean addFavorito(Cancion c) {
        if (c == null) return false;
        boolean yaExiste = favoritos.stream().anyMatch(x -> x.getId().equals(c.getId()));
        return !yaExiste && favoritos.add(c);
    }

    // Elimina por instancia (tu método original)
    public boolean removeFavorito(Cancion c) { return favoritos.remove(c); }

    // Elimina por ID (útil para botones en tablas)
    public boolean removeFavoritoById(String id){
        if (id == null) return false;
        return favoritos.removeIf(x -> id.equals(x.getId()));
    }

    // Consulta rápida para deshabilitar el botón "Añadir ♥" si ya está
    public boolean hasFavorito(String id){
        if (id == null) return false;
        return favoritos.stream().anyMatch(x -> id.equals(x.getId()));
    }

    // -------- Igualdad por username --------
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        return username.equals(((Usuario) o).username);
    }
    @Override public int hashCode() { return username.hashCode(); }
}
