package org.dubytube.dubytube.services;

import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.domain.Usuario;
import org.dubytube.dubytube.ds.MyLinkedList;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;

/**
 * Servicio de exportación de datos a CSV.
 * 
 * <p>Permite exportar:</p>
 * <ul>
 *   <li>Favoritos de un usuario específico</li>
 *   <li>Catálogo completo de canciones (administradores)</li>
 *   <li>Lista de usuarios (administradores)</li>
 * </ul>
 * 
 * @author DubyTube Team
 * @version 2.0 - Exportación completa para admins
 * @since 2025-11-18
 */
public class ExportarServices {
    
    /**
     * Exporta los favoritos de un usuario a CSV.
     * 
     * @param u Usuario
     * @param destino Ruta del archivo CSV
     * @return Path del archivo creado
     * @throws IOException Si ocurre un error al escribir
     */
    public static Path exportFavoritos(Usuario u, Path destino) throws IOException {
        MyLinkedList<Cancion> fav = u.getFavoritos();
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        
        StringBuilder sb = new StringBuilder("id,titulo,artista,genero,anio,duracionSeg\n");
        for (Cancion c : fav) {
            sb.append(c.getId()).append(',')
                    .append(esc(c.getTitulo())).append(',')
                    .append(esc(c.getArtista())).append(',')
                    .append(esc(c.getGenero())).append(',')
                    .append(c.getAnio()).append(',')
                    .append(c.getDuracionSeg()).append('\n');
        }
        
        return Files.writeString(destino, sb.toString(), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * Exporta el catálogo completo de canciones a CSV.
     * Solo para administradores.
     * 
     * @param canciones Colección de canciones
     * @param destino Ruta del archivo CSV
     * @return Path del archivo creado
     * @throws IOException Si ocurre un error al escribir
     */
    public static Path exportCatalogoCanciones(Collection<Cancion> canciones, Path destino) throws IOException {
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        
        StringBuilder sb = new StringBuilder("id,titulo,artista,genero,anio,duracionSeg,archivoAudio,subidaPor\n");
        
        for (Cancion c : canciones) {
            sb.append(esc(c.getId())).append(',')
                    .append(esc(c.getTitulo())).append(',')
                    .append(esc(c.getArtista())).append(',')
                    .append(esc(c.getGenero())).append(',')
                    .append(c.getAnio()).append(',')
                    .append(c.getDuracionSeg()).append(',')
                    .append(esc(c.getArchivoAudio())).append(',')
                    .append(esc(c.getSubidaPor())).append('\n');
        }
        
        System.out.println("✓ Exportadas " + canciones.size() + " canciones a: " + destino);
        
        return Files.writeString(destino, sb.toString(), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * Exporta la lista de usuarios a CSV.
     * Solo para administradores. NO exporta contraseñas por seguridad.
     * 
     * @param usuarios Colección de usuarios
     * @param destino Ruta del archivo CSV
     * @return Path del archivo creado
     * @throws IOException Si ocurre un error al escribir
     */
    public static Path exportUsuarios(Collection<Usuario> usuarios, Path destino) throws IOException {
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        
        StringBuilder sb = new StringBuilder("username,nombre,role,cantidadFavoritos\n");
        
        for (Usuario u : usuarios) {
            int cantFav = 0;
            for (@SuppressWarnings("unused") Cancion c : u.getFavoritos()) {
                cantFav++;
            }
            
            sb.append(esc(u.getUsername())).append(',')
                    .append(esc(u.getNombre())).append(',')
                    .append(u.getRole()).append(',')
                    .append(cantFav).append('\n');
        }
        
        System.out.println("✓ Exportados " + usuarios.size() + " usuarios a: " + destino);
        
        return Files.writeString(destino, sb.toString(), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * Exporta estadísticas de géneros musicales a CSV.
     * Agrupa las canciones por género y cuenta cuántas hay de cada uno.
     * Solo para administradores.
     * 
     * @param canciones Colección de canciones
     * @param destino Ruta del archivo CSV
     * @return Cantidad de géneros únicos exportados
     * @throws IOException Si ocurre un error al escribir
     */
    public static int exportGeneros(Collection<Cancion> canciones, Path destino) throws IOException {
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        
        // Agrupar canciones por género usando HashMap manual
        java.util.HashMap<String, Integer> generoCount = new java.util.HashMap<>();
        for (Cancion c : canciones) {
            String genero = c.getGenero();
            if (genero == null || genero.isBlank()) {
                genero = "Sin género";
            }
            generoCount.put(genero, generoCount.getOrDefault(genero, 0) + 1);
        }
        
        // Ordenar géneros por cantidad (descendente)
        java.util.List<java.util.Map.Entry<String, Integer>> sorted = 
            new java.util.ArrayList<>(generoCount.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // Crear CSV
        StringBuilder sb = new StringBuilder("genero,cantidadCanciones,porcentaje\n");
        int total = canciones.size();
        
        for (java.util.Map.Entry<String, Integer> entry : sorted) {
            String genero = entry.getKey();
            int cantidad = entry.getValue();
            double porcentaje = (cantidad * 100.0) / total;
            
            sb.append(esc(genero)).append(',')
                    .append(cantidad).append(',')
                    .append(String.format("%.2f%%", porcentaje)).append('\n');
        }
        
        System.out.println("✓ Exportados " + generoCount.size() + " géneros a: " + destino);
        
        Files.writeString(destino, sb.toString(), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        return generoCount.size();
    }
    
    /**
     * Escapa caracteres problemáticos en CSV (comas, comillas).
     */
    private static String esc(String s) {
        if (s == null) return "";
        // Si contiene coma o comillas, envolver en comillas y escapar comillas internas
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
