package org.dubytube.dubytube.repo;

import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.domain.Role;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UsuarioRepo {

    private final Map<String, Usuario> idx = new HashMap<>();

    // Seed de prueba (admin + usuario demo)
    public UsuarioRepo() {
        if (idx.isEmpty()) {
            Usuario admin = new Usuario("admin", "123", "Administrador");
            admin.setRole(Role.ADMIN);
            save(admin);

            Usuario demo = new Usuario("daniel", "123", "Daniel");
            demo.setRole(Role.USER);
            save(demo);
        }
    }

    public Optional<Usuario> find(String username) {
        return Optional.ofNullable(idx.get(username));
    }

    public boolean exists(String username) {
        return idx.containsKey(username);
    }

    public Usuario save(Usuario u) {
        idx.put(u.getUsername(), u);
        return u;
    }

    public boolean delete(String username) {
        return idx.remove(username) != null;
    }

    public Collection<Usuario> findAll() {
        return idx.values();
    }
}

