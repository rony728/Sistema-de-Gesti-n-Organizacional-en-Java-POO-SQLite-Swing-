package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Departamento;
import edu.university.system.model.Empresa;
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
                d.presupuesto AS departamento_presupuesto,
                d.activo AS departamento_activo,
                e.id AS empresa_id,
                e.nombre AS empresa_nombre,
                e.rtn AS empresa_rtn,
                e.telefono AS empresa_telefono,
                e.correo_electronico AS empresa_correo,
                e.direccion AS empresa_direccion,
                p.id AS pais_id,
                p.nombre AS pais_nombre,
                p.codigo_iso AS pais_codigo_iso
            FROM departamento d
            INNER JOIN empresa e ON e.id = d.empresa_id
            INNER JOIN pais p ON p.id = e.pais_id
            """;
    private static final String INSERT_SQL = """
            INSERT INTO departamento (empresa_id, nombre, presupuesto, activo)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE departamento
            SET empresa_id = ?, nombre = ?, presupuesto = ?, activo = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM departamento WHERE id = ?";
    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE d.id = ?";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY e.nombre, d.nombre";
    private static final String FIND_BY_EMPRESA_SQL = BASE_SELECT + " WHERE d.empresa_id = ? ORDER BY d.nombre";
    private static final String FIND_BY_EMPRESA_NOMBRE_SQL = BASE_SELECT + """
             WHERE d.empresa_id = ?
               AND lower(d.nombre) = lower(?)
            """;
    private static final String EXISTS_DUPLICATE_SQL = """
            SELECT COUNT(1)
            FROM departamento
            WHERE empresa_id = ?
              AND lower(nombre) = lower(?)
              AND (? IS NULL OR id <> ?)
            """;
    private static final String SEARCH_SQL = BASE_SELECT + """
             WHERE lower(d.nombre) LIKE lower(?)
                OR lower(e.nombre) LIKE lower(?)
             ORDER BY e.nombre, d.nombre
            """;

    public DepartamentoDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Long insertar(Departamento departamento) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, requireId(departamento.getEmpresa().getId(), "empresa"));
            statement.setString(2, departamento.getNombre());
            setBigDecimalOrZero(statement, 3, departamento.getPresupuesto());
            statement.setInt(4, departamento.isActivo() ? 1 : 0);
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
            statement.setLong(1, requireId(departamento.getEmpresa().getId(), "empresa"));
            statement.setString(2, departamento.getNombre());
            setBigDecimalOrZero(statement, 3, departamento.getPresupuesto());
            statement.setInt(4, departamento.isActivo() ? 1 : 0);
            statement.setLong(5, requireId(departamento.getId(), "departamento"));
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
        return queryList(FIND_ALL_SQL);
    }

    public List<Departamento> listarPorEmpresa(Long empresaId) {
        List<Departamento> departamentos = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMPRESA_SQL)) {
            statement.setLong(1, requireId(empresaId, "empresa"));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    departamentos.add(mapDepartamento(resultSet));
                }
            }
            return departamentos;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los departamentos por empresa.", exception);
        }
    }

    public Optional<Departamento> buscarPorEmpresaYNombre(Long empresaId, String nombre) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMPRESA_NOMBRE_SQL)) {
            statement.setLong(1, requireId(empresaId, "empresa"));
            statement.setString(2, nombre == null ? "" : nombre.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapDepartamento(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el departamento por empresa y nombre.", exception);
        }
    }

    public boolean existeDuplicado(Long empresaId, String nombre, Long excludedId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_DUPLICATE_SQL)) {
            statement.setLong(1, requireId(empresaId, "empresa"));
            statement.setString(2, nombre == null ? "" : nombre.trim());
            setLongOrNull(statement, 3, excludedId);
            setLongOrNull(statement, 4, excludedId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo verificar duplicidad de departamento.", exception);
        }
    }

    public List<Departamento> buscar(String criterio) {
        List<Departamento> departamentos = new ArrayList<>();
        String searchPattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
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

    private List<Departamento> queryList(String sql) {
        List<Departamento> departamentos = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                departamentos.add(mapDepartamento(resultSet));
            }
            return departamentos;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los departamentos.", exception);
        }
    }

    static Departamento mapDepartamento(ResultSet resultSet) throws SQLException {
        Pais pais = new Pais(
                resultSet.getLong("pais_id"),
                resultSet.getString("pais_nombre"),
                resultSet.getString("pais_codigo_iso")
        );
        Empresa empresa = new Empresa(
                resultSet.getLong("empresa_id"),
                resultSet.getString("empresa_nombre"),
                resultSet.getString("empresa_rtn"),
                resultSet.getString("empresa_telefono"),
                resultSet.getString("empresa_correo"),
                resultSet.getString("empresa_direccion"),
                pais
        );
        return new Departamento(
                resultSet.getLong("departamento_id"),
                empresa,
                resultSet.getString("departamento_nombre"),
                resultSet.getBigDecimal("departamento_presupuesto"),
                resultSet.getInt("departamento_activo") == 1
        );
    }
}
