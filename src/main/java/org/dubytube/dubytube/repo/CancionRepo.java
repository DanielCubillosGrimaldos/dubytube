package org.dubytube.dubytube.repo;

import org.dubytube.dubytube.domain.Cancion;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CancionRepo {
    private final Map<String, Cancion> idx = new HashMap<>();
    public Optional<Cancion> find(String id) { return Optional.ofNullable(idx.get(id)); }
    public Cancion save(Cancion c) { idx.put(c.getId(), c); return c; }
    public boolean delete(String id) { return idx.remove(id) != null; }
    public Collection<Cancion> findAll() { return idx.values(); }
}
