package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Departamento;
import edu.university.system.model.Pais;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartamentoDao extends DaoSupport implements CrudDao<Departamento, Long> {

    private static final String BASE_SELECT = """
            SELECT
                d.id AS departamento_id,
                d.nombre AS departamento_nombre,
                d.codigo AS departamento_codigo,
                p.id AS pais_id,
                p.nombre AS pais_nombre,
                p.codigo_iso AS pais_codigo_iso
            FROM departamento d
            INNER JOIN pais p ON p.id = d.pais_id
            """;
    private static final String INSERT_SQL = """
            INSERT INTO departamento (pais_id, nombre, codigo)
            VALUES (?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE departamento
            SET pais_id = ?, nombre = ?, codigo = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM departamento WHERE id = ?";
    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE d.id = ?";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY p.nombre, d.nombre";
    private static final String SEARCH_SQL = BASE_SELECT + """
             WHERE lower(d.nombre) LIKE lower(?)
                OR lower(d.codigo) LIKE lower(?)
                OR lower(p.nombre) LIKE lower(?)
             ORDER BY p.nombre, d.nombre
            """;

    public DepartamentoDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Long insertar(Departamento departamento) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, requireId(departamento.getPais().getId(), "pais"));
            statement.setString(2, departamento.getNombre());
            statement.setString(3, departamento.getCodigo());
            statement.executeUpdate();
            long id = getGeneratedId(statement);
            departamento.setId(id);
            return id;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar el departamento.", exception);
        }
    }

    @Override
    public boolean actualizar(Departamento departamento) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setLong(1, requireId(departamento.getPais().getId(), "pais"));
            statement.setString(2, departamento.getNombre());
            statement.setString(3, departamento.getCodigo());
            statement.setLong(4, requireId(departamento.getId(), "departamento"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar el departamento.", exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, requireId(id, "departamento"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo eliminar el departamento.", exception);
        }
    }

    @Override
    public Optional<Departamento> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "departamento"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapDepartamento(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el departamento.", exception);
        }
    }

    @Override
    public List<Departamento> listar() {
        List<Departamento> departamentos = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                departamentos.add(mapDepartamento(resultSet));
            }
            return departamentos;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los departamentos.", exception);
        }
    }

    public List<Departamento> buscar(String criterio) {
        List<Departamento> departamentos = new ArrayList<>();
        String searchPattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    departamentos.add(mapDepartamento(resultSet));
                }
            }
            return departamentos;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron buscar los departamentos.", exception);
        }
    }

    static Departamento mapDepartamento(ResultSet resultSet) throws SQLException {
        Pais pais = new Pais(
                resultSet.getLong("pais_id"),
                resultSet.getString("pais_nombre"),
                resultSet.getString("pais_codigo_iso")
        );
        return new Departamento(
                resultSet.getLong("departamento_id"),
                resultSet.getString("departamento_nombre"),
                resultSet.getString("departamento_codigo"),
                pais
        );
    }
}
