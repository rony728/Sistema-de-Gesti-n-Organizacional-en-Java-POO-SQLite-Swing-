package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;

/**
 * Base comun para DAO JDBC. Entrega conexiones con claves foraneas activadas y
 * helpers de mapeo de valores nulos, fechas, importes e identificadores.
 */
abstract class DaoSupport {

    private final DatabaseConnection databaseConnection;

    protected DaoSupport(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    protected Connection getConnection() throws SQLException {
        Connection connection = databaseConnection.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    protected long getGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.getGeneratedKeys()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            throw new SQLException("No se obtuvo el identificador generado.");
        }
    }

    protected void setLongOrNull(PreparedStatement statement, int parameterIndex, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.INTEGER);
        } else {
            statement.setLong(parameterIndex, value);
        }
    }

    protected void setStringOrNull(PreparedStatement statement, int parameterIndex, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(parameterIndex, Types.VARCHAR);
        } else {
            statement.setString(parameterIndex, value.trim());
        }
    }

    protected void setDateOrNull(PreparedStatement statement, int parameterIndex, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.DATE);
        } else {
            statement.setString(parameterIndex, value.toString());
        }
    }

    protected void setRequiredDate(PreparedStatement statement, int parameterIndex, LocalDate value) throws SQLException {
        if (value == null) {
            throw new DaoException("La fecha requerida no puede ser nula.");
        }
        statement.setString(parameterIndex, value.toString());
    }

    protected void setBigDecimalOrZero(PreparedStatement statement, int parameterIndex, BigDecimal value) throws SQLException {
        statement.setBigDecimal(parameterIndex, value == null ? BigDecimal.ZERO : value);
    }

    protected LocalDate getLocalDate(ResultSet resultSet, String columnLabel) throws SQLException {
        String value = resultSet.getString(columnLabel);
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    protected Long requireId(Long id, String entityName) {
        if (id == null) {
            throw new DaoException("El identificador de " + entityName + " es requerido.");
        }
        return id;
    }

    protected Long getNullableLong(ResultSet resultSet, String columnLabel) throws SQLException {
        long value = resultSet.getLong(columnLabel);
        return resultSet.wasNull() ? null : value;
    }
}
