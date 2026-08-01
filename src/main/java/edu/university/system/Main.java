package edu.university.system;

import com.formdev.flatlaf.FlatLightLaf;
import edu.university.system.config.AppConfig;
import edu.university.system.config.DatabaseConnection;
import edu.university.system.controller.ControllerFactory;
import edu.university.system.controller.LoginController;
import edu.university.system.model.UserSession;
import edu.university.system.dao.DaoFactory;
import edu.university.system.view.AppTheme;
import edu.university.system.view.LoginView;
import edu.university.system.view.MainWindow;
import edu.university.system.view.PasswordChangeDialog;
import edu.university.system.view.SplashScreenView;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Punto de entrada de la aplicacion Swing. Inicializa FlatLaf, carga la
 * configuracion, prepara SQLite y coordina el flujo splash, login, cambio
 * obligatorio de contrasena y ventana principal MDI.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::startApplication);
    }

    private static void startApplication() {
        try {
            configureLookAndFeel();
            AppConfig appConfig = AppConfig.load();
            DatabaseConnection databaseConnection = new DatabaseConnection(appConfig);
            databaseConnection.initialize();
            ControllerFactory controllerFactory = new ControllerFactory(new DaoFactory(databaseConnection));

            SplashScreenView splashScreenView = new SplashScreenView(appConfig);
            splashScreenView.setVisible(true);

            Timer startupTimer = new Timer(1400, event -> {
                splashScreenView.dispose();
                showLogin(appConfig, controllerFactory);
            });
            startupTimer.setRepeats(false);
            startupTimer.start();
        } catch (RuntimeException exception) {
            showStartupError(exception);
            System.exit(1);
        }
    }

    private static void showLogin(AppConfig appConfig, ControllerFactory controllerFactory) {
        LoginController loginController = controllerFactory.loginController();
        LoginView loginView = new LoginView(null, appConfig, loginController);
        loginView.setVisible(true);

        if (loginView.isAuthenticated()) {
            UserSession session = loginView.getSession();
            if (session != null && session.isDebeCambiarPassword()) {
                PasswordChangeDialog passwordChangeDialog = new PasswordChangeDialog(null, controllerFactory.usuarioController(), true);
                passwordChangeDialog.setVisible(true);
                if (!passwordChangeDialog.isPasswordChanged()) {
                    UserSession.clear();
                    System.exit(0);
                    return;
                }
            }
            MainWindow mainWindow = new MainWindow(appConfig, controllerFactory, session, () -> showLogin(appConfig, controllerFactory));
            mainWindow.setVisible(true);
        } else {
            System.exit(0);
        }
    }

    private static void configureLookAndFeel() {
        FlatLightLaf.setup();
        AppTheme.install();
    }

    private static void showStartupError(RuntimeException exception) {
        JOptionPane.showMessageDialog(
                null,
                "No se pudo iniciar la aplicacion.\n" + exception.getMessage(),
                "Error de inicio",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
