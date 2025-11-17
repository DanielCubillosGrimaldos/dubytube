// src/main/java/org/dubytube/dubytube/AppContext.java
package org.dubytube.dubytube;

import org.dubytube.dubytube.repo.CancionRepo;
import org.dubytube.dubytube.repo.UsuarioRepo;
import org.dubytube.dubytube.ds.GrafoSimilitud;
import org.dubytube.dubytube.ds.GrafoSocial;

public final class AppContext {
    private static final CancionRepo canciones   = new CancionRepo();
    private static final UsuarioRepo usuarios    = new UsuarioRepo();
    private static final GrafoSimilitud similitud = new GrafoSimilitud();
    private static final GrafoSocial social       = new GrafoSocial();

    private AppContext(){}

    public static CancionRepo canciones(){ return canciones; }
    public static UsuarioRepo usuarios(){ return usuarios; }
    public static GrafoSimilitud similitud(){ return similitud; }
    public static GrafoSocial social(){ return social; }
}
