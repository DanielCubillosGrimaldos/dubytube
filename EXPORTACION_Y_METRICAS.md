# 📊 Exportación CSV y Panel de Métricas - Implementación

## ✅ Funcionalidades Implementadas

**Fecha:** 18 de Noviembre, 2025  
**Requisitos:** RF-014 - Visualización de métricas con JavaFX Charts

---

## 🎯 Objetivo

1. **Arreglar exportación CSV** para administradores
2. **Crear panel de métricas** con JavaFX Charts (solo administradores)

---

## 📤 1. Exportación de Datos (CSV)

### Archivos Modificados:

#### **ExportarServices.java** - Completamente mejorado

**Nuevos métodos agregados:**

```java
/**
 * Exporta el catálogo completo de canciones a CSV.
 * Solo para administradores.
 */
public static Path exportCatalogoCanciones(Collection<Cancion> canciones, Path destino)

/**
 * Exporta la lista de usuarios a CSV.
 * Solo para administradores. NO exporta contraseñas por seguridad.
 */
public static Path exportUsuarios(Collection<Usuario> usuarios, Path destino)
```

**Formato CSV mejorado:**

- **Canciones:** `id,titulo,artista,genero,anio,duracionSeg,archivoAudio,subidaPor`
- **Usuarios:** `username,nombre,role,cantidadFavoritos` (sin contraseñas)
- Manejo inteligente de comas y comillas en los datos

#### **ImportController.java** - Nuevos métodos

```java
@FXML
private void onExportarCanciones() {
    // Abre FileChooser, exporta todas las canciones
    // Muestra confirmación con cantidad exportada
}

@FXML
private void onExportarUsuarios() {
    // Abre FileChooser, exporta todos los usuarios
    // Muestra confirmación con cantidad exportada
}
```

#### **ImportView.fxml** - Nueva sección de exportación

```fxml
<!-- Export Section -->
<HBox spacing="12">
    <VBox spacing="12" styleClass="card">
        <Label text="📥 EXPORTAR DATOS" />
        <Label text="Exporta el catálogo completo o la lista de usuarios a CSV" />
        <HBox spacing="8">
            <Button text="📊 Exportar Canciones" onAction="#onExportarCanciones" />
            <Button text="👥 Exportar Usuarios" onAction="#onExportarUsuarios" />
        </HBox>
    </VBox>
</HBox>
```

### Flujo de Exportación:

```
Usuario Admin → Menú Principal → Import/Export
    ↓
Botón "Exportar Canciones" o "Exportar Usuarios"
    ↓
FileChooser aparece (seleccionar ubicación y nombre)
    ↓
ExportarServices genera CSV
    ↓
Mensaje de confirmación con ruta del archivo
    ↓
Archivo CSV guardado en disco
```

### Ejemplo de CSV exportado:

**canciones_1731976800000.csv:**

```csv
id,titulo,artista,genero,anio,duracionSeg,archivoAudio,subidaPor
c1,Love Song,Adele,Pop,2015,210,love-song.mp3,admin
c2,Lobo Hombre,La Unión,Rock,1984,245,lobo-hombre.mp3,admin
c3,Ave Maria,Schubert,Clásica,1825,150,ave-maria.mp3,admin
```

**usuarios_1731976800000.csv:**

```csv
username,nombre,role,cantidadFavoritos
admin,Administrador,ADMIN,3
daniel,Daniel Cubillos,USER,5
maria,María García,USER,2
```

---

## 📊 2. Panel de Métricas (JavaFX Charts)

### Archivos Creados:

#### **MetricasController.java** - Nuevo controlador

**Gráficos implementados:**

1. **PieChart - Distribución por Género**

   - Muestra top 10 géneros
   - Ordenado por cantidad de canciones
   - Con leyenda inferior

2. **BarChart - Top 10 Artistas**

   - Artistas con más canciones en el catálogo
   - Eje X: Artista, Eje Y: Cantidad
   - Sin leyenda (visual limpio)

3. **LineChart - Canciones por Década**

   - Agrupa canciones en décadas (1980s, 1990s, etc.)
   - Muestra tendencias temporales
   - Con símbolos en los puntos

4. **AreaChart - Duración Promedio por Género**
   - Calcula duración promedio en segundos
   - Top 10 géneros por duración
   - Visualización tipo área

