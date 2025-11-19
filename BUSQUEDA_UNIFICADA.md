# 🔍 Búsqueda Unificada - Estilo Spotify

## ✅ Implementación Completada

Se ha creado exitosamente una vista de búsqueda unificada que fusiona las funcionalidades de:

- ✅ `BuscarView` (búsqueda básica)
- ✅ `AvanzadaView` (búsqueda avanzada)

Los archivos antiguos han sido respaldados como `.old` y están disponibles para referencia.

---

## 🎯 Características Principales

### 1. **Autocompletado en Tiempo Real con Trie**

- 🔥 **Consulta instantánea**: Cada vez que escribes una letra, se consulta el Trie
- 📋 **Sugerencias inmediatas**: Muestra las 10 mejores coincidencias en un dropdown
- 🎯 **Búsqueda por prefijo**: El Trie está precargado con todas las canciones
- 💡 **Click para seleccionar**: Haz click en una sugerencia para llenar el campo de búsqueda

**Implementación técnica:**

```java
// Listener en tiempo real
txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
    if (newVal != null && !newVal.trim().isEmpty()) {
        mostrarSugerencias(newVal.trim());
    } else {
        ocultarSugerencias();
    }
});

// Consulta al Trie
List<Cancion> sugerencias = indice.sugerirPorTitulo(prefijo, 10);
```

---

### 2. **Sistema de Me Gusta (Favoritos) ❤️**

- ❤️ **Corazón rojo**: Canción marcada como favorita
- 🤍 **Corazón blanco**: Canción no favorita
- 🔄 **Toggle instantáneo**: Click en el botón para cambiar el estado
- 💾 **Persistencia**: Los favoritos se guardan en la lista del usuario

**Implementación técnica:**

```java
private void toggleFavorito(Cancion cancion) {
    Usuario usuario = Session.getUsuarioActual();
    if (usuario == null) return;

    MyLinkedList<Cancion> favoritos = usuario.getFavoritos();
    if (favoritos.contains(cancion)) {
        favoritos.remove(cancion);  // Quitar de favoritos
    } else {
        favoritos.add(cancion);      // Agregar a favoritos
    }

    tblResultados.refresh();  // Actualizar la tabla
}
```

---

### 3. **Filtros Avanzados**

Los filtros se aplican en conjunto con la búsqueda por texto:

#### **Filtro por Género**

- ComboBox con todos los géneros disponibles
- Opción "Todos los géneros" para no filtrar

#### **Filtro por Rango de Años**

- Campo "Año Desde": Filtra canciones >= año especificado
- Campo "Año Hasta": Filtra canciones <= año especificado
- Validación automática de años válidos

**Botones de filtro:**

- 🔍 **Aplicar Filtros**: Ejecuta la búsqueda con los filtros seleccionados
- 🔄 **Limpiar Filtros**: Resetea todos los filtros

---

### 4. **Búsqueda Combinada Inteligente**

El sistema combina dos estrategias de búsqueda:

1. **Búsqueda por Trie (Prefijo exacto)**

   - Rápida y eficiente
   - Prioriza coincidencias que empiezan con el texto buscado

2. **Búsqueda por Contains (Coincidencias parciales)**
   - Encuentra canciones que contengan el texto en cualquier parte
   - Busca en: título, artista y género

**Eliminación de duplicados:**

```java
LinkedHashMap<String, Cancion> mapaResultados = new LinkedHashMap<>();
// Primero: resultados del Trie
for (Cancion c : resultadosTrie) {
    mapaResultados.put(c.getId(), c);
}
// Segundo: resultados por contains (sin duplicar)
for (Cancion c : resultadosContains) {
    mapaResultados.putIfAbsent(c.getId(), c);
}
```

---

## 📋 Estructura de la Tabla de Resultados

