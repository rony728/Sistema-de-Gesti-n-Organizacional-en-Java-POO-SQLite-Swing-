package edu.university.system;

import edu.university.system.controller.AsignacionController;
import edu.university.system.controller.CargoController;
import edu.university.system.controller.ControllerFactory;
import edu.university.system.controller.DepartamentoController;
import edu.university.system.controller.EmpleadoController;
import edu.university.system.controller.EmpresaController;
import edu.university.system.controller.PaisController;
import edu.university.system.controller.ProyectoController;
import edu.university.system.controller.UsuarioController;
import edu.university.system.controller.ValidationException;
import edu.university.system.dao.DaoFactory;
import edu.university.system.model.Asignacion;
import edu.university.system.model.Cargo;
import edu.university.system.model.Departamento;
import edu.university.system.model.Empleado;
import edu.university.system.model.Empresa;
import edu.university.system.model.Pais;
import edu.university.system.model.Proyecto;
import edu.university.system.model.Rol;
import edu.university.system.model.UserSession;
import edu.university.system.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ControllerValidationTest {

    @TempDir
    Path tempDir;

    private ControllerFactory controllerFactory;

    @BeforeEach
    void setUp() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("validation.db"));
        controllerFactory = new ControllerFactory(daoFactory);
        TestSupport.startAdminSession();
    }

    @AfterEach
    void tearDown() {
        UserSession.clear();
    }

    @Test
    void validatesPais() {
        PaisController controller = controllerFactory.paisController();
        assertThrows(ValidationException.class, () -> controller.insertar(new Pais(null, "A", "H")));
        assertDoesNotThrow(() -> controller.insertar(new Pais(null, "Belice", "BZ")));
    }

    @Test
    void validatesEmpresa() {
        EmpresaController controller = controllerFactory.empresaController();
        Pais pais = new Pais(1L, "Honduras", "HN");

        assertThrows(ValidationException.class, () -> controller.insertar(new Empresa(null, "", "1", "", "correo", "", null)));
        assertThrows(ValidationException.class, () -> controller.insertar(new Empresa(null, "Empresa", "12345678", "abc", "correo@local", "Direccion", pais)));
    }

    @Test
    void validatesDepartamento() {
        DepartamentoController controller = controllerFactory.departamentoController();
        Empresa empresa = new Empresa(1L, "Universidad Tecnologica de Honduras", "08019999000001", null, null, "Campus", new Pais(1L, "Honduras", "HN"));

        assertThrows(ValidationException.class, () -> controller.insertar(new Departamento(null, null, "Tecnologia", BigDecimal.ZERO)));
        assertThrows(ValidationException.class, () -> controller.insertar(new Departamento(null, empresa, "T", BigDecimal.ZERO)));
        assertThrows(ValidationException.class, () -> controller.insertar(new Departamento(null, empresa, "Tecnologia", BigDecimal.valueOf(-1))));
    }

    @Test
    void validatesEmpleado() {
        EmpleadoController controller = controllerFactory.empleadoController();
        Empresa empresa = new Empresa(1L, "Universidad Tecnologica de Honduras", "08019999000001", null, null, "Campus", new Pais(1L, "Honduras", "HN"));
        Departamento departamento = new Departamento(1L, empresa, "Tecnologia", BigDecimal.ZERO);
        Cargo cargo = new Cargo(1L, "Administrador de Sistemas", "Administra");

        Empleado invalid = new Empleado(null, "1", "A", "B", null, "bad", "", null, "E", null, BigDecimal.ZERO, cargo, departamento);
        assertThrows(ValidationException.class, () -> controller.insertar(invalid));
    }

    @Test
    void validatesCargo() {
        CargoController controller = controllerFactory.cargoController();

        assertThrows(ValidationException.class, () -> controller.insertar(new Cargo(null, "", "Desc", BigDecimal.ZERO)));
        assertThrows(ValidationException.class, () -> controller.insertar(new Cargo(null, "Cargo negativo", "Desc", BigDecimal.valueOf(-1))));
        assertDoesNotThrow(() -> controller.insertar(new Cargo(null, "Cargo prueba", "Desc", BigDecimal.ZERO)));
    }

    @Test
    void validatesProyectoAndAsignacion() {
        ProyectoController proyectoController = controllerFactory.proyectoController();
        AsignacionController asignacionController = controllerFactory.asignacionController();
        Empresa empresa = new Empresa(1L, "Empresa", "12345678", null, null, "Direccion", new Pais(1L, "Honduras", "HN"));
        Proyecto proyecto = new Proyecto(null, "PR1", "Proyecto", null, LocalDate.now(), LocalDate.now().minusDays(1), BigDecimal.ZERO, empresa);

        assertThrows(ValidationException.class, () -> proyectoController.insertar(proyecto));
        assertThrows(ValidationException.class, () -> asignacionController.insertar(new Asignacion(null, null, null, "", null, null, -1)));
    }

    @Test
    void validatesUsuarioAndPasswordRules() {
        UsuarioController controller = controllerFactory.usuarioController();
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario("usr");
        usuario.setNombreMostrar("Usuario");
        usuario.setRol(new Rol(1L, "Administrador", "Admin", true));

        assertThrows(ValidationException.class, () -> controller.insertar(usuario, new char[]{'s', 'h', 'o', 'r', 't'}, new char[]{'s', 'h', 'o', 'r', 't'}));
        assertThrows(ValidationException.class, () -> controller.insertar(usuario, new char[]{'N', 'u', 'e', 'v', 'a', '1', '2', '3'}, new char[]{'N', 'u', 'e', 'v', 'a', '1', '2', '4'}));
    }
}
