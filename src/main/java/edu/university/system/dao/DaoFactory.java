package edu.university.system.dao;

import edu.university.system.config.DatabaseConnection;

/**
 * Fabrica central de DAO. Mantiene la construccion de dependencias de acceso a
 * datos en un solo punto para que controladores y vistas no creen conexiones
 * ni DAOs manualmente.
 */
public final class DaoFactory {

    private final DatabaseConnection databaseConnection;

    public DaoFactory(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public PaisDao paisDao() {
        return new PaisDao(databaseConnection);
    }

    public DepartamentoDao departamentoDao() {
        return new DepartamentoDao(databaseConnection);
    }

    public CargoDao cargoDao() {
        return new CargoDao(databaseConnection);
    }

    public PersonaDao personaDao() {
        return new PersonaDao(databaseConnection);
    }

    public EmpleadoDao empleadoDao() {
        return new EmpleadoDao(databaseConnection);
    }

    public EmpresaDao empresaDao() {
        return new EmpresaDao(databaseConnection);
    }

    public ProyectoDao proyectoDao() {
        return new ProyectoDao(databaseConnection);
    }

    public AsignacionDao asignacionDao() {
        return new AsignacionDao(databaseConnection);
    }

    public RolDao rolDao() {
        return new RolDao(databaseConnection);
    }

    public UsuarioDao usuarioDao() {
        return new UsuarioDao(databaseConnection);
    }
}
