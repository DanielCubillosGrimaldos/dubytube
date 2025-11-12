package org.dubytube.dubytube.services;

import org.dubytube.dubytube.domain.Usuario;

public final class Session {
    private static Usuario current;
    private Session(){}
    public static void set(Usuario u){ current = u; }
    public static Usuario get(){ return current; }
    public static boolean isLogged(){ return current != null; }
    public static void clear(){ current = null; }
}