**Contadores generales:**

```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ 🎵 Canciones│ 👥 Usuarios │ 🎸 Géneros  │ 🎤 Artistas │
│     250     │     45      │     12      │     87      │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

#### **MetricasView.fxml** - Vista con diseño profesional

**Estructura:**

```
┌─────────────────────────────────────────┐
│  📊 Panel de Métricas        🔄 ← Volver│
├─────────────────────────────────────────┤
│  [4 Cards con contadores]               │
├─────────────────────────────────────────┤
│ ┌──────────────┬───────────────────────┐│
│ │ PieChart     │ BarChart              ││
│ │ Géneros      │ Artistas              ││
│ └──────────────┴───────────────────────┘│
├─────────────────────────────────────────┤
│ ┌──────────────┬───────────────────────┐│
│ │ LineChart    │ AreaChart             ││
│ │ Décadas      │ Duraciones            ││
│ └──────────────┴───────────────────────┘│
├─────────────────────────────────────────┤
│ 💡 Info: RF-014 implementado            │
└─────────────────────────────────────────┘
```

**Características:**

- ScrollPane para visualización completa
- Cards con colores diferentes para cada contador
- Gráficos con títulos descriptivos
- Botón de "Refrescar" para actualizar datos
- Footer informativo

#### **MainController.java** - Navegación agregada

```java
@FXML private void goMetricas() {
    go("/view/MetricasView.fxml", "Panel de Métricas");
}
```

#### **MainView.fxml** - Card de Métricas agregado

```fxml
<!-- Card: Métricas (Nueva fila) -->
<VBox styleClass="card" onMouseClicked="#goMetricas">
    <Text text="📊" />
    <Label text="Métricas" />
    <Label text="Panel de estadísticas" />
</VBox>
```

### Algoritmos de Procesamiento:

#### 1. Distribución por Género (PieChart)

```java
Map<String, Long> generos = canciones.stream()
    .filter(c -> c.getGenero() != null && !c.getGenero().isBlank())
    .collect(Collectors.groupingBy(
        Cancion::getGenero,
        Collectors.counting()
    ));

generos.entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(10) // Top 10
    .forEach(entry -> {
        chartGeneros.getData().add(
            new PieChart.Data(entry.getKey(), entry.getValue())
        );
    });
```

#### 2. Top Artistas (BarChart)

```java
Map<String, Long> artistas = canciones.stream()
    .filter(c -> c.getArtista() != null && !c.getArtista().isBlank())
    .collect(Collectors.groupingBy(
        Cancion::getArtista,
        Collectors.counting()
    ));

XYChart.Series<String, Number> serie = new XYChart.Series<>();
artistas.entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(10)
    .forEach(entry -> {
        serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
    });
```

#### 3. Canciones por Década (LineChart)

```java
Map<String, Long> decadas = canciones.stream()
    .collect(Collectors.groupingBy(
        c -> {
            int decada = (c.getAnio() / 10) * 10;
            return decada + "s"; // "1980s", "1990s", etc.
        },
        Collectors.counting()
    ));

XYChart.Series<String, Number> serie = new XYChart.Series<>();
decadas.entrySet().stream()
    .sorted(Map.Entry.comparingByKey())
    .forEach(entry -> {
        serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
    });
```

#### 4. Duración Promedio (AreaChart)

```java
Map<String, Double> duraciones = canciones.stream()
    .filter(c -> c.getGenero() != null && !c.getGenero().isBlank())
    .collect(Collectors.groupingBy(
        Cancion::getGenero,
        Collectors.averagingInt(Cancion::getDuracionSeg)
    ));

XYChart.Series<String, Number> serie = new XYChart.Series<>();
duraciones.entrySet().stream()
    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
    .limit(10)
    .forEach(entry -> {
        serie.getData().add(new XYChart.Data<>(
            entry.getKey(),
            Math.round(entry.getValue())
        ));
    });
```

---

## 🎨 Diseño Visual

### Cards de Contadores:

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   🎵         │  │   👥         │  │   🎸         │  │   🎤         │
│ Canciones    │  │ Usuarios     │  │ Géneros      │  │ Artistas     │
│    250       │  │    45        │  │    12        │  │    87        │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
  Azul/Accent      Verde            Naranja           Morado
```

### Gráficos:

