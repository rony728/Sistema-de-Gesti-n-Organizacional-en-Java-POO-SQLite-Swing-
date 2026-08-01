package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Rol;
import edu.university.system.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO de usuarios. Encapsula autenticacion persistente, cambios de estado,
 * actualizacion de contrasena y consultas administrativas usando
 * PreparedStatement para todo dato recibido.
 */
public class UsuarioDao extends DaoSupport {

    private static final DateTimeFormatter SQLITE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String BASE_SELECT = """
            SELECT
                u.id AS usuario_id,
                u.nombre_usuario,
                u.password_hash,
                u.password_algoritmo,
                u.nombre_mostrar,
                u.correo_electronico,
                u.activo AS usuario_activo,
                u.debe_cambiar_password,
                u.ultimo_acceso,
                u.fecha_creacion AS usuario_fecha_creacion,
                u.fecha_actualizacion,
                r.id AS rol_id,
                r.nombre AS rol_nombre,
                r.descripcion AS rol_descripcion,
                r.activo AS rol_activo
            FROM usuario u
            INNER JOIN rol r ON r.id = u.rol_id
            """;
    private static final String FIND_BY_USERNAME_SQL = BASE_SELECT + " WHERE lower(u.nombre_usuario) = lower(?)";
    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE u.id = ?";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY u.nombre_usuario";
    private static final String SEARCH_SQL = BASE_SELECT + """
             WHERE lower(u.nombre_usuario) LIKE lower(?)
                OR lower(u.nombre_mostrar) LIKE lower(?)
                OR lower(coalesce(u.correo_electronico, '')) LIKE lower(?)
                OR lower(r.nombre) LIKE lower(?)
             ORDER BY u.nombre_usuario
            """;
    private static final String INSERT_SQL = """
            INSERT INTO usuario (
                rol_id, nombre_usuario, password_hash, password_algoritmo, nombre_mostrar,
                correo_electronico, activo, debe_cambiar_password
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_GENERAL_SQL = """
            UPDATE usuario
            SET rol_id = ?, nombre_mostrar = ?, correo_electronico = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String UPDATE_STATUS_SQL = """
            UPDATE usuario
            SET activo = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String UPDATE_PASSWORD_SQL = """
            UPDATE usuario
            SET password_hash = ?, password_algoritmo = ?, debe_cambiar_password = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String UPDATE_LAST_ACCESS_SQL = """
            UPDATE usuario
            SET ultimo_acceso = datetime('now')
            WHERE id = ?
            """;
    private static final String EXISTS_USERNAME_SQL = """
            SELECT 1
            FROM usuario
            WHERE lower(nombre_usuario) = lower(?)
              AND (? IS NULL OR id <> ?)
            LIMIT 1
            """;
    private static final String ACTIVE_ADMIN_COUNT_SQL = """
            SELECT COUNT(*)
            FROM usuario u
            INNER JOIN rol r ON r.id = u.rol_id
            WHERE u.activo = 1
              AND lower(r.nombre) = lower('Administrador')
            """;

    public UsuarioDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL)) {
            statement.setString(1, normalizeUsername(nombreUsuario));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUsuario(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el usuario.", exception);
        }
    }

    public Optional<Usuario> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "usuario"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUsuario(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el usuario.", exception);
        }
    }

    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                usuarios.add(mapUsuario(resultSet));
            }
            return usuarios;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los usuarios.", exception);
        }
    }

    public List<Usuario> buscar(String criterio) {
        List<Usuario> usuarios = new ArrayList<>();
        String pattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    usuarios.add(mapUsuario(resultSet));
                }
            }
            return usuarios;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron buscar los usuarios.", exception);
        }
    }

    public Long insertar(Usuario usuario) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, requireId(usuario.getRol().getId(), "rol"));
            statement.setString(2, normalizeUsername(usuario.getNombreUsuario()));
            statement.setString(3, usuario.getPasswordHash());
            statement.setString(4, usuario.getPasswordAlgoritmo());
            statement.setString(5, usuario.getNombreMostrar());
            setStringOrNull(statement, 6, usuario.getCorreoElectronico());
            statement.setInt(7, usuario.isActivo() ? 1 : 0);
            statement.setInt(8, usuario.isDebeCambiarPassword() ? 1 : 0);
            statement.executeUpdate();
            long id = getGeneratedId(statement);
            usuario.setId(id);
            return id;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar el usuario.", exception);
        }
    }

    public boolean actualizarDatosGenerales(Usuario usuario) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_GENERAL_SQL)) {
            statement.setLong(1, requireId(usuario.getRol().getId(), "rol"));
            statement.setString(2, usuario.getNombreMostrar());
            setStringOrNull(statement, 3, usuario.getCorreoElectronico());
            statement.setLong(4, requireId(usuario.getId(), "usuario"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar el usuario.", exception);
        }
    }

    public boolean actualizarActivo(Long id, boolean activo) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS_SQL)) {
            statement.setInt(1, activo ? 1 : 0);
            statement.setLong(2, requireId(id, "usuario"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar el estado del usuario.", exception);
        }
    }

    public boolean actualizarPassword(Long id, String passwordHash, String algoritmo, boolean debeCambiarPassword) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD_SQL)) {
            statement.setString(1, passwordHash);
            statement.setString(2, algoritmo);
            statement.setInt(3, debeCambiarPassword ? 1 : 0);
            statement.setLong(4, requireId(id, "usuario"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar la contrasena.", exception);
        }
    }

    public boolean actualizarUltimoAcceso(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_LAST_ACCESS_SQL)) {
            statement.setLong(1, requireId(id, "usuario"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar el ultimo acceso.", exception);
        }
    }

    public boolean existeNombreUsuario(String nombreUsuario, Long excludeId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_USERNAME_SQL)) {
            statement.setString(1, normalizeUsername(nombreUsuario));
            if (excludeId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setLong(2, excludeId);
                statement.setLong(3, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo verificar el nombre de usuario.", exception);
        }
    }

    public int contarAdministradoresActivos() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(ACTIVE_ADMIN_COUNT_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo contar administradores activos.", exception);
        }
    }

    static Usuario mapUsuario(ResultSet resultSet) throws SQLException {
        Rol rol = new Rol(
                resultSet.getLong("rol_id"),
                resultSet.getString("rol_nombre"),
                resultSet.getString("rol_descripcion"),
                resultSet.getInt("rol_activo") == 1
        );
        return new Usuario(
                resultSet.getLong("usuario_id"),
                rol,
                resultSet.getString("nombre_usuario"),
                resultSet.getString("password_hash"),
                resultSet.getString("password_algoritmo"),
                resultSet.getString("nombre_mostrar"),
                resultSet.getString("correo_electronico"),
                resultSet.getInt("usuario_activo") == 1,
                resultSet.getInt("debe_cambiar_password") == 1,
                parseDateTime(resultSet.getString("ultimo_acceso")),
                parseDateTime(resultSet.getString("usuario_fecha_creacion")),
                parseDateTime(resultSet.getString("fecha_actualizacion"))
        );
    }

    private static LocalDateTime parseDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value, SQLITE_DATE_TIME);
    }

    private static String normalizeUsername(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
