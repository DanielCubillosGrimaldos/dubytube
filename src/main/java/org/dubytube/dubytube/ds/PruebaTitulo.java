package org.dubytube.dubytube.ds;

import org.dubytube.dubytube.domain.Cancion;

import java.text.Normalizer;
import java.util.*;

public class PruebaTitulo {

    private static class Nodo {
        Map<Character, Nodo> hijos = new HashMap<>();
        boolean fin;
        List<Cancion> payload = new ArrayList<>(); // canciones con ese título
    }

    private final Nodo raiz = new Nodo();

    // normaliza: quita acentos, baja a minúsculas y limpia símbolos
    private static String norm(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");          // quita diacríticos
        t = t.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")        // solo letras/números/espacio
                .replaceAll("\\s+", " ")              // espacios múltiples -> uno
                .trim();
        return t;
    }

    public void insert(Cancion c) {
        String key = norm(c.getTitulo());
        if (key.isEmpty()) return;
        Nodo n = raiz;
        for (char ch : key.toCharArray()) {
            n = n.hijos.computeIfAbsent(ch, k -> new Nodo());
        }
        n.fin = true;
        if (!n.payload.contains(c)) n.payload.add(c);
    }

    /** Sugerencias por prefijo (hasta k resultados). */
    public List<Cancion> suggest(String prefix, int k) {
        String p = norm(prefix);
        Nodo n = raiz;
        for (char ch : p.toCharArray()) {
            n = n.hijos.get(ch);
            if (n == null) return Collections.emptyList();
        }
        List<Cancion> out = new ArrayList<>();
        Deque<Nodo> stack = new ArrayDeque<>();
        stack.push(n);
        while (!stack.isEmpty() && out.size() < k) {
            Nodo cur = stack.pop();
            if (cur.fin) {
                for (Cancion c : cur.payload) {
                    out.add(c);
                    if (out.size() == k) break;
                }
            }
            for (Nodo hijo : cur.hijos.values()) stack.push(hijo);
        }
        return out;
    }
}
