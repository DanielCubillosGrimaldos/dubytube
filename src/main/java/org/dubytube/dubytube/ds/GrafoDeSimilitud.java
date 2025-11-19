package org.dubytube.dubytube.ds;

import org.dubytube.dubytube.domain.Cancion;

import java.util.*;

/**
 * Grafo pesado no dirigido para gestionar similitudes entre canciones.
 * 
 * <p>Requisitos cumplidos:</p>
 * <ul>
 *   <li>RF-021: Implementación de Grafo Pesado No Dirigido</li>
 *   <li>RF-022: Algoritmo de Dijkstra para encontrar canciones más similares</li>
 * </ul>
 * 
 * <p><b>Estructura:</b></p>
 * <ul>
 *   <li>Vértices: Objetos {@link Cancion}</li>
 *   <li>Aristas: Peso de similitud (0-100) entre dos canciones</li>
 *   <li>No dirigido: Si A está conectada con B, B también está conectada con A</li>
 *   <li>Pesado: Cada arista tiene un peso que representa el grado de similitud</li>
 * </ul>
 * 
 * <p><b>Uso principal:</b> Sistema de recomendación de canciones basado en similitud.
 * Se utiliza el algoritmo de Dijkstra modificado para encontrar las canciones más similares,
 * donde mayor peso indica mayor similitud (se invierte la lógica estándar de Dijkstra).</p>
 * 
 * <p><b>Complejidad de operaciones principales:</b></p>
 * <ul>
 *   <li>Agregar vértice: O(1)</li>
 *   <li>Agregar arista: O(1)</li>
 *   <li>Obtener vecinos: O(1)</li>
 *   <li>Dijkstra: O((V + E) log V) con priority queue</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class GrafoDeSimilitud {
    
    /**
     * Representa una arista pesada en el grafo.
     * Almacena la canción destino y el peso de similitud.
     */
    private static class Arista {
        final Cancion destino;
        final int peso; // 0-100: grado de similitud
        
        /**
         * Constructor de arista.
         * 
         * @param destino Canción destino
         * @param peso Peso de similitud (0-100)
         */
        Arista(Cancion destino, int peso) {
            this.destino = destino;
            this.peso = peso;
        }
        
        @Override
        public String toString() {
            return destino.getTitulo() + " (similitud: " + peso + "%)";
        }
    }
    
    /**
     * Estructura interna del grafo: mapa de adyacencia.
     * Key: Canción (vértice)
     * Value: Lista de aristas (vecinos con pesos)
     */
    private final Map<Cancion, List<Arista>> grafo;
    
    /**
     * Constructor del grafo de similitud.
     */
    public GrafoDeSimilitud() {
        this.grafo = new HashMap<>();
    }
    
    /**
     * Agrega una canción como vértice al grafo.
     * Complejidad: O(1)
     * 
     * @param cancion Canción a agregar
     */
    public void agregarCancion(Cancion cancion) {
        if (cancion == null) {
            return;
        }
        
        grafo.putIfAbsent(cancion, new ArrayList<>());
    }
    
    /**
     * Elimina una canción del grafo.
     * También elimina todas las aristas que la involucran.
     * Complejidad: O(V + E) en el peor caso
     * 
     * @param cancion Canción a eliminar
     * @return true si se eliminó exitosamente
     */
    public boolean eliminarCancion(Cancion cancion) {
        if (cancion == null || !grafo.containsKey(cancion)) {
            return false;
        }
        
        // Eliminar aristas donde esta canción es destino
        for (List<Arista> aristas : grafo.values()) {
            aristas.removeIf(arista -> arista.destino.equals(cancion));
        }
        
        // Eliminar el vértice
        grafo.remove(cancion);
        return true;
    }
    
    /**
     * Conecta dos canciones con un peso de similitud.
     * Como es no dirigido, se crean dos aristas (A→B y B→A).
     * Complejidad: O(1)
     * 
     * @param cancion1 Primera canción
     * @param cancion2 Segunda canción
     * @param similitud Peso de similitud (0-100)
     */
    public void conectar(Cancion cancion1, Cancion cancion2, int similitud) {
        if (cancion1 == null || cancion2 == null) {
            return;
        }
        
        if (cancion1.equals(cancion2)) {
            return; // No se permiten lazos
        }
        
        // Validar rango de similitud
        similitud = Math.max(0, Math.min(100, similitud));
        
        // Asegurar que ambos vértices existen
        agregarCancion(cancion1);
        agregarCancion(cancion2);
        
        // Grafo no dirigido: agregar arista en ambas direcciones
        grafo.get(cancion1).add(new Arista(cancion2, similitud));
        grafo.get(cancion2).add(new Arista(cancion1, similitud));
    }
    
    /**
     * Conecta dos canciones calculando automáticamente su similitud.
     * Utiliza el método {@link Cancion#calcularSimilitud(Cancion)}.
     * Complejidad: O(1)
     * 
     * @param cancion1 Primera canción
     * @param cancion2 Segunda canción
     */
    public void conectarConSimilitudAutomatica(Cancion cancion1, Cancion cancion2) {
        if (cancion1 == null || cancion2 == null) {
            return;
        }
        
        int similitud = (int) Math.round(cancion1.calcularSimilitud(cancion2));
        conectar(cancion1, cancion2, similitud);
    }
    
    /**
     * Desconecta dos canciones eliminando sus aristas mutuas.
     * Complejidad: O(E) donde E es el número de aristas de cada vértice
     * 
     * @param cancion1 Primera canción
     * @param cancion2 Segunda canción
     * @return true si se desconectaron exitosamente
     */
    public boolean desconectar(Cancion cancion1, Cancion cancion2) {
        if (cancion1 == null || cancion2 == null) {
            return false;
        }
        
        if (!grafo.containsKey(cancion1) || !grafo.containsKey(cancion2)) {
            return false;
        }
        
        boolean removed1 = grafo.get(cancion1).removeIf(a -> a.destino.equals(cancion2));
        boolean removed2 = grafo.get(cancion2).removeIf(a -> a.destino.equals(cancion1));
        
        return removed1 && removed2;
    }
    
    /**
     * Obtiene el peso de similitud entre dos canciones.
     * Complejidad: O(E) donde E es el número de aristas del vértice
     * 
     * @param cancion1 Primera canción
     * @param cancion2 Segunda canción
     * @return Peso de similitud, o -1 si no están conectadas
     */
    public int getSimilitud(Cancion cancion1, Cancion cancion2) {
        if (cancion1 == null || cancion2 == null || !grafo.containsKey(cancion1)) {
            return -1;
        }
        
        for (Arista arista : grafo.get(cancion1)) {
            if (arista.destino.equals(cancion2)) {
                return arista.peso;
            }
        }
        
        return -1;
    }
    
    /**
     * Verifica si dos canciones están conectadas.
     * Complejidad: O(E)
     * 
     * @param cancion1 Primera canción
     * @param cancion2 Segunda canción
     * @return true si están conectadas
     */
    public boolean estanConectadas(Cancion cancion1, Cancion cancion2) {
        return getSimilitud(cancion1, cancion2) != -1;
    }
    
    /**
     * Obtiene todos los vecinos de una canción con sus pesos.
     * Complejidad: O(1)
     * 
     * @param cancion Canción origen
     * @return Lista de aristas (vecinos con pesos), o lista vacía si no existe
     */
    public List<Arista> getVecinos(Cancion cancion) {
        if (cancion == null || !grafo.containsKey(cancion)) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(grafo.get(cancion));
    }
    
    /**
     * Obtiene el grado (número de conexiones) de una canción.
     * Complejidad: O(1)
     * 
     * @param cancion Canción
     * @return Número de canciones conectadas
     */
    public int getGrado(Cancion cancion) {
        if (cancion == null || !grafo.containsKey(cancion)) {
            return 0;
        }
        
        return grafo.get(cancion).size();
    }
    
    /**
     * Encuentra las K canciones más similares a una canción dada usando Dijkstra modificado.
     * 
     * <p><b>Algoritmo:</b> Dijkstra invertido (maximización de similitud).
     * En lugar de buscar la ruta más corta, buscamos la ruta de mayor similitud.</p>
     * 
     * <p>Complejidad: O((V + E) log V) con priority queue</p>
     * 
     * <p><b>RF-022:</b> Implementación del algoritmo de Dijkstra para recomendaciones.</p>
     * 
     * @param origen Canción desde la cual buscar similares
     * @param k Número de canciones similares a retornar
     * @return Lista de las K canciones más similares ordenadas por similitud descendente
     */
    public List<Cancion> encontrarMasSimilares(Cancion origen, int k) {
        if (origen == null || !grafo.containsKey(origen) || k <= 0) {
            return new ArrayList<>();
        }
        
        // Mapa de similitud máxima desde el origen
        Map<Cancion, Integer> similitudMaxima = new HashMap<>();
        
        // Priority Queue: ordenar por similitud DESCENDENTE (invertido de Dijkstra clásico)
        PriorityQueue<Map.Entry<Cancion, Integer>> pq = new PriorityQueue<>(
            (a, b) -> b.getValue().compareTo(a.getValue()) // Max-heap
        );
        
        // Inicializar
        similitudMaxima.put(origen, 100); // La canción origen tiene 100% similitud consigo misma
        pq.offer(new AbstractMap.SimpleEntry<>(origen, 100));
        
        // Algoritmo de Dijkstra modificado (maximización)
        while (!pq.isEmpty()) {
            Map.Entry<Cancion, Integer> actual = pq.poll();
            Cancion cancionActual = actual.getKey();
            int similitudActual = actual.getValue();
            
            // Si encontramos un camino con menor similitud, ignorar
            if (similitudActual < similitudMaxima.getOrDefault(cancionActual, 0)) {
                continue;
            }
            
            // Explorar vecinos
            for (Arista arista : grafo.get(cancionActual)) {
                Cancion vecino = arista.destino;
                
                // En este grafo, la similitud es directa (no acumulativa)
                // Usamos el peso de la arista como similitud
                int nuevaSimilitud = arista.peso;
                
                // Si encontramos una similitud mayor, actualizar
                if (nuevaSimilitud > similitudMaxima.getOrDefault(vecino, -1)) {
                    similitudMaxima.put(vecino, nuevaSimilitud);
                    pq.offer(new AbstractMap.SimpleEntry<>(vecino, nuevaSimilitud));
                }
            }
        }
        
        // Eliminar la canción origen de los resultados
        similitudMaxima.remove(origen);
        
        // Ordenar por similitud descendente y tomar las top K
        return similitudMaxima.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(k)
                .map(Map.Entry::getKey)
                .toList();
    }
    
    /**
     * Encuentra canciones similares con un umbral mínimo de similitud.
     * Complejidad: O(V + E)
     * 
     * @param origen Canción origen
     * @param umbralMinimo Similitud mínima requerida (0-100)
     * @return Lista de canciones con similitud >= umbral
     */
    public List<Cancion> encontrarSimilaresConUmbral(Cancion origen, int umbralMinimo) {
        if (origen == null || !grafo.containsKey(origen)) {
            return new ArrayList<>();
        }
        
        List<Cancion> resultado = new ArrayList<>();
        
        for (Arista arista : grafo.get(origen)) {
            if (arista.peso >= umbralMinimo) {
                resultado.add(arista.destino);
            }
        }
        
        // Ordenar por similitud descendente
        resultado.sort((c1, c2) -> {
            int sim1 = getSimilitud(origen, c1);
            int sim2 = getSimilitud(origen, c2);
            return Integer.compare(sim2, sim1);
        });
        
        return resultado;
    }
    
    /**
     * Construye automáticamente el grafo de similitud para una colección de canciones.
     * Conecta cada par de canciones calculando su similitud.
     * 
     * <p>Solo crea aristas si la similitud es >= umbral.</p>
     * 
     * <p>Complejidad: O(n²) donde n es el número de canciones</p>
     * 
     * @param canciones Colección de canciones
     * @param umbralMinimo Similitud mínima para crear una arista (0-100)
     */
    public void construirGrafoCompleto(Collection<Cancion> canciones, int umbralMinimo) {
        if (canciones == null || canciones.isEmpty()) {
            return;
        }
        
        List<Cancion> listaCanciones = new ArrayList<>(canciones);
        
        // Agregar todos los vértices
        for (Cancion c : listaCanciones) {
            agregarCancion(c);
        }
        
        // Conectar cada par de canciones
        for (int i = 0; i < listaCanciones.size(); i++) {
            for (int j = i + 1; j < listaCanciones.size(); j++) {
                Cancion c1 = listaCanciones.get(i);
                Cancion c2 = listaCanciones.get(j);
                
                int similitud = (int) Math.round(c1.calcularSimilitud(c2));
                
                // Solo conectar si la similitud supera el umbral
                if (similitud >= umbralMinimo) {
                    conectar(c1, c2, similitud);
                }
            }
        }
        
        System.out.println("✓ Grafo construido con " + size() + " vértices y " 
                          + contarAristas() + " aristas (umbral: " + umbralMinimo + "%)");
    }
    
    /**
     * Cuenta el número total de aristas en el grafo.
     * Como es no dirigido, cada arista se cuenta una sola vez.
     * Complejidad: O(V + E)
     * 
     * @return Número de aristas
     */
    public int contarAristas() {
        int total = 0;
        for (List<Arista> aristas : grafo.values()) {
            total += aristas.size();
        }
        return total / 2; // Dividir por 2 porque es no dirigido
    }
    
    /**
     * Obtiene el número de vértices (canciones) en el grafo.
     * Complejidad: O(1)
     * 
     * @return Número de canciones
     */
    public int size() {
        return grafo.size();
    }
    
    /**
     * Verifica si el grafo está vacío.
     * 
     * @return true si no hay canciones
     */
    public boolean isEmpty() {
        return grafo.isEmpty();
    }
    
    /**
     * Verifica si una canción existe en el grafo.
     * Complejidad: O(1)
     * 
     * @param cancion Canción a verificar
     * @return true si existe
     */
    public boolean contiene(Cancion cancion) {
        return cancion != null && grafo.containsKey(cancion);
    }
    
    /**
     * Obtiene todas las canciones (vértices) del grafo.
     * Complejidad: O(V)
     * 
     * @return Set de canciones
     */
    public Set<Cancion> getCanciones() {
        return new HashSet<>(grafo.keySet());
    }
    
    /**
     * Limpia completamente el grafo.
     * Complejidad: O(1)
     */
    public void clear() {
        grafo.clear();
    }
    
    /**
     * Calcula la densidad del grafo.
     * Densidad = (2 * E) / (V * (V - 1))
     * 
     * @return Densidad entre 0.0 y 1.0
     */
    public double calcularDensidad() {
        int v = size();
        if (v <= 1) {
            return 0.0;
        }
        
        int e = contarAristas();
        return (2.0 * e) / (v * (v - 1));
    }
    
    /**
     * Encuentra la canción con más conexiones (hub del grafo).
     * Complejidad: O(V)
     * 
     * @return Canción con mayor grado, o null si el grafo está vacío
     */
    public Cancion encontrarHub() {
        if (isEmpty()) {
            return null;
        }
        
        Cancion hub = null;
        int maxGrado = -1;
        
        for (Map.Entry<Cancion, List<Arista>> entry : grafo.entrySet()) {
            int grado = entry.getValue().size();
            if (grado > maxGrado) {
                maxGrado = grado;
                hub = entry.getKey();
            }
        }
        
        return hub;
    }
    
    /**
     * Obtiene estadísticas del grafo.
     * 
     * @return String con información del grafo
     */
    public String getEstadisticas() {
        if (isEmpty()) {
            return "Grafo vacío";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Estadísticas del Grafo de Similitud ===\n");
        sb.append("Vértices (canciones): ").append(size()).append("\n");
        sb.append("Aristas (conexiones): ").append(contarAristas()).append("\n");
        sb.append("Densidad: ").append(String.format("%.2f%%", calcularDensidad() * 100)).append("\n");
        
        Cancion hub = encontrarHub();
        if (hub != null) {
            sb.append("Hub (canción más conectada): ").append(hub.getTitulo())
              .append(" con ").append(getGrado(hub)).append(" conexiones\n");
        }
        
        // Estadísticas de similitud
        int totalSimilitud = 0;
        int contador = 0;
        for (List<Arista> aristas : grafo.values()) {
            for (Arista a : aristas) {
                totalSimilitud += a.peso;
                contador++;
            }
        }
        
        if (contador > 0) {
            double similitudPromedio = (double) totalSimilitud / contador;
            sb.append("Similitud promedio: ").append(String.format("%.1f%%", similitudPromedio)).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Representación en String del grafo (para debugging).
     * 
     * @return String con lista de adyacencia
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "Grafo vacío";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Grafo de Similitud (").append(size()).append(" canciones):\n");
        
        for (Map.Entry<Cancion, List<Arista>> entry : grafo.entrySet()) {
            sb.append("  ").append(entry.getKey().getTitulo()).append(" → [");
            
            List<Arista> aristas = entry.getValue();
            for (int i = 0; i < aristas.size(); i++) {
                sb.append(aristas.get(i));
                if (i < aristas.size() - 1) {
                    sb.append(", ");
                }
            }
            
            sb.append("]\n");
        }
        
        return sb.toString();
    }
}
