package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO de roles. Provee consultas de lectura para autenticacion, autorizacion y
 * administracion de usuarios sin exponer SQL a controladores o vistas.
 */
public class RolDao extends DaoSupport {

    private static final String FIND_BY_ID_SQL = """
            SELECT id, nombre, descripcion, activo
            FROM rol
            WHERE id = ?
            """;
    private static final String FIND_BY_NAME_SQL = """
            SELECT id, nombre, descripcion, activo
            FROM rol
            WHERE lower(nombre) = lower(?)
            """;
    private static final String FIND_ACTIVE_SQL = """
            SELECT id, nombre, descripcion, activo
            FROM rol
            WHERE activo = 1
            ORDER BY nombre
            """;

    public RolDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    public Optional<Rol> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "rol"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRol(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el rol.", exception);
        }
    }

    public Optional<Rol> buscarPorNombre(String nombre) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_NAME_SQL)) {
            statement.setString(1, nombre == null ? "" : nombre.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRol(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el rol.", exception);
        }
    }

    public List<Rol> listarActivos() {
        List<Rol> roles = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ACTIVE_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                roles.add(mapRol(resultSet));
            }
            return roles;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los roles.", exception);
        }
    }

    static Rol mapRol(ResultSet resultSet) throws SQLException {
        return new Rol(
                resultSet.getLong("id"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion"),
                resultSet.getInt("activo") == 1
        );
    }
}
