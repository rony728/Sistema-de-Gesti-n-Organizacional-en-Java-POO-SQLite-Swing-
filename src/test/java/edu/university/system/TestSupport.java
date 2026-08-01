package edu.university.system;

import edu.university.system.config.AppConfig;
import edu.university.system.config.DatabaseConnection;
import edu.university.system.dao.DaoFactory;
import edu.university.system.model.Rol;
import edu.university.system.model.UserSession;
import edu.university.system.model.Usuario;

import java.nio.file.Path;
import java.util.Properties;

final class TestSupport {

    private TestSupport() {
    }

    static DatabaseConnection database(Path databasePath) {
        Properties properties = new Properties();
        properties.setProperty("app.name", "Sistema Universitario Test");
        properties.setProperty("app.version", "1.0.0-test");
        properties.setProperty("app.company", "Pruebas");
        properties.setProperty("database.path", databasePath.toString());
        DatabaseConnection databaseConnection = new DatabaseConnection(AppConfig.fromProperties(properties));
        databaseConnection.initialize();
        return databaseConnection;
    }

    static DaoFactory daoFactory(Path databasePath) {
        return new DaoFactory(database(databasePath));
    }

    static void startAdminSession() {
        UserSession.start(new Usuario(
                1L,
                new Rol(1L, "Administrador", "Acceso completo", true),
                "admin",
                "hash",
                "SHA-256",
                "Administrador",
                null,
                true,
                false,
                null,
                null,
                null
        ));
    }

    static void startConsultaSession() {
        UserSession.start(new Usuario(
                2L,
                new Rol(2L, "Consulta", "Solo consulta", true),
                "consulta",
                "hash",
                "SHA-256",
                "Consulta",
                null,
                true,
                false,
                null,
                null,
                null
        ));
    }

    static char[] adminPassword() {
        return new char[]{'A', 'd', 'm', 'i', 'n', '1', '2', '3', '*'};
    }

    static char[] consultaPassword() {
        return new char[]{'C', 'o', 'n', 's', 'u', 'l', 't', 'a', '1', '2', '3', '*'};
    }

    static char[] invalidPassword() {
        return new char[]{'E', 'r', 'r', 'o', 'r', '1', '2', '3', '*'};
    }
}
