package org.dubytube.dubytube.ds;
import java.util.*;
/** Grafo no dirigido; peso = "distancia" (más bajo = más similar). */
public class GrafoSimilitud {
    private final Map<String, Map<String, Double>> adj = new HashMap<>();
    public void agregarCancion(String id) {
        adj.computeIfAbsent(id, k -> new HashMap<>());
    }
    public void agregarSimilitud(String id1, String id2, double distancia) {
        if (id1.equals(id2)) return;
        agregarCancion(id1); agregarCancion(id2);
        adj.get(id1).put(id2, distancia);
        adj.get(id2).put(id1, distancia);
    }
    /** Dijkstra estándar: retorna distancia mínima desde source a cada nodo. */
    public Map<String, Double> dijkstra(String source) {
        Map<String, Double> dist = new HashMap<>();
        for (String v : adj.keySet()) dist.put(v, Double.POSITIVE_INFINITY);
        if (!adj.containsKey(source)) return dist;
        dist.put(source, 0.0);
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(source);
        while (!pq.isEmpty()) {
            String u = pq.poll();
            for (Map.Entry<String, Double> e : adj.get(u).entrySet()) {
                String v = e.getKey(); double w = e.getValue();
                double alt = dist.get(u) + w;
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }
        return dist;
    }
    /** Recomienda las k canciones más cercanas (menor distancia), excluye la misma. */
    public List<String> recomendarDesde(String source, int k) {
        Map<String, Double> d = dijkstra(source);
        return d.entrySet().stream()
                .filter(e -> !e.getKey().equals(source) && e.getValue()!=Double.POSITIVE_INFINITY)
                .sorted(Map.Entry.comparingByValue())
                .limit(k)
                .map(Map.Entry::getKey)
                .toList();
    }
}
