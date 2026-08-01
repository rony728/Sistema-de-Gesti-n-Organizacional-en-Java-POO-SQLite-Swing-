package edu.university.system;

import edu.university.system.dao.DaoException;
import edu.university.system.dao.DaoFactory;
import edu.university.system.model.Cargo;
import edu.university.system.model.Departamento;
import edu.university.system.model.Empleado;
import edu.university.system.model.Empresa;
import edu.university.system.model.Pais;
import edu.university.system.model.Proyecto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DaoIntegrityTest {

    @TempDir
    Path tempDir;

    @Test
    void createsSchemaRepeatedlyAndKeepsSeedData() {
        Path database = tempDir.resolve("schema.db");
        TestSupport.database(database).initialize();
        DaoFactory daoFactory = TestSupport.daoFactory(database);

        assertTrue(daoFactory.paisDao().listar().size() >= 6);
        for (Pais pais : daoFactory.paisDao().listar()) {
            boolean hasDepartamento = daoFactory.departamentoDao().listar().stream()
                    .anyMatch(departamento -> departamento.getPais() != null && pais.getId().equals(departamento.getPais().getId()));
            assertTrue(hasDepartamento, "El pais semilla debe tener departamentos: " + pais.getNombre());
        }
        assertTrue(daoFactory.departamentoDao().listar().stream()
                .anyMatch(departamento -> "Costa Rica".equals(departamento.getPais().getNombre())
                        && "San Jose".equals(departamento.getNombre())));
        assertTrue(daoFactory.usuarioDao().buscarPorNombreUsuario("admin").isPresent());
        assertTrue(daoFactory.rolDao().buscarPorNombre("Consulta").isPresent());
    }

    @Test
    void supportsCrudSearchUniqueAndForeignKeyRestrictions() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("crud.db"));

        Pais pais = new Pais(null, "Mexico", "MX");
        Long paisId = daoFactory.paisDao().insertar(pais);
        Departamento departamento = new Departamento(null, "Jalisco", "JAL", new Pais(paisId, "Mexico", "MX"));
        Long departamentoId = daoFactory.departamentoDao().insertar(departamento);
        Empresa empresa = new Empresa(null, "Empresa Test", "RTN123456", "2222-3333", "empresa@test.local", "Direccion Test", new Pais(paisId, "Mexico", "MX"), new Departamento(departamentoId, "Jalisco", "JAL", new Pais(paisId, "Mexico", "MX")));
        Long empresaId = daoFactory.empresaDao().insertar(empresa);

        assertEquals("Empresa Test", daoFactory.empresaDao().buscarPorId(empresaId).orElseThrow().getNombre());
        empresa.setId(empresaId);
        empresa.setNombre("Empresa Actualizada");
        assertTrue(daoFactory.empresaDao().actualizar(empresa));
        assertFalse(daoFactory.empresaDao().buscar("Actualizada").isEmpty());
        assertThrows(DaoException.class, () -> daoFactory.paisDao().insertar(new Pais(null, "Mexico", "MX2")));
        assertThrows(DaoException.class, () -> daoFactory.paisDao().eliminar(paisId));
    }

    @Test
    void restrictsProjectDeletionWhenAssignedAndCascadesEmpleadoWithPersona() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("relations.db"));
        Empleado empleado = insertEmpleado(daoFactory, "0801199000001", "EMP100");
        Empresa empresa = new Empresa(1L, "Empresa", "12345678", null, null, "Direccion", new Pais(1L, "Honduras", "HN"), new Departamento(1L, "Atlantida", "ATL", new Pais(1L, "Honduras", "HN")));
        Long empresaId = daoFactory.empresaDao().insertar(empresa);
        Proyecto proyecto = new Proyecto(null, "PR100", "Proyecto Relacionado", null, LocalDate.now(), null, BigDecimal.TEN, new Empresa(empresaId, "Empresa", "12345678", null, null, "Direccion", new Pais(1L, "Honduras", "HN"), new Departamento(1L, "Atlantida", "ATL", new Pais(1L, "Honduras", "HN"))));
        Long proyectoId = daoFactory.proyectoDao().insertar(proyecto);
        daoFactory.asignacionDao().insertar(new edu.university.system.model.Asignacion(null, empleado, new Proyecto(proyectoId, "PR100", "Proyecto Relacionado", null, LocalDate.now(), null, BigDecimal.TEN, empresa), "Lider", LocalDate.now(), null, 20));

        assertThrows(DaoException.class, () -> daoFactory.proyectoDao().eliminar(proyectoId));

        Empleado empleadoSinAsignacion = insertEmpleado(daoFactory, "0801199000004", "EMP101");
        assertTrue(daoFactory.empleadoDao().eliminar(empleadoSinAsignacion.getId()));
        assertTrue(daoFactory.personaDao().buscarPorId(empleadoSinAsignacion.getId()).isEmpty());
    }

    @Test
    void empleadoInsertAndUpdateTransactionsRollbackOnFailure() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("tx.db"));
        Empleado first = insertEmpleado(daoFactory, "0801199000002", "EMP200");
        Empleado second = insertEmpleado(daoFactory, "0801199000003", "EMP201");

        second.setIdentidad(first.getIdentidad());
        second.setCodigoEmpleado("EMP202");
        assertThrows(DaoException.class, () -> daoFactory.empleadoDao().actualizar(second));

        Empleado persisted = daoFactory.empleadoDao().buscarPorId(second.getId()).orElseThrow();
        assertEquals("0801199000003", persisted.getIdentidad());
        assertEquals("EMP201", persisted.getCodigoEmpleado());
    }

    private Empleado insertEmpleado(DaoFactory daoFactory, String identidad, String codigo) {
        Empleado empleado = new Empleado(
                null,
                identidad,
                "Ana",
                "Prueba",
                null,
                identidad + "@test.local",
                "Direccion",
                LocalDate.of(1990, 1, 1),
                codigo,
                LocalDate.now(),
                BigDecimal.valueOf(1000),
                new Cargo(1L, "Administrador de Sistemas", "Administra"),
                new Departamento(1L, "Atlantida", "ATL", new Pais(1L, "Honduras", "HN"))
        );
        daoFactory.empleadoDao().insertar(empleado);
        return empleado;
    }
}
