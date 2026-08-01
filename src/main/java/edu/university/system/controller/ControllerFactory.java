package edu.university.system.controller;

import edu.university.system.dao.DaoFactory;

/**
 * Fabrica central de controladores MVC. Ensambla controladores con sus DAO y
 * conserva la separacion entre la capa visual y el acceso directo a datos.
 */
public final class ControllerFactory {

    private final DaoFactory daoFactory;

    public ControllerFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public PaisController paisController() {
        return new PaisController(daoFactory.paisDao());
    }

    public DepartamentoController departamentoController() {
        return new DepartamentoController(daoFactory.departamentoDao());
    }

    public CargoController cargoController() {
        return new CargoController(daoFactory.cargoDao());
    }

    public PersonaController personaController() {
        return new PersonaController(daoFactory.personaDao());
    }

    public EmpleadoController empleadoController() {
        return new EmpleadoController(daoFactory.empleadoDao(), personaController());
    }

    public EmpresaController empresaController() {
        return new EmpresaController(daoFactory.empresaDao());
    }

    public ProyectoController proyectoController() {
        return new ProyectoController(daoFactory.proyectoDao());
    }

    public AsignacionController asignacionController() {
        return new AsignacionController(daoFactory.asignacionDao());
    }

    public LoginController loginController() {
        return new LoginController(daoFactory.usuarioDao());
    }

    public UsuarioController usuarioController() {
        return new UsuarioController(daoFactory.usuarioDao(), daoFactory.rolDao());
    }
}
