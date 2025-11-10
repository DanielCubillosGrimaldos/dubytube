package org.dubytube.dubytube.repo;

import org.dubytube.dubytube.domain.Usuario;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UsuarioRepo {
    private final Map<String, Usuario> idx = new HashMap<>();
    public Optional<Usuario> find(String username) { return Optional.ofNullable(idx.get(username)); }
    public boolean exists(String username) { return idx.containsKey(username); }
    public Usuario save(Usuario u) { idx.put(u.getUsername(), u); return u; }
    public boolean delete(String username) { return idx.remove(username) != null; }
    public Collection<Usuario> findAll() { return idx.values(); }
}
