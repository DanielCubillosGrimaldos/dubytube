package org.dubytube.dubytube.services;

import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.UsuarioRepo;

import java.util.Objects;
import java.util.Optional;

public class AuthService {
    private final UsuarioRepo repo;
    public AuthService(UsuarioRepo repo){ this.repo = repo; }

    public Optional<Usuario> login(String username, String password){
        return repo.find(username).filter(u -> Objects.equals(u.getPassword(), password));
    }
}
