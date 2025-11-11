package org.dubytube.dubytube.services;
import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.repo.CancionRepo;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
public class BusquedaAvanzada {

    public enum Logica { AND, OR }

    private final CancionRepo repo;
    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    public BusquedaAvanzada(CancionRepo repo) {
        this.repo = repo;
    }

    public List<Cancion> buscar(String artista, String genero,
                                Integer anioMin, Integer anioMax,
                                Logica logica) {
        // snapshot para evitar ConcurrentModification
        List<Cancion> fuente = new ArrayList<>(repo.findAll());

        List<Callable<Set<Cancion>>> tareas = new ArrayList<>();

        if (artista != null && !artista.isBlank()) {
            String a = artista.toLowerCase(Locale.ROOT);
            tareas.add(() -> filtrar(fuente, c -> c.getArtista() != null &&
                    c.getArtista().toLowerCase(Locale.ROOT).contains(a)));
        }
        if (genero != null && !genero.isBlank()) {
            String g = genero.toLowerCase(Locale.ROOT);
            tareas.add(() -> filtrar(fuente, c -> c.getGenero() != null &&
                    c.getGenero().toLowerCase(Locale.ROOT).contains(g)));
        }
        if (anioMin != null || anioMax != null) {
            int min = (anioMin != null) ? anioMin : Integer.MIN_VALUE;
            int max = (anioMax != null) ? anioMax : Integer.MAX_VALUE;
            tareas.add(() -> filtrar(fuente, c -> c.getAnio() >= min && c.getAnio() <= max));
        }

        // Si no hay filtros, devuelve todo (comportamiento típico)
        if (tareas.isEmpty()) return fuente;

        try {
            List<Future<Set<Cancion>>> futuros = pool.invokeAll(tareas);

            // Combinar resultados según lógica
            Set<Cancion> resultado = (logica == Logica.AND)
                    ? new HashSet<>(fuente) // intersección
                    : new HashSet<>();      // unión

            for (Future<Set<Cancion>> f : futuros) {
                Set<Cancion> parciales = f.get();
                if (logica == Logica.AND) {
                    resultado.retainAll(parciales);
                } else {
                    resultado.addAll(parciales);
                }
            }
            return resultado.stream().collect(Collectors.toList());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (ExecutionException e) {
            throw new RuntimeException("Error en búsqueda concurrente", e.getCause());
        }
    }

    private Set<Cancion> filtrar(List<Cancion> fuente, Predicate<Cancion> p) {
        return fuente.stream().filter(p).collect(Collectors.toSet());
    }

    public void shutdown() { pool.shutdown(); }
}
