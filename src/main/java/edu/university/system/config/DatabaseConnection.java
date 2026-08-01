package edu.university.system.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexion JDBC hacia SQLite y aplica el esquema idempotente al
 * iniciar. Las migraciones internas solo completan compatibilidad de versiones
 * anteriores y no reemplazan datos creados por el usuario.
 */
public final class DatabaseConnection {

    private static final String SCHEMA_RESOURCE = "database/schema.sql";

    private final AppConfig appConfig;

    public DatabaseConnection(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Prepara la base SQLite y aplica el esquema idempotente. No elimina bases
     * existentes ni reemplaza datos de usuario; solo ejecuta migraciones puntuales
     * necesarias para compatibilidad.
     */
    public void initialize() {
        Path databasePath = appConfig.getDatabasePath();
        Path parentDirectory = databasePath.toAbsolutePath().getParent();

        try {
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = getConnection()) {
                migrateLegacyOrganizationalDepartmentModel(connection);
                applySchema(connection);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo preparar el directorio de la base de datos.", exception);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("No se encontro el controlador JDBC de SQLite.", exception);
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo establecer la conexion SQLite.", exception);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getJdbcUrl());
    }

    public String getJdbcUrl() {
        return "jdbc:sqlite:" + appConfig.getDatabasePath().toAbsolutePath();
    }

    private void applySchema(Connection connection) {
        try (InputStream inputStream = DatabaseConnection.class.getClassLoader().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (inputStream == null) {
                throw new IllegalStateException("No se encontro el script de base de datos: " + SCHEMA_RESOURCE);
            }
            String script = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            executeScript(connection, script);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer el script de base de datos.", exception);
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo aplicar el script de base de datos.", exception);
        }
    }

    private void executeScript(Connection connection, String script) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String sql : script.split(";")) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty()) {
                    statement.execute(trimmedSql);
                }
            }
            ensureEmpleadoPhotoColumn(connection);
            ensureKnownDevelopmentPasswords(connection);
        }
    }

    private void migrateLegacyOrganizationalDepartmentModel(Connection connection) throws SQLException {
        if (!tableExists(connection, "departamento")) {
            return;
        }
        boolean hasPaisId = columnExists(connection, "departamento", "pais_id");
        boolean hasEmpresaId = columnExists(connection, "departamento", "empresa_id");
        boolean empresaHasDepartamentoId = tableExists(connection, "empresa") && columnExists(connection, "empresa", "departamento_id");
        if (!hasPaisId && hasEmpresaId && !empresaHasDepartamentoId) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");
            statement.execute("BEGIN TRANSACTION");
            try {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS departamento_geografico_backup AS
                        SELECT * FROM departamento
                        """);
                if (tableExists(connection, "empresa") && empresaHasDepartamentoId) {
                    rebuildEmpresaWithoutDepartamento(statement);
                }
                rebuildDepartamentoAsOrganizational(statement, hasPaisId);
                statement.execute("COMMIT");
            } catch (SQLException exception) {
                statement.execute("ROLLBACK");
                throw exception;
            } finally {
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
    }

    private void rebuildEmpresaWithoutDepartamento(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS empresa_migracion (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pais_id INTEGER NOT NULL,
                    nombre TEXT NOT NULL COLLATE NOCASE,
                    rtn TEXT NOT NULL UNIQUE COLLATE NOCASE,
                    telefono TEXT,
                    correo_electronico TEXT UNIQUE COLLATE NOCASE,
                    direccion TEXT,
                    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
                    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
                    fecha_actualizacion TEXT,
                    FOREIGN KEY (pais_id) REFERENCES pais (id) ON UPDATE CASCADE ON DELETE RESTRICT,
                    CHECK (length(trim(nombre)) >= 2),
                    CHECK (length(trim(rtn)) >= 8),
                    CHECK (correo_electronico IS NULL OR instr(correo_electronico, '@') > 1)
                )
                """);
        statement.execute("""
                INSERT OR IGNORE INTO empresa_migracion (
                    id, pais_id, nombre, rtn, telefono, correo_electronico, direccion,
                    activo, fecha_creacion, fecha_actualizacion
                )
                SELECT id, pais_id, nombre, rtn, telefono, correo_electronico, direccion,
                       activo, fecha_creacion, fecha_actualizacion
                FROM empresa
                """);
        statement.execute("DROP TABLE empresa");
        statement.execute("ALTER TABLE empresa_migracion RENAME TO empresa");
    }

    private void rebuildDepartamentoAsOrganizational(Statement statement, boolean hasPaisId) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS departamento_migracion (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    empresa_id INTEGER NOT NULL,
                    nombre TEXT NOT NULL COLLATE NOCASE,
                    presupuesto NUMERIC NOT NULL DEFAULT 0 CHECK (presupuesto >= 0),
                    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
                    fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')),
                    fecha_actualizacion TEXT,
                    FOREIGN KEY (empresa_id) REFERENCES empresa (id) ON UPDATE CASCADE ON DELETE RESTRICT,
                    UNIQUE (empresa_id, nombre),
                    CHECK (length(trim(nombre)) >= 2)
                )
                """);
        statement.execute("""
                INSERT OR IGNORE INTO empresa (id, pais_id, nombre, rtn, telefono, correo_electronico, direccion)
                VALUES (1, 1, 'Universidad Tecnologica de Honduras', '08019999000001', '2234-5678', 'contacto@uth.local', 'Campus principal')
                """);
        if (!hasPaisId) {
            statement.execute("""
                    INSERT OR IGNORE INTO departamento_migracion (
                        id, empresa_id, nombre, presupuesto, activo, fecha_creacion, fecha_actualizacion
                    )
                    SELECT id, empresa_id, nombre, presupuesto, activo, fecha_creacion, fecha_actualizacion
                    FROM departamento
                    """);
        }
        statement.execute("DROP TABLE departamento");
        statement.execute("ALTER TABLE departamento_migracion RENAME TO departamento");
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (var statement = connection.prepareStatement("""
                SELECT COUNT(1)
                FROM sqlite_master
                WHERE type = 'table'
                  AND name = ?
                """)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

    private void ensureEmpleadoPhotoColumn(Connection connection) throws SQLException {
        boolean hasPhotoColumn = false;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(empleado)")) {
            while (resultSet.next()) {
                if ("foto_ruta".equalsIgnoreCase(resultSet.getString("name"))) {
                    hasPhotoColumn = true;
                    break;
                }
            }
        }
        if (!hasPhotoColumn) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE empleado ADD COLUMN foto_ruta TEXT");
            }
        }
    }

    private void ensureKnownDevelopmentPasswords(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    UPDATE usuario
                    SET password_hash = '0a5bc3e342432f1bad92ffd51b785343ec72906cdba6a26131060b008e786656',
                        password_algoritmo = 'SHA-256',
                        debe_cambiar_password = 1,
                        fecha_actualizacion = datetime('now')
                    WHERE nombre_usuario = 'admin'
                      AND password_hash = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'
                    """);
            statement.executeUpdate("""
                    UPDATE usuario
                    SET password_hash = '824ae985fa847e5c7b2aec9e7ecaf165a9093015b585110783dc79db1abcfa8b',
                        password_algoritmo = 'SHA-256',
                        debe_cambiar_password = 1,
                        fecha_actualizacion = datetime('now')
                    WHERE nombre_usuario = 'consulta'
                      AND password_hash = '7fa95c704c2defa7b1295d28bcddfd752bf9b594db18886dd64721183cfc47a5'
                    """);
        }
    }
}
