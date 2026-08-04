# Sistema de Gestión Organizacional

Sistema de escritorio desarrollado como proyecto final universitario. La aplicacion permite administrar catalogos, empleados, empresas, proyectos, asignaciones, usuarios, autenticacion, permisos, fotografias y exportaciones.

## Objetivo universitario

Demostrar el uso integrado de Java moderno, programacion orientada a objetos, arquitectura MVC, patron DAO, persistencia SQLite con JDBC, interfaz grafica Swing MDI y empaquetado profesional con Maven.

## Tecnologias

- Java 24
- Maven 3.9+
- Swing
- JDesktopPane y JInternalFrame
- SQLite
- JDBC
- FlatLaf
- Apache POI
- OpenPDF
- JUnit Jupiter

## Requisitos

- JDK 24 configurado en `JAVA_HOME`.
- Maven disponible en el `PATH`.
- Sistema operativo con entorno grafico para ejecutar Swing.

## Arquitectura

El proyecto usa MVC con DAO:

- `model`: entidades y relaciones de objetos.
- `dao`: acceso a SQLite mediante JDBC y `PreparedStatement`.
- `controller`: validaciones, reglas de negocio, autenticacion y autorizacion.
- `view`: formularios Swing, tablas, menus y dialogos.
- `utils`: utilidades de contrasenas y exportacion.
- `config`: configuracion y conexion SQLite.
- `resources`: propiedades y script SQL.

Las vistas no ejecutan SQL. Los DAO no muestran ventanas. Los controladores no crean componentes Swing.

## Entidades

- Pais
- Departamento
- Cargo con salario base
- Persona
- Empleado con pais, departamento y cargo
- Empresa con departamentos, empleados y proyectos
- Proyecto
- Asignacion
- Rol
- Usuario

## Relaciones

- Pais se conserva como catalogo independiente de ubicacion.
- Una empresa pertenece a un pais.
- Una empresa tiene departamentos organizacionales.
- Una empresa agrupa empleados como coleccion POO, a traves de su estructura organizacional.
- Un departamento organizacional pertenece a una empresa.
- Un proyecto pertenece a una empresa.
- Un empleado hereda datos de persona, tiene pais, cargo y departamento.
- Un cargo tiene salario base de referencia.
- Una asignacion relaciona empleado y proyecto.
- Un usuario pertenece a un rol.

## Base de datos

SQLite se inicializa con `src/main/resources/database/schema.sql`.

Incluye:

- Claves primarias.
- Claves foraneas.
- Restricciones `UNIQUE`.
- Restricciones `CHECK`.
- Indices.
- Datos iniciales.
- Usuarios iniciales.
- Migraciones idempotentes para compatibilidad con bases anteriores.
- `empleado.pais_id` como relacion obligatoria con `pais`.
- `cargo.salario_base` como referencia salarial del puesto.
- Migraciones idempotentes para bases anteriores sin `pais_id` en `empleado` o `salario_base` en `cargo`.

La ruta por defecto de la base es:

```properties
database.path=data/sistema_universitario.db
```

## Modulos disponibles

- Paises
- Departamentos
- Cargos
- Empresas
- Empleados
- Proyectos
- Asignaciones
- Usuarios

Los modulos usan busqueda instantanea, ordenamiento por JTable, validaciones visuales y mensajes amigables.

## Roles y permisos

Administrador:

- Consulta registros.
- Inserta, actualiza y elimina.
- Administra empresas.
- Administra usuarios.
- Exporta PDF y Excel.
- Cambia su contrasena.

Consulta:

- Abre modulos.
- Consulta, busca y ordena registros.
- Exporta PDF y Excel.
- Cambia su propia contrasena.
- No inserta, actualiza, elimina ni administra usuarios.

## Credenciales iniciales

```text
Usuario: admin
Contrasena: Admin123*

Usuario: consulta
Contrasena: Consulta123*
```

Ambos usuarios iniciales deben cambiar la contrasena al primer ingreso.

## Autenticacion y contrasenas

La autenticacion se realiza contra SQLite. Las contrasenas no se guardan en texto plano; se almacenan como hash SHA-256. El sistema rechaza usuarios inexistentes, usuarios inactivos y credenciales incorrectas con mensajes genericos.

## Manejo de fotografias

El modulo Empleados permite seleccionar una imagen, validar que exista, mostrar vista previa y guardar unicamente la ruta del archivo. Si la imagen ya no existe, el sistema muestra un aviso comprensible.

Formatos permitidos:

- JPG
- JPEG
- PNG
- GIF
- BMP

## Exportaciones

Desde los modulos Empleados y Proyectos se puede exportar el contenido visible del JTable a:

- Excel `.xlsx`
- PDF `.pdf`

La exportacion respeta busqueda, ordenamiento y valores visibles.

## Configuracion

Archivo:

```text
src/main/resources/application.properties
```

Propiedades principales:

```properties
app.name=Sistema de Gestión Organizacional
app.version=1.0.0
app.company=Proyecto Final Universitario
database.path=data/sistema_universitario.db
```

## Compilacion con Maven

```bash
mvn clean compile
```

## Ejecucion desde Maven

```bash
mvn exec:java
```

## Ejecucion desde NetBeans

1. Abrir NetBeans.
2. Seleccionar `File > Open Project`.
3. Elegir la carpeta del proyecto.
4. Verificar que el JDK del proyecto sea Java 24.
5. Ejecutar la clase `edu.university.system.Main`.

## Generacion del JAR

```bash
mvn clean package
```

El JAR ejecutable con dependencias se genera en:

```text
target/sistema-universitario-1.0.0.jar
```

Ejecucion:

```bash
java -jar target/sistema-universitario-1.0.0.jar
```

## Pruebas

Las pruebas automatizadas usan SQLite temporal y no modifican la base real.

```bash
mvn clean test
```

Cubren autenticacion, autorizacion, validaciones de controladores, DAO, integridad referencial, transacciones de empleado y exportacion PDF/Excel.
Tambien verifican que Departamento sea una unidad organizacional asociada a Empresa, que Empleado conserve pais, cargo y departamento, que Cargo conserve salario base y que la migracion desde bases antiguas sea idempotente.

## Solucion de problemas

`release version 24 not supported`:

- Instalar JDK 24.
- Configurar `JAVA_HOME`.
- Confirmar con `java -version` y `javac -version`.

`mvn no se reconoce`:

- Instalar Maven.
- Agregar `MAVEN_HOME/bin` al `PATH`.

No se puede abrir la base:

- Revisar permisos de escritura en la carpeta configurada en `database.path`.
- Verificar que la aplicacion tenga acceso al directorio `data`.

No inicia el esquema:

- Confirmar que `database/schema.sql` exista dentro de `src/main/resources`.

Error al eliminar registros:

- Puede existir integridad referencial. Elimine primero registros relacionados o conserve el dato como historico.

Error al exportar:

- Verificar que el archivo no este abierto en Excel o un lector PDF.
- Revisar permisos de escritura en la carpeta destino.

Fotografia no visible:

- Confirmar que la ruta guardada exista y apunte a una imagen valida.

## Autor

Rony Turcios, programador independiente.
