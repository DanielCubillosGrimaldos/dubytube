package org.dubytube.dubytube.ds;

import org.dubytube.dubytube.domain.Usuario;

import java.util.*;

/**
 * Grafo no dirigido para gestionar conexiones sociales entre usuarios.
 * 
 * <p>Requisitos cumplidos:</p>
 * <ul>
 *   <li>RF-023: Implementación de Grafo No Dirigido para red social</li>
 *   <li>RF-024: Algoritmo BFS para encontrar amigos de amigos</li>
 * </ul>
 * 
 * <p><b>Estructura:</b></p>
 * <ul>
 *   <li>Vértices: Objetos {@link Usuario}</li>
 *   <li>Aristas: Relación de amistad entre dos usuarios</li>
 *   <li>No dirigido: Si A es amigo de B, B también es amigo de A</li>
 *   <li>No pesado: Todas las amistades tienen el mismo valor (binario)</li>
 * </ul>
 * 
 * <p><b>Uso principal:</b> Sistema de recomendación de amigos y exploración
 * de la red social. Permite encontrar conexiones de segundo nivel
 * (amigos de amigos) y calcular distancias entre usuarios.</p>
 * 
 * <p><b>Complejidad de operaciones principales:</b></p>
 * <ul>
 *   <li>Agregar vértice: O(1)</li>
 *   <li>Agregar arista: O(1)</li>
 *   <li>Obtener amigos: O(1)</li>
 *   <li>BFS: O(V + E)</li>
 *   <li>DFS: O(V + E)</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class GrafoSocial {
    
    /**
     * Estructura interna del grafo: mapa de adyacencia.
     * Key: Usuario (vértice)
     * Value: Set de usuarios amigos (vecinos)
     */
    private final Map<Usuario, Set<Usuario>> grafo;
    
    /**
     * Constructor del grafo social.
     */
    public GrafoSocial() {
        this.grafo = new HashMap<>();
    }
    
    /**
     * Agrega un usuario como vértice al grafo.
     * Complejidad: O(1)
     * 
     * @param usuario Usuario a agregar
     */
    public void agregarUsuario(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        
        grafo.putIfAbsent(usuario, new HashSet<>());
    }
    
    /**
     * Elimina un usuario del grafo.
     * También elimina todas las amistades que lo involucran.
     * Complejidad: O(V + E) en el peor caso
     * 
     * @param usuario Usuario a eliminar
     * @return true si se eliminó exitosamente
     */
    public boolean eliminarUsuario(Usuario usuario) {
        if (usuario == null || !grafo.containsKey(usuario)) {
            return false;
        }
        
        // Eliminar este usuario de las listas de amigos de otros
        for (Set<Usuario> amigos : grafo.values()) {
            amigos.remove(usuario);
        }
        
        // Eliminar el vértice
        grafo.remove(usuario);
        return true;
    }
    
    /**
     * Crea una amistad entre dos usuarios.
     * Como es no dirigido, se crean dos conexiones (A→B y B→A).
     * Complejidad: O(1)
     * 
     * @param usuario1 Primer usuario
     * @param usuario2 Segundo usuario
     * @return true si se creó la amistad, false si ya existía o los usuarios son el mismo
     */
    public boolean agregarAmistad(Usuario usuario1, Usuario usuario2) {
        if (usuario1 == null || usuario2 == null) {
            return false;
        }
        
        if (usuario1.equals(usuario2)) {
            return false; // Un usuario no puede ser amigo de sí mismo
        }
        
        // Asegurar que ambos vértices existen
        agregarUsuario(usuario1);
        agregarUsuario(usuario2);
        
        // Grafo no dirigido: agregar en ambas direcciones
        boolean added1 = grafo.get(usuario1).add(usuario2);
        boolean added2 = grafo.get(usuario2).add(usuario1);
        
        return added1 && added2;
    }
    
    /**
     * Elimina la amistad entre dos usuarios.
     * Complejidad: O(1)
     * 
     * @param usuario1 Primer usuario
     * @param usuario2 Segundo usuario
     * @return true si se eliminó la amistad
     */
    public boolean eliminarAmistad(Usuario usuario1, Usuario usuario2) {
        if (usuario1 == null || usuario2 == null) {
            return false;
        }
        
        if (!grafo.containsKey(usuario1) || !grafo.containsKey(usuario2)) {
            return false;
        }
        
        boolean removed1 = grafo.get(usuario1).remove(usuario2);
        boolean removed2 = grafo.get(usuario2).remove(usuario1);
        
        return removed1 && removed2;
    }
    
    /**
     * Verifica si dos usuarios son amigos.
     * Complejidad: O(1)
     * 
     * @param usuario1 Primer usuario
     * @param usuario2 Segundo usuario
     * @return true si son amigos
     */
    public boolean sonAmigos(Usuario usuario1, Usuario usuario2) {
        if (usuario1 == null || usuario2 == null || !grafo.containsKey(usuario1)) {
            return false;
        }
        
        return grafo.get(usuario1).contains(usuario2);
    }
    
    /**
     * Obtiene todos los amigos directos de un usuario.
     * Complejidad: O(1) para acceso al set, O(k) para copiar donde k = número de amigos
     * 
     * @param usuario Usuario
     * @return Set de amigos, o set vacío si el usuario no existe
     */
    public Set<Usuario> getAmigos(Usuario usuario) {
        if (usuario == null || !grafo.containsKey(usuario)) {
            return new HashSet<>();
        }
        
        return new HashSet<>(grafo.get(usuario));
    }
    
    /**
     * Obtiene el número de amigos de un usuario (grado del vértice).
     * Complejidad: O(1)
     * 
     * @param usuario Usuario
     * @return Número de amigos
     */
    public int contarAmigos(Usuario usuario) {
        if (usuario == null || !grafo.containsKey(usuario)) {
            return 0;
        }
        
        return grafo.get(usuario).size();
    }
    
    /**
     * Encuentra amigos de amigos usando BFS (Breadth-First Search).
     * 
     * <p><b>Algoritmo BFS:</b> Explora la red social nivel por nivel,
     * encontrando primero amigos directos (nivel 1) y luego amigos de amigos (nivel 2).</p>
     * 
     * <p>Complejidad: O(V + E) donde V = usuarios, E = amistades</p>
     * 
     * <p><b>RF-024:</b> Implementación del algoritmo BFS para recomendaciones.</p>
     * 
     * @param usuario Usuario origen
     * @return Set de usuarios que son amigos de amigos (excluye amigos directos y al usuario mismo)
     */
    public Set<Usuario> encontrarAmigosDeAmigos(Usuario usuario) {
        if (usuario == null || !grafo.containsKey(usuario)) {
            return new HashSet<>();
        }
        
        Set<Usuario> amigosDirectos = grafo.get(usuario);
        Set<Usuario> amigosDeAmigos = new HashSet<>();
        
        // Recorrer cada amigo directo
        for (Usuario amigo : amigosDirectos) {
            // Obtener los amigos de este amigo
            Set<Usuario> amigosDelAmigo = grafo.get(amigo);
            
            if (amigosDelAmigo != null) {
                for (Usuario candidato : amigosDelAmigo) {
                    // Excluir al usuario mismo y a sus amigos directos
                    if (!candidato.equals(usuario) && !amigosDirectos.contains(candidato)) {
                        amigosDeAmigos.add(candidato);
                    }
                }
            }
        }
        
        return amigosDeAmigos;
    }
    
    /**
     * Encuentra amigos de amigos ordenados por número de conexiones mutuas.
     * Útil para recomendar usuarios con más amigos en común.
     * 
     * <p>Complejidad: O(V + E + k log k) donde k = número de amigos de amigos</p>
     * 
     * @param usuario Usuario origen
     * @param limite Número máximo de recomendaciones
     * @return Lista ordenada de usuarios recomendados (más conexiones mutuas primero)
     */
    public List<Usuario> recomendarAmigos(Usuario usuario, int limite) {
        if (usuario == null || !grafo.containsKey(usuario) || limite <= 0) {
            return new ArrayList<>();
        }
        
        Set<Usuario> amigosDirectos = grafo.get(usuario);
        Map<Usuario, Integer> conexionesMutuas = new HashMap<>();
        
        // Contar conexiones mutuas para cada amigo de amigo
        for (Usuario amigo : amigosDirectos) {
            Set<Usuario> amigosDelAmigo = grafo.get(amigo);
            
            if (amigosDelAmigo != null) {
                for (Usuario candidato : amigosDelAmigo) {
                    // Excluir al usuario mismo y a sus amigos directos
                    if (!candidato.equals(usuario) && !amigosDirectos.contains(candidato)) {
                        conexionesMutuas.put(candidato, 
                            conexionesMutuas.getOrDefault(candidato, 0) + 1);
                    }
                }
            }
        }
        
        // Ordenar por número de conexiones mutuas (descendente) y tomar los top K
        return conexionesMutuas.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limite)
                .map(Map.Entry::getKey)
                .toList();
    }
    
    /**
     * Calcula la distancia más corta entre dos usuarios usando BFS.
     * La distancia es el número de aristas en el camino más corto.
     * 
     * <p>Complejidad: O(V + E)</p>
     * 
     * @param origen Usuario origen
     * @param destino Usuario destino
     * @return Distancia (número de saltos), o -1 si no están conectados
     */
    public int calcularDistancia(Usuario origen, Usuario destino) {
        if (origen == null || destino == null || 
            !grafo.containsKey(origen) || !grafo.containsKey(destino)) {
            return -1;
        }
        
        if (origen.equals(destino)) {
            return 0;
        }
        
        // BFS para encontrar el camino más corto
        Queue<Usuario> cola = new LinkedList<>();
        Map<Usuario, Integer> distancias = new HashMap<>();
        
        cola.offer(origen);
        distancias.put(origen, 0);
        
        while (!cola.isEmpty()) {
            Usuario actual = cola.poll();
            int distanciaActual = distancias.get(actual);
            
            // Explorar vecinos
            for (Usuario vecino : grafo.get(actual)) {
                if (!distancias.containsKey(vecino)) {
                    int nuevaDistancia = distanciaActual + 1;
                    distancias.put(vecino, nuevaDistancia);
                    
                    if (vecino.equals(destino)) {
                        return nuevaDistancia;
                    }
                    
                    cola.offer(vecino);
                }
            }
        }
        
        return -1; // No hay camino
    }
    
    /**
     * Encuentra el camino más corto entre dos usuarios usando BFS.
     * Retorna la secuencia de usuarios que conecta origen con destino.
     * 
     * <p>Complejidad: O(V + E)</p>
     * 
     * @param origen Usuario origen
     * @param destino Usuario destino
     * @return Lista con el camino (incluyendo origen y destino), o lista vacía si no hay camino
     */
    public List<Usuario> encontrarCamino(Usuario origen, Usuario destino) {
        if (origen == null || destino == null || 
            !grafo.containsKey(origen) || !grafo.containsKey(destino)) {
            return new ArrayList<>();
        }
        
        if (origen.equals(destino)) {
            return List.of(origen);
        }
        
        // BFS con reconstrucción de camino
        Queue<Usuario> cola = new LinkedList<>();
        Map<Usuario, Usuario> padres = new HashMap<>();
        Set<Usuario> visitados = new HashSet<>();
        
        cola.offer(origen);
        visitados.add(origen);
        padres.put(origen, null);
        
        boolean encontrado = false;
        
        while (!cola.isEmpty() && !encontrado) {
            Usuario actual = cola.poll();
            
            for (Usuario vecino : grafo.get(actual)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    padres.put(vecino, actual);
                    cola.offer(vecino);
                    
                    if (vecino.equals(destino)) {
                        encontrado = true;
                        break;
                    }
                }
            }
        }
        
        if (!encontrado) {
            return new ArrayList<>();
        }
        
        // Reconstruir el camino desde destino hasta origen
        List<Usuario> camino = new ArrayList<>();
        Usuario actual = destino;
        
        while (actual != null) {
            camino.add(0, actual); // Agregar al inicio
            actual = padres.get(actual);
        }
        
        return camino;
    }
    
    /**
     * Encuentra todos los usuarios alcanzables desde un usuario usando BFS.
     * Útil para identificar componentes conexas.
     * 
     * <p>Complejidad: O(V + E)</p>
     * 
     * @param usuario Usuario origen
     * @return Set de usuarios en la misma componente conexa
     */
    public Set<Usuario> encontrarComponente(Usuario usuario) {
        if (usuario == null || !grafo.containsKey(usuario)) {
            return new HashSet<>();
        }
        
        Set<Usuario> componente = new HashSet<>();
        Queue<Usuario> cola = new LinkedList<>();
        
        cola.offer(usuario);
        componente.add(usuario);
        
        while (!cola.isEmpty()) {
            Usuario actual = cola.poll();
            
            for (Usuario vecino : grafo.get(actual)) {
                if (!componente.contains(vecino)) {
                    componente.add(vecino);
                    cola.offer(vecino);
                }
            }
        }
        
        return componente;
    }
    
    /**
     * Cuenta el número de componentes conexas en el grafo.
     * Una componente conexa es un subgrafo donde todos los usuarios están conectados.
     * 
     * <p>Complejidad: O(V + E)</p>
     * 
     * @return Número de componentes conexas
     */
    public int contarComponentes() {
        Set<Usuario> visitados = new HashSet<>();
        int componentes = 0;
        
        for (Usuario usuario : grafo.keySet()) {
            if (!visitados.contains(usuario)) {
                // Explorar toda la componente desde este usuario
                Set<Usuario> componente = encontrarComponente(usuario);
                visitados.addAll(componente);
                componentes++;
            }
        }
        
        return componentes;
    }
    
    /**
     * Verifica si el grafo es conexo (todos los usuarios están conectados).
     * 
     * <p>Complejidad: O(V + E)</p>
     * 
     * @return true si hay exactamente una componente conexa
     */
    public boolean esConexo() {
        if (isEmpty()) {
            return true;
        }
        
        return contarComponentes() == 1;
    }
    
    /**
     * Encuentra el usuario con más amigos (hub de la red social).
     * Complejidad: O(V)
     * 
     * @return Usuario con más amigos, o null si el grafo está vacío
     */
    public Usuario encontrarUsuarioMasPopular() {
        if (isEmpty()) {
            return null;
        }
        
        Usuario masPopular = null;
        int maxAmigos = -1;
        
        for (Map.Entry<Usuario, Set<Usuario>> entry : grafo.entrySet()) {
            int numAmigos = entry.getValue().size();
            if (numAmigos > maxAmigos) {
                maxAmigos = numAmigos;
                masPopular = entry.getKey();
            }
        }
        
        return masPopular;
    }
    
    /**
     * Encuentra usuarios con exactamente el número especificado de amigos.
     * Complejidad: O(V)
     * 
     * @param numAmigos Número de amigos
     * @return Set de usuarios con ese número de amigos
     */
    public Set<Usuario> encontrarUsuariosPorNumAmigos(int numAmigos) {
        Set<Usuario> resultado = new HashSet<>();
        
        for (Map.Entry<Usuario, Set<Usuario>> entry : grafo.entrySet()) {
            if (entry.getValue().size() == numAmigos) {
                resultado.add(entry.getKey());
            }
        }
        
        return resultado;
    }
    
    /**
     * Cuenta el número total de amistades en el grafo.
     * Como es no dirigido, cada amistad se cuenta una sola vez.
     * Complejidad: O(V)
     * 
     * @return Número de amistades
     */
    public int contarAmistades() {
        int total = 0;
        for (Set<Usuario> amigos : grafo.values()) {
            total += amigos.size();
        }
        return total / 2; // Dividir por 2 porque es no dirigido
    }
    
    /**
     * Obtiene el número de usuarios en el grafo.
     * Complejidad: O(1)
     * 
     * @return Número de usuarios
     */
    public int size() {
        return grafo.size();
    }
    
    /**
     * Verifica si el grafo está vacío.
     * 
     * @return true si no hay usuarios
     */
    public boolean isEmpty() {
        return grafo.isEmpty();
    }
    
    /**
     * Verifica si un usuario existe en el grafo.
     * Complejidad: O(1)
     * 
     * @param usuario Usuario a verificar
     * @return true si existe
     */
    public boolean contiene(Usuario usuario) {
        return usuario != null && grafo.containsKey(usuario);
    }
    
    /**
     * Obtiene todos los usuarios del grafo.
     * Complejidad: O(V)
     * 
     * @return Set de usuarios
     */
    public Set<Usuario> getUsuarios() {
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
     * Rango: 0.0 (sin conexiones) a 1.0 (totalmente conectado)
     * 
     * @return Densidad entre 0.0 y 1.0
     */
    public double calcularDensidad() {
        int v = size();
        if (v <= 1) {
            return 0.0;
        }
        
        int e = contarAmistades();
        return (2.0 * e) / (v * (v - 1));
    }
    
    /**
     * Calcula el número promedio de amigos por usuario.
     * 
     * @return Promedio de amigos, o 0.0 si el grafo está vacío
     */
    public double calcularPromedioAmigos() {
        if (isEmpty()) {
            return 0.0;
        }
        
        int totalAmigos = 0;
        for (Set<Usuario> amigos : grafo.values()) {
            totalAmigos += amigos.size();
        }
        
        return (double) totalAmigos / size();
    }
    
    /**
     * Obtiene estadísticas del grafo social.
     * 
     * @return String con información del grafo
     */
    public String getEstadisticas() {
        if (isEmpty()) {
            return "Grafo social vacío";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Estadísticas del Grafo Social ===\n");
        sb.append("Usuarios: ").append(size()).append("\n");
        sb.append("Amistades: ").append(contarAmistades()).append("\n");
        sb.append("Promedio de amigos: ").append(String.format("%.2f", calcularPromedioAmigos())).append("\n");
        sb.append("Densidad: ").append(String.format("%.2f%%", calcularDensidad() * 100)).append("\n");
        sb.append("Componentes conexas: ").append(contarComponentes()).append("\n");
        sb.append("Es conexo: ").append(esConexo() ? "Sí" : "No").append("\n");
        
        Usuario masPopular = encontrarUsuarioMasPopular();
        if (masPopular != null) {
            sb.append("Usuario más popular: ").append(masPopular.getUsername())
              .append(" con ").append(contarAmigos(masPopular)).append(" amigos\n");
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
            return "Grafo social vacío";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Grafo Social (").append(size()).append(" usuarios):\n");
        
        for (Map.Entry<Usuario, Set<Usuario>> entry : grafo.entrySet()) {
            sb.append("  ").append(entry.getKey().getUsername()).append(" → [");
            
            List<String> amigosNombres = entry.getValue().stream()
                    .map(Usuario::getUsername)
                    .sorted()
                    .toList();
            
            sb.append(String.join(", ", amigosNombres));
            sb.append("]\n");
        }
        
        return sb.toString();
    }
}

