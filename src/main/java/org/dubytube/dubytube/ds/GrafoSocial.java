package org.dubytube.dubytube.ds;
import java.util.*;
public class GrafoSocial {
    private final Map<String, Set<String>> adj = new HashMap<>();
    public void agregarUsuario(String u) { adj.computeIfAbsent(u, k -> new HashSet<>()); }
    public void amistad(String a, String b) {
        agregarUsuario(a); agregarUsuario(b);
        adj.get(a).add(b); adj.get(b).add(a);
    }

    // en org.dubytube.dubytube.ds.GrafoSocial
    public Set<String> amigosDe(String user) {
        return new java.util.HashSet<>(adj.getOrDefault(user, java.util.Collections.emptySet()));
    }

    /** Sugerencias: amigos de amigos a distancia EXACTA 2 (no amigos directos). */
    public Set<String> sugerenciasAmigos(String user) {
        if (!adj.containsKey(user)) return Collections.emptySet();

        Set<String> directos = adj.get(user);
        Set<String> sugerencias = new HashSet<>();
        Queue<String> q = new ArrayDeque<>();
        Map<String,Integer> dist = new HashMap<>();

        q.add(user); dist.put(user, 0);
        while (!q.isEmpty()) {
            String u = q.poll();
            int du = dist.get(u);
            if (du == 2) continue; // no expandir más allá de 2
            for (String v : adj.getOrDefault(u, Set.of())) {
                if (!dist.containsKey(v)) {
                    dist.put(v, du+1);
                    q.add(v);
                    if (du+1 == 2 && !directos.contains(v)) sugerencias.add(v);
                }
            }
        }
        sugerencias.remove(user);
        return sugerencias;
    }
}
