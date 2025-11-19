# DubyTube - Documentación y Pruebas Unitarias

## 📋 Resumen del Proyecto

DubyTube es un sistema de gestión musical desarrollado en Java 21 con JavaFX, que incluye:

- Sistema de autenticación de usuarios
- Búsqueda avanzada de canciones con concurrencia
- Sistema de recomendaciones basado en grafos de similitud
- Red social musical con grafo de conexiones
- Autocompletado de búsquedas con estructura Trie

## ✅ Pruebas Unitarias Implementadas

Se han implementado **35 pruebas unitarias** cubriendo más de **7 métodos clave** del sistema:

### 1. **AuthServiceTest** (4 tests)

- ✅ Login exitoso con credenciales correctas
- ✅ Login fallido con contraseña incorrecta
- ✅ Login fallido con usuario inexistente
- ✅ Login exitoso para usuario normal

**Clase probada:** `org.dubytube.dubytube.services.AuthService`
**Método clave:** `login(String username, String password)`

### 2. **GrafoSocialTest** (7 tests)

- ✅ Agregar usuarios al grafo
- ✅ Crear amistad bidireccional
- ✅ Obtener amigos de un usuario
- ✅ Eliminar amistad
- ✅ Encontrar amigos de amigos (BFS - RF-024)
- ✅ No se puede crear amistad consigo mismo
- ✅ Amistad duplicada no se agrega dos veces

**Clase probada:** `org.dubytube.dubytube.ds.GrafoSocial`
**Métodos clave:**

- `agregarUsuario(Usuario)`
- `agregarAmistad(Usuario, Usuario)`
- `getAmigos(Usuario)`
- `eliminarAmistad(Usuario, Usuario)`
- `encontrarAmigosDeAmigos(Usuario)` ⭐ BFS

### 3. **TrieTest** (9 tests)

- ✅ Insertar palabras en el Trie
- ✅ Búsqueda exacta de palabra
- ✅ Autocompletado por prefijo (RF-026)
- ✅ Prefijo vacío devuelve lista vacía
- ✅ Palabras duplicadas no se cuentan dos veces
- ✅ Soporte para caracteres especiales
- ✅ Prefijo sin coincidencias devuelve lista vacía
- ✅ No se puede insertar palabra null o vacía
- ✅ Autocompletado es case-insensitive

**Clase probada:** `org.dubytube.dubytube.ds.Trie`
**Métodos clave:**

- `insert(String word)`
- `search(String word)`
- `searchByPrefix(String prefix)` ⭐ Autocompletado
- `size()`

### 4. **BusquedaAvanzadaTest** (8 tests)

- ✅ Búsqueda por artista
- ✅ Búsqueda por género
- ✅ Búsqueda por rango de años
- ✅ Búsqueda combinada con lógica AND
- ✅ Búsqueda combinada con lógica OR
- ✅ Búsqueda sin filtros devuelve todo
- ✅ Búsqueda con substring en artista
- ✅ Búsqueda con año mínimo solamente

**Clase probada:** `org.dubytube.dubytube.services.BusquedaAvanzada`
**Método clave:** `buscar(String artista, String genero, Integer anioMin, Integer anioMax, Logica logica)` ⭐ Concurrencia

### 5. **RecomendacionServiceTest** (7 tests)

- ✅ Recomendaciones no incluyen la canción fuente
- ✅ Recomendaciones priorizan mismo artista
- ✅ Devuelve el número correcto de recomendaciones
- ✅ Recomendaciones con ID inexistente devuelve lista vacía
- ✅ Recomendaciones con ID null devuelve lista vacía
- ✅ Recomendaciones ordenadas por similitud
- ✅ Recomendaciones consideran género similar

**Clase probada:** `org.dubytube.dubytube.services.RecomendacionService`
**Método clave:** `recomendar(String sourceId, int k)` ⭐ Algoritmo de Dijkstra

## 📊 Resultados de Ejecución

```
Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: ~1.4s
```

**Cobertura de métodos clave: ✅ 100% de los 7+ métodos principales**

## 📚 Documentación JavaDoc

### RF-032: Generación de Documentación Completa

La documentación JavaDoc ha sido generada exitosamente con las siguientes características:

#### Configuración del Plugin JavaDoc

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.6.3</version>
    <configuration>
        <show>public</show>
        <encoding>UTF-8</encoding>
        <author>true</author>
        <version>true</version>
        <windowtitle>DubyTube - Documentación API</windowtitle>
        <doctitle>DubyTube - Sistema de Gestión Musical</doctitle>
    </configuration>
</plugin>
```

#### Ubicación de la Documentación

📁 **Ruta:** `target/site/apidocs/index.html`

#### Comando para Generar la Documentación

```bash
mvn javadoc:javadoc
```

#### Comando para Ver la Documentación

```bash
# En Linux/Mac
xdg-open target/site/apidocs/index.html

# En Windows
start target/site/apidocs/index.html

