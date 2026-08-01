package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Empresa;
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

public class ProyectoDao extends DaoSupport implements CrudDao<Proyecto, Long> {

    private static final String BASE_SELECT = """
            SELECT
                pr.id AS proyecto_id,
                pr.codigo AS proyecto_codigo,
                pr.nombre AS proyecto_nombre,
                pr.descripcion AS proyecto_descripcion,
                pr.fecha_inicio AS proyecto_fecha_inicio,
                pr.fecha_fin AS proyecto_fecha_fin,
                pr.presupuesto AS proyecto_presupuesto,
                e.id AS empresa_id,
                e.nombre AS empresa_nombre,
                e.rtn AS empresa_rtn,
                e.telefono AS empresa_telefono,
                e.correo_electronico AS empresa_correo,
                e.direccion AS empresa_direccion,
                p.id AS pais_id,
                p.nombre AS pais_nombre,
                p.codigo_iso AS pais_codigo_iso
            FROM proyecto pr
            INNER JOIN empresa e ON e.id = pr.empresa_id
            INNER JOIN pais p ON p.id = e.pais_id
            """;
    private static final String INSERT_SQL = """
            INSERT INTO proyecto (empresa_id, codigo, nombre, descripcion, fecha_inicio, fecha_fin, presupuesto)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE proyecto
            SET empresa_id = ?, codigo = ?, nombre = ?, descripcion = ?, fecha_inicio = ?,
                fecha_fin = ?, presupuesto = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM proyecto WHERE id = ?";
    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE pr.id = ?";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY pr.nombre";
    private static final String SEARCH_SQL = BASE_SELECT + """
             WHERE lower(pr.codigo) LIKE lower(?)
                OR lower(pr.nombre) LIKE lower(?)
                OR lower(coalesce(pr.descripcion, '')) LIKE lower(?)
                OR lower(e.nombre) LIKE lower(?)
             ORDER BY pr.nombre
            """;

    public ProyectoDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Long insertar(Proyecto proyecto) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, requireId(proyecto.getEmpresa().getId(), "empresa"));
            statement.setString(2, proyecto.getCodigo());
            statement.setString(3, proyecto.getNombre());
            setStringOrNull(statement, 4, proyecto.getDescripcion());
            setRequiredDate(statement, 5, proyecto.getFechaInicio());
            setDateOrNull(statement, 6, proyecto.getFechaFin());
            setBigDecimalOrZero(statement, 7, proyecto.getPresupuesto());
            statement.executeUpdate();
            long id = getGeneratedId(statement);
            proyecto.setId(id);
            return id;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar el proyecto.", exception);
        }
    }

    @Override
    public boolean actualizar(Proyecto proyecto) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setLong(1, requireId(proyecto.getEmpresa().getId(), "empresa"));
            statement.setString(2, proyecto.getCodigo());
            statement.setString(3, proyecto.getNombre());
            setStringOrNull(statement, 4, proyecto.getDescripcion());
            setRequiredDate(statement, 5, proyecto.getFechaInicio());
            setDateOrNull(statement, 6, proyecto.getFechaFin());
            setBigDecimalOrZero(statement, 7, proyecto.getPresupuesto());
            statement.setLong(8, requireId(proyecto.getId(), "proyecto"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar el proyecto.", exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, requireId(id, "proyecto"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo eliminar el proyecto.", exception);
        }
    }

    @Override
    public Optional<Proyecto> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "proyecto"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapProyecto(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar el proyecto.", exception);
        }
    }

    @Override
    public List<Proyecto> listar() {
        List<Proyecto> proyectos = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                proyectos.add(mapProyecto(resultSet));
            }
            return proyectos;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar los proyectos.", exception);
        }
    }

    public List<Proyecto> buscar(String criterio) {
        List<Proyecto> proyectos = new ArrayList<>();
        String searchPattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    proyectos.add(mapProyecto(resultSet));
                }
            }
            return proyectos;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron buscar los proyectos.", exception);
        }
    }

    static Proyecto mapProyecto(ResultSet resultSet) throws SQLException {
        Empresa empresa = EmpresaDao.mapEmpresa(resultSet);
        return new Proyecto(
                resultSet.getLong("proyecto_id"),
                resultSet.getString("proyecto_codigo"),
                resultSet.getString("proyecto_nombre"),
                resultSet.getString("proyecto_descripcion"),
                LocalDate.parse(resultSet.getString("proyecto_fecha_inicio")),
                resultSet.getString("proyecto_fecha_fin") == null ? null : LocalDate.parse(resultSet.getString("proyecto_fecha_fin")),
                resultSet.getBigDecimal("proyecto_presupuesto"),
                empresa
        );
    }
}
