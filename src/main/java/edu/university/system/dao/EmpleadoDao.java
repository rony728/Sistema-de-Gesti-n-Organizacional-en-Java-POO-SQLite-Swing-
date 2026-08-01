package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Cargo;
import edu.university.system.model.Departamento;
import edu.university.system.model.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO de empleados. Las operaciones de alta y actualizacion son transacciones
 * compuestas sobre persona y empleado; ante cualquier error se ejecuta rollback
 * para conservar la integridad.
 */
public class EmpleadoDao extends DaoSupport implements CrudDao<Empleado, Long> {

    private static final String BASE_SELECT = """
            SELECT
                p.id AS persona_id,
                p.identidad,
                p.nombres,
                p.apellidos,
                p.telefono,
                p.correo_electronico,
                p.direccion,
                p.fecha_nacimiento,
                e.codigo_empleado,
                e.fecha_contratacion,
                e.salario,
                e.foto_ruta,
                c.id AS cargo_id,
                c.nombre AS cargo_nombre,
                c.descripcion AS cargo_descripcion,
                d.id AS departamento_id,
                d.nombre AS departamento_nombre,
                d.presupuesto AS departamento_presupuesto,
                d.activo AS departamento_activo,
                emp.id AS empresa_id,
                emp.nombre AS empresa_nombre,
                emp.rtn AS empresa_rtn,
                emp.telefono AS empresa_telefono,
                emp.correo_electronico AS empresa_correo,
                emp.direccion AS empresa_direccion,
                pa.id AS pais_id,
                pa.nombre AS pais_nombre,
                pa.codigo_iso AS pais_codigo_iso
            FROM empleado e
            INNER JOIN persona p ON p.id = e.id
            INNER JOIN cargo c ON c.id = e.cargo_id
            INNER JOIN departamento d ON d.id = e.departamento_id
            INNER JOIN empresa emp ON emp.id = d.empresa_id
            INNER JOIN pais pa ON pa.id = emp.pais_id
            """;
    private static final String INSERT_EMPLEADO_SQL = """
            INSERT INTO empleado (id, cargo_id, departamento_id, codigo_empleado, fecha_contratacion, salario, foto_ruta)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_EMPLEADO_SQL = """
            UPDATE empleado
            SET cargo_id = ?, departamento_id = ?, codigo_empleado = ?, fecha_contratacion = ?,
                salario = ?, foto_ruta = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM persona WHERE id = ?";
    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE e.id = ?";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY p.apellidos, p.nombres";
    private static final String SEARCH_SQL = BASE_SELECT + """
             WHERE lower(e.codigo_empleado) LIKE lower(?)
                OR lower(p.identidad) LIKE lower(?)
                OR lower(p.nombres) LIKE lower(?)
                OR lower(p.apellidos) LIKE lower(?)
                OR lower(c.nombre) LIKE lower(?)
                OR lower(d.nombre) LIKE lower(?)
             ORDER BY p.apellidos, p.nombres
            """;

    private final PersonaDao personaDao;

    public EmpleadoDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
        this.personaDao = new PersonaDao(databaseConnection);
    }

    @Override
    public Long insertar(Empleado empleado) {
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement personaStatement = connection.prepareStatement(PersonaDao.INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement empleadoStatement = connection.prepareStatement(INSERT_EMPLEADO_SQL)) {
                personaDao.fillInsertStatement(personaStatement, empleado);
                personaStatement.executeUpdate();
                long personaId = getGeneratedId(personaStatement);

                empleadoStatement.setLong(1, personaId);
                empleadoStatement.setLong(2, requireId(empleado.getCargo().getId(), "cargo"));
                empleadoStatement.setLong(3, requireId(empleado.getDepartamento().getId(), "departamento"));
                empleadoStatement.setString(4, empleado.getCodigoEmpleado());
                setRequiredDate(empleadoStatement, 5, empleado.getFechaContratacion());
                setBigDecimalOrZero(empleadoStatement, 6, empleado.getSalario());
                setStringOrNull(empleadoStatement, 7, empleado.getRutaFotografia());
                empleadoStatement.executeUpdate();

                connection.commit();
                empleado.setId(personaId);
                return personaId;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar el empleado.", exception);
        }
    }

    @Override
    public boolean actualizar(Empleado empleado) {
        try (Connection connection = getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement personaStatement = connection.prepareStatement(PersonaDao.UPDATE_SQL);
                 PreparedStatement empleadoStatement = connection.prepareStatement(UPDATE_EMPLEADO_SQL)) {
                personaDao.fillUpdateStatement(personaStatement, empleado);
                int personaRows = personaStatement.executeUpdate();

                empleadoStatement.setLong(1, requireId(empleado.getCargo().getId(), "cargo"));
                empleadoStatement.setLong(2, requireId(empleado.getDepartamento().getId(), "departamento"));
                empleadoStatement.setString(3, empleado.getCodigoEmpleado());
                setRequiredDate(empleadoStatement, 4, empleado.getFechaContratacion());
                setBigDecimalOrZero(empleadoStatement, 5, empleado.getSalario());
                setStringOrNull(empleadoStatement, 6, empleado.getRutaFotografia());
                empleadoStatement.setLong(7, requireId(empleado.getId(), "empleado"));
                int empleadoRows = empleadoStatement.executeUpdate();

                connection.commit();
                return personaRows > 0 && empleadoRows > 0;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar el empleado.", exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, requireId(id, "empleado"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo eliminar el empleado.", exception);
        }
    }

    @Override
    public Optional<Empleado> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "empleado"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapEmpleado(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el empleado.", exception);
        }
    }

    @Override
    public List<Empleado> listar() {
        List<Empleado> empleados = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                empleados.add(mapEmpleado(resultSet));
            }
            return empleados;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los empleados.", exception);
        }
    }

    public List<Empleado> buscar(String criterio) {
        List<Empleado> empleados = new ArrayList<>();
        String searchPattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            for (int index = 1; index <= 6; index++) {
                statement.setString(index, searchPattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    empleados.add(mapEmpleado(resultSet));
                }
            }
            return empleados;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron buscar los empleados.", exception);
        }
    }

    static Empleado mapEmpleado(ResultSet resultSet) throws SQLException {
        Cargo cargo = new Cargo(
                resultSet.getLong("cargo_id"),
                resultSet.getString("cargo_nombre"),
                resultSet.getString("cargo_descripcion")
        );
        Departamento departamento = DepartamentoDao.mapDepartamento(resultSet);

        return new Empleado(
                resultSet.getLong("persona_id"),
                resultSet.getString("identidad"),
                resultSet.getString("nombres"),
                resultSet.getString("apellidos"),
                resultSet.getString("telefono"),
                resultSet.getString("correo_electronico"),
                resultSet.getString("direccion"),
                resultSet.getString("fecha_nacimiento") == null ? null : java.time.LocalDate.parse(resultSet.getString("fecha_nacimiento")),
                resultSet.getString("codigo_empleado"),
                java.time.LocalDate.parse(resultSet.getString("fecha_contratacion")),
                resultSet.getBigDecimal("salario"),
                cargo,
                departamento,
                resultSet.getString("foto_ruta")
        );
    }
}
