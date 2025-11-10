package org.dubytube.dubytube.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Usuario {
    private final String username;
    private String password;
    private String nombre;
    private final LinkedList<Cancion> favoritos = new LinkedList<>();

    public Usuario(String username, String password, String nombre) {
        this.username = Objects.requireNonNull(username);
        this.password = password;
        this.nombre = nombre;
    }

    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<Cancion> getFavoritos() { return favoritos; }
    public boolean addFavorito(Cancion c) { return c != null && !favoritos.contains(c) && favoritos.add(c); }
    public boolean removeFavorito(Cancion c) { return favoritos.remove(c); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        return username.equals(((Usuario) o).username);
    }
    @Override public int hashCode() { return username.hashCode(); }
}
