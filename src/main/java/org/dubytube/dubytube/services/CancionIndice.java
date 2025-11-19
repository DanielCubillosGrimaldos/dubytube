package org.dubytube.dubytube.services;

import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.ds.PruebaTitulo;
import org.dubytube.dubytube.repo.CancionRepo;

import java.util.List;

public class CancionIndice {
    private final CancionRepo repo;
    private final PruebaTitulo trie = new PruebaTitulo();

    public CancionIndice(CancionRepo repo) {
        this.repo = repo;
    }

    /** Indexa todo lo que haya en memoria. */
    public void indexarExistentes() {
        for (Cancion c : repo.findAll()) trie.insert(c);
    }

    public void registrarCancion(Cancion c) {
        repo.save(c);
        trie.insert(c);
    }

    public List<Cancion> sugerirPorTitulo(String prefijo, int k) {
        return trie.suggest(prefijo, k);
    }
}
