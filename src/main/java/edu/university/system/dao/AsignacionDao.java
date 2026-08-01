package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Asignacion;
import edu.university.system.model.Empleado;
import edu.university.system.model.Proyecto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AsignacionDao extends DaoSupport implements CrudDao<Asignacion, Long> {

    private static final String INSERT_SQL = """
            INSERT INTO asignacion (empleado_id, proyecto_id, rol, fecha_inicio, fecha_fin, horas_asignadas)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE asignacion
            SET empleado_id = ?, proyecto_id = ?, rol = ?, fecha_inicio = ?, fecha_fin = ?,
                horas_asignadas = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM asignacion WHERE id = ?";
    private static final String FIND_BY_ID_SQL = """
            SELECT id, empleado_id, proyecto_id, rol, fecha_inicio, fecha_fin, horas_asignadas
            FROM asignacion
            WHERE id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, empleado_id, proyecto_id, rol, fecha_inicio, fecha_fin, horas_asignadas
            FROM asignacion
            ORDER BY fecha_inicio DESC, id DESC
            """;
    private static final String SEARCH_SQL = """
            SELECT
                a.id,
                a.empleado_id,
                a.proyecto_id,
                a.rol,
                a.fecha_inicio,
                a.fecha_fin,
                a.horas_asignadas
            FROM asignacion a
            INNER JOIN empleado e ON e.id = a.empleado_id
            INNER JOIN persona p ON p.id = e.id
            INNER JOIN proyecto pr ON pr.id = a.proyecto_id
            WHERE lower(a.rol) LIKE lower(?)
               OR lower(e.codigo_empleado) LIKE lower(?)
               OR lower(p.nombres) LIKE lower(?)
               OR lower(p.apellidos) LIKE lower(?)
               OR lower(pr.codigo) LIKE lower(?)
               OR lower(pr.nombre) LIKE lower(?)
            ORDER BY a.fecha_inicio DESC, a.id DESC
            """;

    private final EmpleadoDao empleadoDao;
    private final ProyectoDao proyectoDao;

    public AsignacionDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
        this.empleadoDao = new EmpleadoDao(databaseConnection);
        this.proyectoDao = new ProyectoDao(databaseConnection);
    }

    @Override
    public Long insertar(Asignacion asignacion) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, requireId(asignacion.getEmpleado().getId(), "empleado"));
            statement.setLong(2, requireId(asignacion.getProyecto().getId(), "proyecto"));
            statement.setString(3, asignacion.getRol());
            setRequiredDate(statement, 4, asignacion.getFechaInicio());
            setDateOrNull(statement, 5, asignacion.getFechaFin());
            statement.setInt(6, asignacion.getHorasAsignadas());
            statement.executeUpdate();
            long id = getGeneratedId(statement);
            asignacion.setId(id);
            return id;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar la asignacion.", exception);
        }
    }

    @Override
    public boolean actualizar(Asignacion asignacion) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setLong(1, requireId(asignacion.getEmpleado().getId(), "empleado"));
            statement.setLong(2, requireId(asignacion.getProyecto().getId(), "proyecto"));
            statement.setString(3, asignacion.getRol());
            setRequiredDate(statement, 4, asignacion.getFechaInicio());
            setDateOrNull(statement, 5, asignacion.getFechaFin());
            statement.setInt(6, asignacion.getHorasAsignadas());
            statement.setLong(7, requireId(asignacion.getId(), "asignacion"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar la asignacion.", exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, requireId(id, "asignacion"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo eliminar la asignacion.", exception);
        }
    }

    @Override
    public Optional<Asignacion> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "asignacion"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapAsignacion(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar la asignacion.", exception);
        }
    }

    @Override
    public List<Asignacion> listar() {
        List<Asignacion> asignaciones = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                asignaciones.add(mapAsignacion(resultSet));
            }
            return asignaciones;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar las asignaciones.", exception);
        }
    }

    public List<Asignacion> buscar(String criterio) {
        List<Asignacion> asignaciones = new ArrayList<>();
        String searchPattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            for (int index = 1; index <= 6; index++) {
                statement.setString(index, searchPattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    asignaciones.add(mapAsignacion(resultSet));
                }
            }
            return asignaciones;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron buscar las asignaciones.", exception);
        }
    }

    private Asignacion mapAsignacion(ResultSet resultSet) throws SQLException {
        Long empleadoId = resultSet.getLong("empleado_id");
        Long proyectoId = resultSet.getLong("proyecto_id");
        Empleado empleado = empleadoDao.buscarPorId(empleadoId)
                .orElseThrow(() -> new DaoException("No se encontro el empleado de la asignacion."));
        Proyecto proyecto = proyectoDao.buscarPorId(proyectoId)
                .orElseThrow(() -> new DaoException("No se encontro el proyecto de la asignacion."));

        return new Asignacion(
                resultSet.getLong("id"),
                empleado,
                proyecto,
                resultSet.getString("rol"),
                LocalDate.parse(resultSet.getString("fecha_inicio")),
                resultSet.getString("fecha_fin") == null ? null : LocalDate.parse(resultSet.getString("fecha_fin")),
                resultSet.getInt("horas_asignadas")
        );
    }
}
