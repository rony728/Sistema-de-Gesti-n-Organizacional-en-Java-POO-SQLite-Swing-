package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;
import edu.university.system.model.Persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonaDao extends DaoSupport implements CrudDao<Persona, Long> {

    static final String INSERT_SQL = """
            INSERT INTO persona (identidad, nombres, apellidos, telefono, correo_electronico, direccion, fecha_nacimiento)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    static final String UPDATE_SQL = """
            UPDATE persona
            SET identidad = ?, nombres = ?, apellidos = ?, telefono = ?, correo_electronico = ?,
                direccion = ?, fecha_nacimiento = ?, fecha_actualizacion = datetime('now')
            WHERE id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM persona WHERE id = ?";
    private static final String FIND_BY_ID_SQL = """
            SELECT id, identidad, nombres, apellidos, telefono, correo_electronico, direccion, fecha_nacimiento
            FROM persona
            WHERE id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, identidad, nombres, apellidos, telefono, correo_electronico, direccion, fecha_nacimiento
            FROM persona
            ORDER BY apellidos, nombres
            """;

    public PersonaDao(DatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Long insertar(Persona persona) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            fillInsertStatement(statement, persona);
            statement.executeUpdate();
            long id = getGeneratedId(statement);
            persona.setId(id);
            return id;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo insertar la persona.", exception);
        }
    }

    @Override
    public boolean actualizar(Persona persona) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            fillUpdateStatement(statement, persona);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo actualizar la persona.", exception);
        }
    }

    @Override
    public boolean eliminar(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, requireId(id, "persona"));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DaoException("No se pudo eliminar la persona.", exception);
        }
    }

    @Override
    public Optional<Persona> buscarPorId(Long id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, requireId(id, "persona"));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapPersona(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("No se pudo buscar la persona.", exception);
        }
    }

    @Override
    public List<Persona> listar() {
        List<Persona> personas = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                personas.add(mapPersona(resultSet));
            }
            return personas;
        } catch (SQLException exception) {
            throw new DaoException("No se pudieron listar las personas.", exception);
        }
    }

    void fillInsertStatement(PreparedStatement statement, Persona persona) throws SQLException {
        statement.setString(1, persona.getIdentidad());
        statement.setString(2, persona.getNombres());
        statement.setString(3, persona.getApellidos());
        setStringOrNull(statement, 4, persona.getTelefono());
        setStringOrNull(statement, 5, persona.getCorreoElectronico());
        setStringOrNull(statement, 6, persona.getDireccion());
        setDateOrNull(statement, 7, persona.getFechaNacimiento());
    }

    void fillUpdateStatement(PreparedStatement statement, Persona persona) throws SQLException {
        fillInsertStatement(statement, persona);
        statement.setLong(8, requireId(persona.getId(), "persona"));
    }

    static Persona mapPersona(ResultSet resultSet) throws SQLException {
        PersonaRegistro persona = new PersonaRegistro();
        persona.setId(resultSet.getLong("id"));
        persona.setIdentidad(resultSet.getString("identidad"));
        persona.setNombres(resultSet.getString("nombres"));
        persona.setApellidos(resultSet.getString("apellidos"));
        persona.setTelefono(resultSet.getString("telefono"));
        persona.setCorreoElectronico(resultSet.getString("correo_electronico"));
        persona.setDireccion(resultSet.getString("direccion"));
        String fechaNacimiento = resultSet.getString("fecha_nacimiento");
        persona.setFechaNacimiento(fechaNacimiento == null ? null : LocalDate.parse(fechaNacimiento));
        return persona;
    }

    private static final class PersonaRegistro extends Persona {
        private PersonaRegistro() {
            super();
        }
    }
}
