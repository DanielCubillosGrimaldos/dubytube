package org.dubytube.dubytube.services;

import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.ds.GrafoSimilitud;
import org.dubytube.dubytube.repo.CancionRepo;

import java.util.*;

public class RecomendacionService {

    public static class Rec {
        public final Cancion cancion;
        public final double distancia; // menor = más similar
        public Rec(Cancion c, double d){ this.cancion = c; this.distancia = d; }
    }

    private final CancionRepo repo;
    private final GrafoSimilitud grafo = new GrafoSimilitud();

    public RecomendacionService(CancionRepo repo) {
        this.repo = repo;
        buildGraph();
    }

    private void buildGraph() {
        // nodos
        for (Cancion c : repo.findAll()) grafo.agregarCancion(c.getId());
        // aristas con peso (distancia): más bajo = más similar
        List<Cancion> all = new ArrayList<>(repo.findAll());
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                Cancion a = all.get(i), b = all.get(j);
                double w = distancia(a, b);
                grafo.agregarSimilitud(a.getId(), b.getId(), w);
            }
        }
    }

    // Heurística simple: artista igual (mucha similitud), género igual (bastante),
    // y años cercanos suman similitud; el resultado es una distancia (menor = más similar).
    private double distancia(Cancion a, Cancion b) {
        double d = 1.0;
        if (a.getGenero()!=null && b.getGenero()!=null &&
                a.getGenero().equalsIgnoreCase(b.getGenero())) d -= 0.4;
        if (a.getArtista()!=null && b.getArtista()!=null &&
                a.getArtista().equalsIgnoreCase(b.getArtista())) d -= 0.5;
        int diff = Math.abs(a.getAnio() - b.getAnio());
        d += Math.min(diff, 40)/100.0; // +0.00..+0.40
        if (d < 0.05) d = 0.05;
        return d;
    }

    /**
     * Recomienda canciones similares a la fuente, excluyendo la canción original.
     * 
     * @param sourceId ID de la canción semilla
     * @param k Número de recomendaciones deseadas
     * @return Lista de recomendaciones ordenadas por similitud (sin incluir la canción fuente)
     */
    public List<Rec> recomendar(String sourceId, int k) {
        if (sourceId == null) return Collections.emptyList();
        
        // Pedimos k+1 para compensar la eliminación de la canción fuente
        List<String> ids = grafo.recomendarDesde(sourceId, k + 1);
        Map<String, Double> dist = grafo.dijkstra(sourceId);
        List<Rec> out = new ArrayList<>();
        
        for (String id : ids) {
            // Excluir la canción fuente de los resultados
            if (id.equals(sourceId)) continue;
            
            repo.find(id).ifPresent(c -> out.add(new Rec(c, dist.getOrDefault(id, Double.NaN))));
        }
        
        // Limitar a k resultados (sin contar la fuente)
        return out.size() > k ? out.subList(0, k) : out;
    }
}