**PieChart:**

- Colores automáticos por JavaFX
- Leyenda en la parte inferior
- Sin valores en las slices (más limpio)

**BarChart:**

- Barras verticales
- Colores consistentes con el tema
- Sin leyenda (auto-explicativo)

**LineChart:**

- Línea con símbolos en puntos
- Bueno para tendencias temporales
- Color accent del tema

**AreaChart:**

- Área sombreada bajo la línea
- Ideal para comparar magnitudes
- Suave y visualmente agradable

---

## 🔒 Seguridad

### Control de Acceso:

```java
// En MainController.initialize()
if (Session.isLogged()) {
    Usuario u = Session.get();
    boolean isAdmin = (u.getRole() == Role.ADMIN);
    if (adminSection != null) {
        adminSection.setVisible(isAdmin); // ✅ Solo admins ven la sección
    }
}
```

**Vistas protegidas:**

- ✅ Import/Export (botones visibles solo para admins)
- ✅ Panel de Métricas (accesible solo desde sección admin)
- ✅ CRUD Canciones
- ✅ Gestión de Usuarios
- ✅ Gestión de Géneros

### Exportación Segura:

```java
// ExportarServices.exportUsuarios()
// NO exporta contraseñas por seguridad
StringBuilder sb = new StringBuilder("username,nombre,role,cantidadFavoritos\n");
// ❌ password NO incluido
```

---

## 🧪 Cómo Probar

### Test 1: Exportar Canciones

1. **Login como admin** (admin/123)
2. **Click en "Import/Export"** (sección de administrador)
3. **Click en "📊 Exportar Canciones"**
4. **Seleccionar ubicación** en el FileChooser
5. **Verificar CSV creado:**
   ```bash
   cat ~/Downloads/canciones_*.csv
   ```
6. **Verificar formato:**
   - Header: `id,titulo,artista,genero,anio,duracionSeg,archivoAudio,subidaPor`
   - Datos: correctamente separados por comas
   - Comillas escapadas si hay caracteres especiales

### Test 2: Exportar Usuarios

1. **Login como admin**
2. **Ir a "Import/Export"**
3. **Click en "👥 Exportar Usuarios"**
4. **Seleccionar ubicación**
5. **Verificar CSV:**
   ```bash
   cat ~/Downloads/usuarios_*.csv
   ```
6. **Verificar que NO hay contraseñas** (seguridad)

### Test 3: Visualizar Métricas

1. **Login como admin**
2. **Click en "📊 Métricas"** (card en sección admin)
3. **Verificar que se muestren:**
   - ✅ 4 contadores en la parte superior
   - ✅ PieChart de géneros (con datos)
   - ✅ BarChart de artistas (top 10)
   - ✅ LineChart de décadas
   - ✅ AreaChart de duraciones
4. **Click en "🔄 Refrescar"**
5. **Verificar que los gráficos se actualicen**

### Test 4: Control de Acceso

1. **Login como usuario normal** (daniel/123)
2. **Verificar que NO se ve:**
   - ❌ Sección de administrador
   - ❌ Botón de Métricas
   - ❌ Botón de Import/Export
3. **Login como admin**
4. **Verificar que SÍ se ve:**
   - ✅ Toda la sección de administrador
   - ✅ 5 cards (Canciones, Usuarios, Géneros, Import/Export, Métricas)

---

## 📊 Ejemplos de Visualización

### PieChart - Géneros:

```
         Pop (45) ██████████████████
        Rock (32) ████████████
     Clásica (18) ██████
        Jazz (12) ████
         Rap (8)  ██
```

### BarChart - Artistas:

```
Adele          ████████████ 12
Ed Sheeran     ██████████ 10
Queen          ████████ 8
The Beatles    ███████ 7
Metallica      ██████ 6
```

### LineChart - Décadas:

```
  │
50│         ●
  │        / \
40│       /   \
  │      /     \
30│     /       \
  │    /         \
20│   ●           ●───●
  │  /                 \
10│ ●                   ●
  │
  └──────────────────────────
   1950s 1960s ... 2020s
```

### AreaChart - Duración Promedio:

```
300│         ███████
   │        ████████
250│       █████████
   │      ██████████
200│     ███████████
   │    ████████████
150│   █████████████
   │  ██████████████
100│ ███████████████
   └──────────────────
    Pop Rock Jazz Rap
```

