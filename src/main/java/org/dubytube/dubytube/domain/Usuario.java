package org.dubytube.dubytube.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Usuario {
    private String username;      // ← quitamos final
    private String password;
    private String nombre;

    private List<Cancion> favoritos;   // ← quitamos final

    private Role role;

    // Constructor vacío OBLIGATORIO para JSON
    public Usuario() {
        this.favoritos = new LinkedList<>();
        this.role = Role.USER;
    }

    public Usuario(String username, String password, String nombre) {
        this.username = Objects.requireNonNull(username);
        this.password = password;
        this.nombre = nombre;
        this.favoritos = new LinkedList<>();
        this.role = Role.USER;
    }

    // -------- Getters / Setters --------
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword(){ return password; }
    public void setPassword(String p){ this.password = p; }

    public Role getRole(){ return role; }
    public void setRole(Role r){ this.role = r; }

    public List<Cancion> getFavoritos() { return favoritos; }
    public void setFavoritos(List<Cancion> favoritos) { this.favoritos = favoritos; }

    // Métodos para favoritos
    public boolean addFavorito(Cancion c) {
        if (c == null) return false;
        boolean yaExiste = favoritos.stream().anyMatch(x -> x.getId().equals(c.getId()));
        return !yaExiste && favoritos.add(c);
    }

    public boolean removeFavorito(Cancion c) { return favoritos.remove(c); }

    public boolean removeFavoritoById(String id){
        if (id == null) return false;
        return favoritos.removeIf(x -> id.equals(x.getId()));
    }

    public boolean hasFavorito(String id){
        if (id == null) return false;
        return favoritos.stream().anyMatch(x -> id.equals(x.getId()));
    }

    // Igualdad por username
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        return username.equals(((Usuario) o).username);
    }

    @Override public int hashCode() { return username.hashCode(); }
}