# O simplemente abrir el archivo en tu navegador
```

### Documentación Incluida

La documentación JavaDoc cubre:

#### 1. **Paquete `domain`**

- `Cancion` - Entidad de canción musical
- `Usuario` - Entidad de usuario del sistema
- `Role` - Enum de roles (USER, ADMIN)

#### 2. **Paquete `ds` (Estructuras de Datos)**

- `GrafoSocial` - Grafo no dirigido para red social
  - Implementación de BFS para amigos de amigos
  - Complejidad temporal documentada
- `GrafoSimilitud` - Grafo pesado para similitud de canciones
  - Implementación de Dijkstra
- `Trie` - Árbol de prefijos para autocompletado
  - Complejidad O(m) para inserción y búsqueda
  - Autocompletado en O(m + k)
- `MyLinkedList` - Lista enlazada personalizada

#### 3. **Paquete `services`**

- `AuthService` - Servicio de autenticación
- `BusquedaAvanzada` - Búsqueda concurrente con ExecutorService
- `CancionIndice` - Indexación de canciones con Trie
- `RecomendacionService` - Sistema de recomendaciones
- `ExportarServices` - Exportación de datos
- `Session` - Gestión de sesión de usuario

#### 4. **Paquete `repo`**

- `CancionRepo` - Repositorio de canciones
- `UsuarioRepo` - Repositorio de usuarios

#### 5. **Paquete `viewController`**

- Todos los controladores de vistas JavaFX documentados

### Estándares de Documentación

Cada clase incluye:

- ✅ **Descripción general** del propósito de la clase
- ✅ **Requisitos funcionales cumplidos** (RF-XXX)
- ✅ **Complejidad temporal** de algoritmos importantes
- ✅ **Ejemplos de uso** cuando es relevante
- ✅ **@param** - Descripción de cada parámetro
- ✅ **@return** - Descripción del valor de retorno
- ✅ **@throws** - Excepciones que pueden lanzarse
- ✅ **@author** - DubyTube Team
- ✅ **@version** - Número de versión
- ✅ **@since** - Fecha de creación

## 🚀 Comandos Principales

### Compilar el Proyecto

```bash
mvn clean compile
```

### Ejecutar Pruebas Unitarias

```bash
mvn test
```

### Generar Documentación JavaDoc

```bash
mvn javadoc:javadoc
```

### Ejecutar la Aplicación

```bash
mvn javafx:run
```

### Generar JavaDoc JAR (con documentación empaquetada)

```bash
mvn javadoc:jar
```

## 📦 Estructura de Archivos de Test

```
src/test/java/org/dubytube/dubytube/
├── AuthServiceTest.java           (4 tests)
├── GrafoSocialTest.java          (7 tests)
├── TrieTest.java                 (9 tests)
├── BusquedaAvanzadaTest.java     (8 tests)
└── RecomendacionServiceTest.java (7 tests)
```

## 🎯 Requisitos Cumplidos

### RF-032: JavaDoc

- ✅ Documentación completa generada
- ✅ Plugin configurado en pom.xml
- ✅ Todas las clases públicas documentadas
- ✅ Todos los métodos públicos documentados
- ✅ Descripción de parámetros y retornos
- ✅ Complejidad temporal especificada

### Pruebas Unitarias

- ✅ Cobertura de al menos 7 métodos clave
- ✅ 35 tests unitarios implementados
- ✅ 100% de tests pasando (0 failures, 0 errors)
- ✅ Tests para clases críticas:
  - AuthService (autenticación)
  - GrafoSocial (red social + BFS)
  - Trie (autocompletado)
  - BusquedaAvanzada (búsqueda concurrente)
  - RecomendacionService (algoritmo de Dijkstra)

## 📈 Estadísticas Finales

| Métrica                     | Valor     |
| --------------------------- | --------- |
| **Tests Totales**           | 35        |
| **Tests Exitosos**          | 35 (100%) |
| **Tests Fallidos**          | 0         |
| **Clases de Test**          | 5         |
| **Métodos Clave Cubiertos** | 7+        |
| **Tiempo de Ejecución**     | ~1.4s     |
| **Clases Documentadas**     | 40        |
| **Líneas de Código**        | ~5000+    |

## 🛠️ Tecnologías Utilizadas

- **Java 21** - Lenguaje de programación
- **JavaFX 21.0.6** - Framework de UI
- **JUnit 5.12.1** - Framework de pruebas unitarias
- **Maven 3.x** - Gestión de dependencias
- **Maven Javadoc Plugin 3.6.3** - Generación de documentación
- **Maven Surefire Plugin 3.2.5** - Ejecución de pruebas

## 📝 Notas Adicionales

- Las pruebas son independientes entre sí (no dependen del orden de ejecución)
- Cada test tiene su propio `@BeforeEach` para configuración inicial
- Los tests limpian los repositorios antes de ejecutarse
- La documentación JavaDoc está en formato HTML5
- Compatible con navegadores modernos

## 👨‍💻 Autor

**DubyTube Team** - 2025

---

**¡Documentación y pruebas unitarias completadas exitosamente! ✅**
