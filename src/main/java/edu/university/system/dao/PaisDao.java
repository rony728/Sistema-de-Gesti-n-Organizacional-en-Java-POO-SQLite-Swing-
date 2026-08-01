package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Pais;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaisDao extends DaoSupport implements CrudDao<Pais, Long> {

    private static final String INSERT_SQL = """
            INSERT INTO pais (nombre, codigo_iso)
            VALUES (?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE pais
            SET nombre = ?, codigo_iso = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM pais WHERE id = ?";
    private static final String FIND_BY_ID_SQL = """
            SELECT id, nombre, codigo_iso
            FROM pais
            WHERE id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, nombre, codigo_iso
            FROM pais
            ORDER BY nombre
            """;
    private static final String SEARCH_SQL = """
            SELECT id, nombre, codigo_iso
            FROM pais
            WHERE lower(nombre) LIKE lower(?)
               OR lower(codigo_iso) LIKE lower(?)
            ORDER BY nombre
            """;

    public PaisDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Long insertar(Pais pais) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pais.getNombre());
            statement.setString(2, pais.getCodigoIso());
            statement.executeUpdate();
            long id = getGeneratedId(statement);
            pais.setId(id);
            return id;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar el pais.", exception);
        }
    }

    @Override
    public boolean actualizar(Pais pais) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, pais.getNombre());
            statement.setString(2, pais.getCodigoIso());
            statement.setLong(3, requireId(pais.getId(), "pais"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar el pais.", exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, requireId(id, "pais"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo eliminar el pais.", exception);
        }
    }

    @Override
    public Optional<Pais> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "pais"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapPais(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el pais.", exception);
        }
    }

    @Override
    public List<Pais> listar() {
        List<Pais> paises = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                paises.add(mapPais(resultSet));
            }
            return paises;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los paises.", exception);
        }
    }

    public List<Pais> buscar(String criterio) {
        List<Pais> paises = new ArrayList<>();
        String searchPattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    paises.add(mapPais(resultSet));
                }
            }
            return paises;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron buscar los paises.", exception);
        }
    }

    static Pais mapPais(ResultSet resultSet) throws SQLException {
        return new Pais(
                resultSet.getLong("id"),
                resultSet.getString("nombre"),
                resultSet.getString("codigo_iso")
        );
    }
}
