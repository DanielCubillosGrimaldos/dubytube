// src/main/java/org/dubytube/dubytube/AppContext.java
package org.dubytube.dubytube;

import org.dubytube.dubytube.ds.GrafoSimilitud;
import org.dubytube.dubytube.ds.GrafoSocial;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.domain.Role;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.repo.UsuarioRepo;
import org.dubytube.dubytube.services.CancionIndice;

import java.util.ArrayList;
import java.util.List;

public final class AppContext {

    private static final CancionRepo     canciones  = new CancionRepo();
    private static final UsuarioRepo     usuarios   = new UsuarioRepo();
    private static final GrafoSimilitud  similitud  = new GrafoSimilitud();
    private static final GrafoSocial     social     = new GrafoSocial();
    // Índice compartido de títulos (Trie)
    private static final CancionIndice   indice     = new CancionIndice(canciones);

    // Flag para evitar re-sembrar
    private static boolean BOOTSTRAPPED = false;

    private AppContext(){}

    public static CancionRepo canciones()   { return canciones; }
    public static UsuarioRepo usuarios()    { return usuarios; }
    public static GrafoSimilitud similitud(){ return similitud; }
    public static GrafoSocial social()      { return social; }
    public static CancionIndice indice()    { return indice; }

    /**
     * Inicializa datos mínimos de demo y construye índice/grafo si están vacíos.
     * Llamar una vez al entrar (por ejemplo, en LoginController.initialize()).
     */
    public static synchronized void bootstrapIfEmpty() {
        if (BOOTSTRAPPED) return;

        // --- Usuarios demo ---
        if (usuarios.find("admin").isEmpty()) {
            Usuario a = new Usuario("admin", "123", "Administrador");
            a.setRole(Role.ADMIN);
            usuarios.save(a);
        }
        if (usuarios.find("daniel").isEmpty()) {
            usuarios.save(new Usuario("daniel", "123", "Daniel"));
        }

        // --- Canciones demo ---
        if (canciones.findAll().isEmpty()) {
            canciones.save(new Cancion("c1","Love Song","Adele","Pop",2015,210));
            canciones.save(new Cancion("c2","Lobo Hombre","La Unión","Rock",1984,190));
            canciones.save(new Cancion("c3","Ave Maria","Schubert","Clásica",1825,150));
        }

        // Siempre garantizamos que el índice esté actualizado
        reindex();

        // (Re)construir el grafo de similitud con todo el catálogo actual
        rebuildSimilarityGraph();

        BOOTSTRAPPED = true;
    }

    /** Reindexa el trie de títulos con el catálogo actual. */
    public static void reindex() {
        indice.indexarExistentes();
    }

    /** Reconstruye heurísticamente el grafo de similitud del catálogo actual. */
    public static void rebuildSimilarityGraph() {
        // Si tu clase GrafoSimilitud soporta limpiar, podrías llamar similitud.clear();
        // Obtenemos una lista concreta para iterar sin casts peligrosos
        List<Cancion> list = new ArrayList<>(canciones.findAll());

        int n = list.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Cancion a = list.get(i);
                Cancion b = list.get(j);

                double score = 0;
                if (eq(a.getArtista(), b.getArtista())) score += 5.0;
                if (eq(a.getGenero(),  b.getGenero()))  score += 3.0;
                int diff = Math.abs(a.getAnio() - b.getAnio());
                if (diff <= 2)      score += 2.0;
                else if (diff <= 5) score += 1.0;

                double distancia = Math.max(0.5, 10.0 - score); // menor = más similar
                similitud.agregarSimilitud(a.getId(), b.getId(), distancia);
            }
        }
    }

    private static boolean eq(String x, String y) {
        return x != null && y != null && x.equalsIgnoreCase(y);
    }
}
