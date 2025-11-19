# Directorio de Archivos de Audio

Este directorio almacena los archivos de audio asociados a las canciones en DubyTube.

## Formatos Soportados

- **MP3** (`.mp3`) - Recomendado
- **WAV** (`.wav`)
- **M4A** (`.m4a`)
- **FLAC** (`.flac`)
- **OGG** (`.ogg`)

## Cómo Funciona

1. Cuando un usuario sube una canción desde el panel de CRUD, puede seleccionar un archivo de audio
2. El sistema copia automáticamente el archivo a este directorio
3. El archivo se renombra usando el ID de la canción más su extensión original
4. La ruta relativa se guarda en el campo `archivoAudio` de la entidad `Cancion`

## Estructura de Archivos

```
audio/
├── {id-cancion-1}.mp3
├── {id-cancion-2}.wav
├── {id-cancion-3}.mp3
└── ...
```

## Radio Player

El `RadioService` lee los archivos desde este directorio usando la siguiente ruta:

```
src/main/resources/audio/{nombreArchivo}
```

## Permisos de Eliminación

- Los **usuarios** solo pueden eliminar canciones (y sus archivos de audio) que ellos mismos subieron
- Los **administradores** pueden eliminar cualquier canción y su audio asociado

## Notas Importantes

⚠️ **No editar manualmente**: Los archivos en este directorio son gestionados automáticamente por la aplicación.

⚠️ **Respaldo**: Considera hacer respaldos periódicos de este directorio si contiene archivos importantes.

✅ **Git**: Este directorio está incluido en el control de versiones, pero los archivos de audio grandes pueden ser excluidos mediante `.gitignore` si es necesario.
