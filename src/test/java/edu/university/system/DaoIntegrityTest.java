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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DaoIntegrityTest {

    @TempDir
    Path tempDir;

    @Test
    void createsSchemaRepeatedlyAndKeepsOrganizationalSeedData() {
        Path database = tempDir.resolve("schema.db");
        TestSupport.database(database).initialize();
        TestSupport.database(database).initialize();
        DaoFactory daoFactory = TestSupport.daoFactory(database);

        assertTrue(daoFactory.paisDao().listar().size() >= 6);
        assertTrue(daoFactory.empresaDao().buscar("Universidad Tecnologica").stream().findAny().isPresent());
        assertTrue(daoFactory.departamentoDao().listar().stream()
                .anyMatch(departamento -> "Tecnologia".equals(departamento.getNombre())
                        && departamento.getEmpresa() != null
                        && "Universidad Tecnologica de Honduras".equals(departamento.getEmpresa().getNombre())));
        assertTrue(daoFactory.usuarioDao().buscarPorNombreUsuario("admin").isPresent());
        assertTrue(daoFactory.rolDao().buscarPorNombre("Consulta").isPresent());
    }

    @Test
    void supportsCompanyAndOrganizationalDepartmentRules() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("crud.db"));

        Pais pais = new Pais(null, "Mexico", "MX");
        Long paisId = daoFactory.paisDao().insertar(pais);
        Empresa firstCompany = new Empresa(null, "Empresa Test", "RTN123456", "2222-3333", "empresa@test.local", "Direccion Test", new Pais(paisId, "Mexico", "MX"));
        Empresa secondCompany = new Empresa(null, "Empresa Dos", "RTN654321", null, "empresa2@test.local", "Direccion Dos", new Pais(paisId, "Mexico", "MX"));
        Long firstCompanyId = daoFactory.empresaDao().insertar(firstCompany);
        Long secondCompanyId = daoFactory.empresaDao().insertar(secondCompany);

        Departamento tecnologia = new Departamento(null, new Empresa(firstCompanyId, "Empresa Test", "RTN123456", null, null, "Direccion Test", new Pais(paisId, "Mexico", "MX")), "Tecnologia", BigDecimal.valueOf(1000));
        Long departamentoId = daoFactory.departamentoDao().insertar(tecnologia);
        assertEquals("Tecnologia", daoFactory.departamentoDao().buscarPorId(departamentoId).orElseThrow().getNombre());
        assertFalse(daoFactory.departamentoDao().listarPorEmpresa(firstCompanyId).isEmpty());
        assertFalse(daoFactory.departamentoDao().buscar("Tecnologia").isEmpty());

        Departamento sameNameOtherCompany = new Departamento(null, new Empresa(secondCompanyId, "Empresa Dos", "RTN654321", null, null, "Direccion Dos", new Pais(paisId, "Mexico", "MX")), "Tecnologia", BigDecimal.ZERO);
        assertDoesNotThrow(() -> daoFactory.departamentoDao().insertar(sameNameOtherCompany));
        assertThrows(DaoException.class, () -> daoFactory.departamentoDao().insertar(new Departamento(null, tecnologia.getEmpresa(), "Tecnologia", BigDecimal.ZERO)));

        tecnologia.setId(departamentoId);
        tecnologia.setNombre("Innovacion");
        tecnologia.setPresupuesto(BigDecimal.valueOf(2500));
        assertTrue(daoFactory.departamentoDao().actualizar(tecnologia));
        Departamento updated = daoFactory.departamentoDao().buscarPorId(departamentoId).orElseThrow();
        assertEquals("Innovacion", updated.getNombre());
        assertEquals(0, BigDecimal.valueOf(2500).compareTo(updated.getPresupuesto()));
        assertThrows(DaoException.class, () -> daoFactory.empresaDao().eliminar(firstCompanyId));
    }

    @Test
    void restrictsProjectDeletionWhenAssignedAndCascadesEmpleadoWithPersona() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("relations.db"));
        Empleado empleado = insertEmpleado(daoFactory, "0801199000001", "EMP100");
        Empresa empresa = new Empresa(null, "Empresa Proyecto", "12345678", null, null, "Direccion", new Pais(1L, "Honduras", "HN"));
        Long empresaId = daoFactory.empresaDao().insertar(empresa);
        Empresa persistedEmpresa = new Empresa(empresaId, "Empresa Proyecto", "12345678", null, null, "Direccion", new Pais(1L, "Honduras", "HN"));
        Proyecto proyecto = new Proyecto(null, "PR100", "Proyecto Relacionado", null, LocalDate.now(), null, BigDecimal.TEN, persistedEmpresa);
        Long proyectoId = daoFactory.proyectoDao().insertar(proyecto);
        daoFactory.asignacionDao().insertar(new edu.university.system.model.Asignacion(null, empleado, new Proyecto(proyectoId, "PR100", "Proyecto Relacionado", null, LocalDate.now(), null, BigDecimal.TEN, persistedEmpresa), "Lider", LocalDate.now(), null, 20));

        assertThrows(DaoException.class, () -> daoFactory.proyectoDao().eliminar(proyectoId));
        assertThrows(DaoException.class, () -> daoFactory.departamentoDao().eliminar(empleado.getDepartamento().getId()));

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

    @Test
    void migratesLegacyDepartmentSchemaIdempotently() throws Exception {
        Path database = tempDir.resolve("legacy.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("CREATE TABLE pais (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL UNIQUE, codigo_iso TEXT NOT NULL UNIQUE, activo INTEGER NOT NULL DEFAULT 1, fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')), fecha_actualizacion TEXT)");
            statement.execute("CREATE TABLE departamento (id INTEGER PRIMARY KEY AUTOINCREMENT, pais_id INTEGER NOT NULL, nombre TEXT NOT NULL, codigo TEXT NOT NULL, activo INTEGER NOT NULL DEFAULT 1, fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')), fecha_actualizacion TEXT)");
            statement.execute("CREATE TABLE empresa (id INTEGER PRIMARY KEY AUTOINCREMENT, pais_id INTEGER NOT NULL, departamento_id INTEGER NOT NULL, nombre TEXT NOT NULL, rtn TEXT NOT NULL UNIQUE, telefono TEXT, correo_electronico TEXT UNIQUE, direccion TEXT, activo INTEGER NOT NULL DEFAULT 1, fecha_creacion TEXT NOT NULL DEFAULT (datetime('now')), fecha_actualizacion TEXT)");
            statement.execute("INSERT INTO pais (id, nombre, codigo_iso) VALUES (1, 'Honduras', 'HN')");
            statement.execute("INSERT INTO departamento (id, pais_id, nombre, codigo) VALUES (1, 1, 'Atlantida', 'ATL')");
            statement.execute("INSERT INTO empresa (id, pais_id, departamento_id, nombre, rtn, direccion) VALUES (1, 1, 1, 'Empresa Antigua', '12345678', 'Direccion')");
        }

        TestSupport.database(database).initialize();
        TestSupport.database(database).initialize();
        DaoFactory daoFactory = TestSupport.daoFactory(database);

        assertTrue(daoFactory.empresaDao().buscar("Empresa Antigua").stream().findAny().isPresent());
        assertTrue(daoFactory.departamentoDao().listar().stream().anyMatch(departamento -> "Tecnologia".equals(departamento.getNombre())));
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            assertTrue(statement.executeQuery("SELECT COUNT(1) FROM departamento_geografico_backup").next());
        }
    }

    private Empleado insertEmpleado(DaoFactory daoFactory, String identidad, String codigo) {
        Empresa empresa = daoFactory.empresaDao().buscar("Universidad Tecnologica").stream().findFirst().orElseThrow();
        Departamento departamento = daoFactory.departamentoDao().listarPorEmpresa(empresa.getId()).stream().findFirst().orElseThrow();
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
                departamento
        );
        daoFactory.empleadoDao().insertar(empleado);
        return empleado;
    }
}
