package edu.university.system.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * Lee y expone las propiedades de configuracion de la aplicacion. Centraliza
 * valores como nombre, version y ubicacion de la base SQLite para evitar
 * literales repartidos por el codigo.
 */
public final class AppConfig {

    private static final String CONFIG_FILE = "application.properties";

    private final Properties properties;

    private AppConfig(Properties properties) {
        this.properties = properties;
    }

    /**
     * Crea una configuracion desde propiedades ya cargadas. Se usa principalmente
     * para pruebas automatizadas con bases SQLite temporales sin tocar la base real
     * definida en application.properties.
     *
     * @param properties propiedades de aplicacion requeridas por el sistema
     * @return configuracion inmutable para los consumidores de la aplicacion
     */
    public static AppConfig fromProperties(Properties properties) {
        Properties copy = new Properties();
        copy.putAll(properties);
        return new AppConfig(copy);
    }

    /**
     * Carga la configuracion principal desde application.properties incluido en el classpath.
     *
     * @return configuracion de la aplicacion
     * @throws IllegalStateException si el recurso no existe o no puede leerse
     */
    public static AppConfig load() {
        Properties properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException("No se encontro el archivo de configuracion: " + CONFIG_FILE);
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            return new AppConfig(properties);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo cargar la configuracion inicial.", exception);
        }
    }

    public String getApplicationName() {
        return getRequiredProperty("app.name");
    }

    public String getApplicationVersion() {
        return getRequiredProperty("app.version");
    }

    public String getCompanyName() {
        return getRequiredProperty("app.company");
    }

    public Path getDatabasePath() {
        return Path.of(getRequiredProperty("database.path"));
    }

    private String getRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta la propiedad requerida: " + key);
        }
        return value.trim();
    }

    public String getProperty(String key, String defaultValue) {
        return Objects.requireNonNullElse(properties.getProperty(key), defaultValue);
    }
}
