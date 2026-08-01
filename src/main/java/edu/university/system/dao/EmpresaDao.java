package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
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

public class EmpresaDao extends DaoSupport implements CrudDao<Empresa, Long> {

    private static final String BASE_SELECT = """
            SELECT
                e.id AS empresa_id,
                e.nombre AS empresa_nombre,
                e.rtn AS empresa_rtn,
                e.telefono AS empresa_telefono,
                e.correo_electronico AS empresa_correo,
                e.direccion AS empresa_direccion,
                p.id AS pais_id,
                p.nombre AS pais_nombre,
                p.codigo_iso AS pais_codigo_iso
            FROM empresa e
            INNER JOIN pais p ON p.id = e.pais_id
            """;
    private static final String INSERT_SQL = """
            INSERT INTO empresa (pais_id, nombre, rtn, telefono, correo_electronico, direccion)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE empresa
            SET pais_id = ?, nombre = ?, rtn = ?, telefono = ?,
                correo_electronico = ?, direccion = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM empresa WHERE id = ?";
    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE e.id = ?";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY e.nombre";
    private static final String SEARCH_SQL = BASE_SELECT + """
             WHERE lower(e.nombre) LIKE lower(?)
                OR lower(e.rtn) LIKE lower(?)
                OR lower(coalesce(e.telefono, '')) LIKE lower(?)
                OR lower(coalesce(e.correo_electronico, '')) LIKE lower(?)
                OR lower(p.nombre) LIKE lower(?)
                OR lower(coalesce(e.direccion, '')) LIKE lower(?)
             ORDER BY e.nombre
            """;

    public EmpresaDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Long insertar(Empresa empresa) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, requireId(empresa.getPais().getId(), "pais"));
            statement.setString(2, empresa.getNombre());
            statement.setString(3, empresa.getRtn());
            setStringOrNull(statement, 4, empresa.getTelefono());
            setStringOrNull(statement, 5, empresa.getCorreoElectronico());
            setStringOrNull(statement, 6, empresa.getDireccion());
            statement.executeUpdate();
            long id = getGeneratedId(statement);
            empresa.setId(id);
            return id;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar la empresa.", exception);
        }
    }

    @Override
    public boolean actualizar(Empresa empresa) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setLong(1, requireId(empresa.getPais().getId(), "pais"));
            statement.setString(2, empresa.getNombre());
            statement.setString(3, empresa.getRtn());
            setStringOrNull(statement, 4, empresa.getTelefono());
            setStringOrNull(statement, 5, empresa.getCorreoElectronico());
            setStringOrNull(statement, 6, empresa.getDireccion());
            statement.setLong(7, requireId(empresa.getId(), "empresa"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar la empresa.", exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, requireId(id, "empresa"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo eliminar la empresa.", exception);
        }
    }

    @Override
    public Optional<Empresa> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "empresa"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapEmpresa(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar la empresa.", exception);
        }
    }

    @Override
    public List<Empresa> listar() {
        List<Empresa> empresas = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                empresas.add(mapEmpresa(resultSet));
            }
            return empresas;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar las empresas.", exception);
        }
    }

    public List<Empresa> buscar(String criterio) {
        List<Empresa> empresas = new ArrayList<>();
        String pattern = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            for (int index = 1; index <= 6; index++) {
                statement.setString(index, pattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    empresas.add(mapEmpresa(resultSet));
                }
            }
            return empresas;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron buscar las empresas.", exception);
        }
    }

    static Empresa mapEmpresa(ResultSet resultSet) throws SQLException {
        Pais pais = new Pais(
                resultSet.getLong("pais_id"),
                resultSet.getString("pais_nombre"),
                resultSet.getString("pais_codigo_iso")
        );
        return new Empresa(
                resultSet.getLong("empresa_id"),
                resultSet.getString("empresa_nombre"),
                resultSet.getString("empresa_rtn"),
                resultSet.getString("empresa_telefono"),
                resultSet.getString("empresa_correo"),
                resultSet.getString("empresa_direccion"),
                pais
        );
    }
}
