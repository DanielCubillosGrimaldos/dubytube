package org.dubytube.dubytube.services;

import org.dubytube.dubytube.domain.Cancion;
import org.dubytube.dubytube.domain.Usuario;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class ExportarServices {
    public static Path exportFavoritos(Usuario u, Path destino) throws IOException {
        List<Cancion> fav = u.getFavoritos();
        if (destino.getParent()!=null) Files.createDirectories(destino.getParent());
        StringBuilder sb = new StringBuilder("id,titulo,artista,genero,anio,duracionSeg\n");
        for (Cancion c : fav) {
            sb.append(c.getId()).append(',')
                    .append(esc(c.getTitulo())).append(',')
                    .append(esc(c.getArtista())).append(',')
                    .append(esc(c.getGenero())).append(',')
                    .append(c.getAnio()).append(',')
                    .append(c.getDuracionSeg()).append('\n');
        }
        return Files.writeString(destino, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    private static String esc(String s){ return (s==null)?"":s.replace(",", " "); }
}