| Columna  | Descripción                 | Ancho |
| -------- | --------------------------- | ----- |
| ♥        | Botón de favorito (❤️/🤍)   | 50px  |
| Título   | Nombre de la canción        | 250px |
| Artista  | Nombre del artista          | 200px |
| Género   | Género musical              | 130px |
| Año      | Año de lanzamiento          | 80px  |
| Duración | Duración en formato MM:SS   | 100px |
| 🎵       | Botón para reproducir audio | 60px  |

---

## 🎨 Diseño Estilo Spotify

### **Colores y Estilo**

- 🟢 **Verde Spotify**: #1DB954 (logo y acentos)
- ⚫ **Fondo oscuro**: Variables CSS de la app
- ⚪ **Texto blanco**: Para contraste en áreas oscuras
- 🔘 **Bordes redondeados**: 24px en el campo de búsqueda

### **Componentes Visuales**

- 🔍 Logo circular con ícono de búsqueda
- 📝 Campo de búsqueda grande y destacado (48px de altura)
- 📊 Lista de sugerencias con bordes verdes
- 📑 Tabla moderna con espaciado generoso

---

## 🔧 Archivos Modificados

### **Nuevos archivos:**

```
src/main/resources/view/BuscarView.fxml          (123 líneas)
src/main/java/.../viewController/BuscarController.java  (430+ líneas)
```

### **Archivos respaldados:**

```
src/main/resources/view/BuscarView.fxml.old
src/main/java/.../viewController/BuscarController.java.old
src/main/resources/view/AvanzadaView.fxml        (sin cambios, puede eliminarse)
src/main/java/.../viewController/AvanzadaController.java  (sin cambios, puede eliminarse)
```

---

## 🚀 Cómo Usar

1. **Abrir la vista de búsqueda** desde el menú principal
2. **Escribir en el campo de búsqueda**:
   - Las sugerencias aparecerán automáticamente
   - Haz click en una sugerencia o presiona Enter
3. **Aplicar filtros opcionales**:
   - Selecciona un género específico
   - Define un rango de años
   - Click en "🔍 Aplicar Filtros"
4. **Marcar favoritos**:
   - Click en el botón ♥ de cualquier canción
   - El corazón cambiará de 🤍 a ❤️
5. **Limpiar búsqueda**:
   - Borra el texto del campo de búsqueda
   - O usa "🔄 Limpiar Filtros"

---

## ✅ Estado de Compilación

```
✅ Proyecto compila sin errores
✅ Todos los imports correctos
✅ Métodos del controller coinciden con FXML
✅ API de MyLinkedList correctamente usada (add/remove)
```

**Última compilación exitosa:** 2025-11-18 18:47:13

---

## 📝 Notas Técnicas

### **Dependencias:**

- `CancionRepo`: Repositorio de canciones
- `CancionIndice`: Servicio que envuelve el Trie
- `Session`: Gestión del usuario actual
- `MyLinkedList`: Lista enlazada para favoritos

### **Métodos clave del controlador:**

```java
configurarAutocompletado()       // Configura el listener del TextField
mostrarSugerencias(String)       // Consulta el Trie y muestra resultados
buscarEnTiempoReal()             // Búsqueda combinada (Trie + contains)
configurarColumnaFavoritos()     // Crea el botón ❤️ en cada fila
toggleFavorito(Cancion)          // Agregar/quitar de favoritos
onAplicarFiltros()               // Filtrar por género y año
actualizarResultados(List)       // Actualiza la tabla y el contador
```

---

## 🎉 ¡Listo para Usar!

La búsqueda unificada está completamente implementada y lista para ser utilizada.
Ejecuta la aplicación y disfruta de la nueva experiencia de búsqueda estilo Spotify.

```bash
mvn clean javafx:run
```

---

## 🗑️ Archivos que Pueden Eliminarse (Opcional)

Si confirmas que la búsqueda unificada funciona correctamente, puedes eliminar:

- `AvanzadaView.fxml`
- `AvanzadaController.java`
- `BuscarView.fxml.old`
- `BuscarController.java.old`

**⚠️ Recomendación:** Prueba primero la funcionalidad antes de eliminar los respaldos.
