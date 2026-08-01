package edu.university.system;

import edu.university.system.controller.AuthenticationResult;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.ControllerFactory;
import edu.university.system.controller.LoginController;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.dao.DaoFactory;
import edu.university.system.model.Pais;
import edu.university.system.model.UserSession;
import edu.university.system.utils.PasswordUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationAuthorizationTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void clearSession() {
        UserSession.clear();
    }

    @Test
    void authenticatesInitialAdminAndUpdatesLastAccess() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("auth.db"));
        LoginController loginController = new ControllerFactory(daoFactory).loginController();

        AuthenticationResult result = loginController.authenticate(" ADMIN ", TestSupport.adminPassword());

        assertTrue(result.isAuthenticated());
        assertEquals("Administrador", result.getSession().getRoleName());
        assertTrue(result.getSession().isDebeCambiarPassword());
        assertNotNull(daoFactory.usuarioDao().buscarPorNombreUsuario("admin").orElseThrow().getUltimoAcceso());
    }

    @Test
    void rejectsInvalidCredentialsMissingUsersAndInactiveUsers() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("reject.db"));
        LoginController loginController = new ControllerFactory(daoFactory).loginController();

        assertFalse(loginController.authenticate("admin", TestSupport.invalidPassword()).isAuthenticated());
        assertFalse(loginController.authenticate("nadie", TestSupport.adminPassword()).isAuthenticated());

        daoFactory.usuarioDao().actualizarActivo(2L, false);
        assertFalse(loginController.authenticate("consulta", TestSupport.consultaPassword()).isAuthenticated());
    }

    @Test
    void passwordHashesRemainCompatibleWithSeedUsers() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("hash.db"));

        String adminHash = daoFactory.usuarioDao().buscarPorNombreUsuario("admin").orElseThrow().getPasswordHash();
        String consultaHash = daoFactory.usuarioDao().buscarPorNombreUsuario("consulta").orElseThrow().getPasswordHash();

        assertTrue(PasswordUtils.matches(TestSupport.adminPassword(), adminHash));
        assertTrue(PasswordUtils.matches(TestSupport.consultaPassword(), consultaHash));
    }

    @Test
    void permissionsMatchAdministradorAndConsultaRoles() {
        TestSupport.startAdminSession();
        assertTrue(AuthorizationService.can(Permission.CREATE));
        assertTrue(AuthorizationService.can(Permission.UPDATE));
        assertTrue(AuthorizationService.can(Permission.DELETE));
        assertTrue(AuthorizationService.can(Permission.MANAGE_USERS));

        TestSupport.startConsultaSession();
        assertFalse(AuthorizationService.can(Permission.CREATE));
        assertFalse(AuthorizationService.can(Permission.UPDATE));
        assertFalse(AuthorizationService.can(Permission.DELETE));
        assertFalse(AuthorizationService.can(Permission.MANAGE_USERS));
        assertTrue(AuthorizationService.can(Permission.READ));
        assertTrue(AuthorizationService.can(Permission.EXPORT));
    }

    @Test
    void consultaCannotMutateThroughControllers() {
        DaoFactory daoFactory = TestSupport.daoFactory(tempDir.resolve("controller-permissions.db"));
        ControllerFactory controllerFactory = new ControllerFactory(daoFactory);

        TestSupport.startConsultaSession();

        assertThrows(ValidationException.class, () -> controllerFactory.paisController().insertar(new Pais(null, "Belice", "BZ")));
        assertThrows(ValidationException.class, () -> controllerFactory.usuarioController().listar());
        assertDoesNotThrow(() -> controllerFactory.paisController().listar());
        assertDoesNotThrow(() -> controllerFactory.paisController().buscar("Hon"));
    }
}