---

## ✅ Checklist de Implementación

### Exportación CSV:

- [x] Método `exportCatalogoCanciones()` en ExportarServices
- [x] Método `exportUsuarios()` en ExportarServices
- [x] Método `onExportarCanciones()` en ImportController
- [x] Método `onExportarUsuarios()` en ImportController
- [x] Botones de exportación en ImportView.fxml
- [x] FileChooser para seleccionar ubicación
- [x] Mensajes de confirmación
- [x] Logs en consola
- [x] Seguridad: NO exportar contraseñas

### Panel de Métricas:

- [x] MetricasController.java creado
- [x] MetricasView.fxml creado
- [x] PieChart - Distribución por género
- [x] BarChart - Top 10 artistas
- [x] LineChart - Canciones por década
- [x] AreaChart - Duración promedio por género
- [x] 4 contadores generales (canciones, usuarios, géneros, artistas)
- [x] Botón de refrescar
- [x] Navegación desde MainView
- [x] Card de Métricas en menú principal
- [x] Control de acceso (solo admins)
- [x] Diseño responsive con ScrollPane
- [x] Estilo consistente con app.css

### Integración:

- [x] Método `goMetricas()` en MainController
- [x] Card de Métricas en MainView.fxml
- [x] Texto actualizado "Import/Export" en lugar de solo "Import"
- [x] Compilación exitosa: **BUILD SUCCESS**

---

## 🎯 Requisitos Cumplidos

### RF-014: Visualización de métricas con JavaFX Charts

✅ **PieChart de géneros:** Muestra distribución de canciones por género  
✅ **BarChart de artistas más populares:** Top 10 artistas por cantidad de canciones  
✅ **LineChart de décadas:** Tendencia temporal de canciones  
✅ **AreaChart de duraciones:** Duración promedio por género

### Funcionalidad de Exportación:

✅ **Exportar catálogo completo:** Todas las canciones a CSV  
✅ **Exportar usuarios:** Lista de usuarios sin contraseñas  
✅ **FileChooser integrado:** Seleccionar ubicación de guardado  
✅ **Mensajes de confirmación:** Feedback al usuario  
✅ **Seguridad:** Solo administradores tienen acceso

---

## 🚀 Próximas Mejoras Sugeridas

### 1. Más Gráficos

- **StackedBarChart:** Canciones por género y década
- **ScatterChart:** Duración vs Año
- **BubbleChart:** Popularidad vs Duración (tamaño = cantidad de favoritos)

### 2. Filtros Interactivos

```java
@FXML private ComboBox<String> cboFiltroGenero;
@FXML private DatePicker dpDesde, dpHasta;

@FXML
private void onFiltrar() {
    String genero = cboFiltroGenero.getValue();
    // Recargar gráficos con filtro aplicado
}
```

### 3. Exportar Gráficos como Imagen

```java
@FXML
private void onExportarGrafico() {
    WritableImage image = chartGeneros.snapshot(null, null);
    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File("grafico.png"));
}
```

### 4. Métricas en Tiempo Real

```java
// Actualizar cada 5 segundos
Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
    cargarMetricas();
}));
timeline.setCycleCount(Timeline.INDEFINITE);
timeline.play();
```

### 5. Comparación de Periodos

- Comparar mes actual vs mes anterior
- Tendencias de crecimiento
- Usuarios activos vs inactivos

---

## 🎉 Resultado Final

### ✅ Exportación CSV:

- **Funciona correctamente** la exportación de canciones y usuarios
- **FileChooser** integrado para seleccionar ubicación
- **Formato CSV válido** con escape de caracteres especiales
- **Seguro:** No exporta contraseñas

### ✅ Panel de Métricas:

- **4 gráficos diferentes:** PieChart, BarChart, LineChart, AreaChart
- **Contadores generales:** Canciones, Usuarios, Géneros, Artistas
- **Diseño profesional:** Cards con colores, ScrollPane, responsive
- **Solo para admins:** Control de acceso implementado
- **RF-014 completamente implementado**

**¡Sistema de exportación y métricas completamente funcional! 📊✨**

---

**Autor:** GitHub Copilot  
**Fecha:** 18 de Noviembre, 2025  
**Versión:** 1.0 - Exportación CSV + Panel de Métricas
