package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Cargo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CargoDao extends DaoSupport implements CrudDao<Cargo, Long> {

    private static final String INSERT_SQL = """
            INSERT INTO cargo (nombre, descripcion)
            VALUES (?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE cargo
            SET nombre = ?, descripcion = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM cargo WHERE id = ?";
    private static final String FIND_BY_ID_SQL = """
            SELECT id, nombre, descripcion
            FROM cargo
            WHERE id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, nombre, descripcion
            FROM cargo
            ORDER BY nombre
            """;
    private static final String SEARCH_SQL = """
            SELECT id, nombre, descripcion
            FROM cargo
            WHERE lower(nombre) LIKE lower(?)
               OR lower(coalesce(descripcion, '')) LIKE lower(?)
            ORDER BY nombre
            """;

    public CargoDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Long insertar(Cargo cargo) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cargo.getNombre());
            setStringOrNull(statement, 2, cargo.getDescripcion());
            statement.executeUpdate();
            long id = getGeneratedId(statement);
            cargo.setId(id);
            return id;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar el cargo.", exception);
        }
    }

    @Override
    public boolean actualizar(Cargo cargo) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, cargo.getNombre());
            setStringOrNull(statement, 2, cargo.getDescripcion());
            statement.setLong(3, requireId(cargo.getId(), "cargo"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar el cargo.", exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, requireId(id, "cargo"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo eliminar el cargo.", exception);
        }
    }

    @Override
    public Optional<Cargo> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "cargo"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapCargo(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el cargo.", exception);
        }
    }

    @Override
    public List<Cargo> listar() {
        List<Cargo> cargos = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                cargos.add(mapCargo(resultSet));
            }
            return cargos;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los cargos.", exception);
        }
    }

    public List<Cargo> buscar(String criterio) {
        List<Cargo> cargos = new ArrayList<>();
        String searchPattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cargos.add(mapCargo(resultSet));
                }
            }
            return cargos;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron buscar los cargos.", exception);
        }
    }

    static Cargo mapCargo(ResultSet resultSet) throws SQLException {
        return new Cargo(
                resultSet.getLong("id"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion")
        );
    }
}
