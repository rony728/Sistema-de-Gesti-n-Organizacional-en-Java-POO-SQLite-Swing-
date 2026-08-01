package edu.university.system.view;

import edu.university.system.config.AppConfig;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.ControllerFactory;
import edu.university.system.controller.Permission;
import edu.university.system.model.UserSession;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;

public final class MainWindow extends JFrame {

    private final AppConfig appConfig;
    private final ControllerFactory controllerFactory;
    private final JDesktopPane desktopPane;
    private final UserSession session;
    private final Runnable logoutHandler;

    public MainWindow(
            AppConfig appConfig,
            ControllerFactory controllerFactory
    ) {
        this(
                appConfig,
                controllerFactory,
                UserSession.current(),
                null
        );
    }

    public MainWindow(
            AppConfig appConfig,
            ControllerFactory controllerFactory,
            UserSession session,
            Runnable logoutHandler
    ) {
        super(appConfig.getApplicationName());

        this.appConfig = appConfig;
        this.controllerFactory = controllerFactory;

        /*
         * El fondo se carga desde:
         * src/main/resources/images/fondo_principal.png
         */
        this.desktopPane = new BackgroundDesktopPane(
                "/images/fondo_principal.png"
        );

        this.session = session;
        this.logoutHandler = logoutHandler;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setJMenuBar(createMenuBar());
        setContentPane(createContent());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel createContent() {
        /*
         * Este color se mostrará si la imagen no puede cargarse.
         */
        desktopPane.setBackground(AppTheme.BLUE_LIGHT);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(AppTheme.NAVY);
        statusBar.setBorder(
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );

        String userText = session == null
                ? "Usuario no autenticado"
                : session.getDisplayName()
                + " | Rol: "
                + session.getRoleName();

        JLabel statusLabel = new JLabel(
                userText + " | Sistema listo"
        );

        statusLabel.setFont(
                statusLabel.getFont().deriveFont(Font.PLAIN, 12f)
        );
        statusLabel.setForeground(AppTheme.WHITE);

        statusBar.add(statusLabel, BorderLayout.WEST);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(AppTheme.BLUE_PALE);
        rootPanel.add(desktopPane, BorderLayout.CENTER);
        rootPanel.add(statusBar, BorderLayout.SOUTH);

        return rootPanel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu systemMenu = new JMenu("Sistema");

        JMenuItem homeItem = new JMenuItem("Inicio");
        JMenuItem changePasswordItem =
                new JMenuItem("Cambiar contrasena");
        JMenuItem logoutItem = new JMenuItem("Cerrar sesion");
        JMenuItem exitItem = new JMenuItem("Salir");

        homeItem.addActionListener(
                event -> showWelcomeMessage()
        );

        changePasswordItem.addActionListener(
                event -> changePassword()
        );

        logoutItem.addActionListener(
                event -> logout()
        );

        exitItem.addActionListener(event -> {
            UserSession.clear();
            dispose();
            System.exit(0);
        });

        systemMenu.add(homeItem);
        systemMenu.addSeparator();
        systemMenu.add(changePasswordItem);
        systemMenu.add(logoutItem);
        systemMenu.addSeparator();
        systemMenu.add(exitItem);

        JMenu catalogsMenu = new JMenu("Catalogos");

        JMenuItem paisesItem = new JMenuItem("Paises");
        JMenuItem departamentosItem =
                new JMenuItem("Departamentos");
        JMenuItem cargosItem = new JMenuItem("Cargos");
        JMenuItem empresasItem = new JMenuItem("Empresas");

        paisesItem.addActionListener(
                event -> openPaisModule()
        );

        departamentosItem.addActionListener(
                event -> openDepartamentoModule()
        );

        cargosItem.addActionListener(
                event -> openCargoModule()
        );

        empresasItem.addActionListener(
                event -> openEmpresaModule()
        );

        catalogsMenu.add(paisesItem);
        catalogsMenu.add(departamentosItem);
        catalogsMenu.add(cargosItem);
        catalogsMenu.add(empresasItem);

        JMenu managementMenu = new JMenu("Gestion");

        JMenuItem empleadosItem = new JMenuItem("Empleados");
        JMenuItem proyectosItem = new JMenuItem("Proyectos");
        JMenuItem asignacionesItem =
                new JMenuItem("Asignaciones");
        JMenuItem usuariosItem = new JMenuItem("Usuarios");

        empleadosItem.addActionListener(
                event -> openEmpleadoModule()
        );

        proyectosItem.addActionListener(
                event -> openProyectoModule()
        );

        asignacionesItem.addActionListener(
                event -> openAsignacionModule()
        );

        usuariosItem.addActionListener(
                event -> openUsuarioModule()
        );

        managementMenu.add(empleadosItem);
        managementMenu.add(proyectosItem);
        managementMenu.add(asignacionesItem);

        if (AuthorizationService.can(Permission.MANAGE_USERS)) {
            managementMenu.addSeparator();
            managementMenu.add(usuariosItem);
        }

        JMenu reportsMenu = new JMenu("Reportes");

        JMenuItem reportsInfoItem = new JMenuItem(
                "Exportaciones disponibles en Empleados y Proyectos"
        );

        reportsInfoItem.setEnabled(false);
        reportsMenu.add(reportsInfoItem);

        JMenu helpMenu = new JMenu("Ayuda");

        JMenuItem aboutItem = new JMenuItem("Acerca de");

        aboutItem.addActionListener(
                event -> showAboutDialog()
        );

        helpMenu.add(aboutItem);

        menuBar.add(systemMenu);
        menuBar.add(catalogsMenu);
        menuBar.add(managementMenu);
        menuBar.add(reportsMenu);
        menuBar.add(helpMenu);
        styleMenuBar(menuBar);

        return menuBar;
    }

    private void styleMenuBar(JMenuBar menuBar) {
        menuBar.setBackground(AppTheme.NAVY);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppTheme.ORANGE));
        for (Component component : menuBar.getComponents()) {
            if (component instanceof JMenu menu) {
                styleMenu(menu);
            }
        }
    }

    private void styleMenu(JMenu menu) {
        menu.setBackground(AppTheme.NAVY);
        menu.setForeground(AppTheme.WHITE);
        for (Component component : menu.getMenuComponents()) {
            if (component instanceof JMenuItem menuItem) {
                menuItem.setForeground(AppTheme.TEXT);
            }
        }
    }

    private void changePassword() {
        PasswordChangeDialog passwordChangeDialog =
                new PasswordChangeDialog(
                        this,
                        controllerFactory.usuarioController(),
                        false
                );

        passwordChangeDialog.setVisible(true);
    }

    private void logout() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.dispose();
        }

        UserSession.clear();
        dispose();

        if (logoutHandler != null) {
            logoutHandler.run();
        }
    }

    private void openPaisModule() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof PaisInternalFrame) {
                try {
                    frame.setSelected(true);
                    frame.toFront();
                } catch (java.beans.PropertyVetoException exception) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo activar el modulo Paises.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                return;
            }
        }

        PaisInternalFrame paisInternalFrame =
                new PaisInternalFrame(
                        controllerFactory.paisController()
                );

        desktopPane.add(paisInternalFrame);
        paisInternalFrame.setVisible(true);

        try {
            paisInternalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo seleccionar el modulo Paises.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openDepartamentoModule() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof DepartamentoInternalFrame) {
                try {
                    frame.setSelected(true);
                    frame.toFront();
                } catch (java.beans.PropertyVetoException exception) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo activar el modulo Departamentos.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                return;
            }
        }

        DepartamentoInternalFrame departamentoInternalFrame =
                new DepartamentoInternalFrame(
                        controllerFactory.departamentoController(),
                        controllerFactory.paisController()
                );

        desktopPane.add(departamentoInternalFrame);
        departamentoInternalFrame.setVisible(true);

        try {
            departamentoInternalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo seleccionar el modulo Departamentos.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openCargoModule() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof CargoInternalFrame) {
                try {
                    frame.setSelected(true);
                    frame.toFront();
                } catch (java.beans.PropertyVetoException exception) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo activar el modulo Cargos.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                return;
            }
        }

        CargoInternalFrame cargoInternalFrame =
                new CargoInternalFrame(
                        controllerFactory.cargoController()
                );

        desktopPane.add(cargoInternalFrame);
        cargoInternalFrame.setVisible(true);

        try {
            cargoInternalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo seleccionar el modulo Cargos.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openEmpresaModule() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof EmpresaInternalFrame) {
                try {
                    frame.setSelected(true);
                    frame.toFront();
                } catch (java.beans.PropertyVetoException exception) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo activar el modulo Empresas.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                return;
            }
        }

        EmpresaInternalFrame empresaInternalFrame =
                new EmpresaInternalFrame(
                        controllerFactory.empresaController(),
                        controllerFactory.paisController(),
                        controllerFactory.departamentoController()
                );

        desktopPane.add(empresaInternalFrame);
        empresaInternalFrame.setVisible(true);

        try {
            empresaInternalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo seleccionar el modulo Empresas.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openProyectoModule() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof ProyectoInternalFrame) {
                try {
                    frame.setSelected(true);
                    frame.toFront();
                } catch (java.beans.PropertyVetoException exception) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo activar el modulo Proyectos.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                return;
            }
        }

        ProyectoInternalFrame proyectoInternalFrame =
                new ProyectoInternalFrame(
                        controllerFactory.proyectoController(),
                        controllerFactory.empresaController()
                );

        desktopPane.add(proyectoInternalFrame);
        proyectoInternalFrame.setVisible(true);

        try {
            proyectoInternalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo seleccionar el modulo Proyectos.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openEmpleadoModule() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof EmpleadoInternalFrame) {
                try {
                    frame.setSelected(true);
                    frame.toFront();
                } catch (java.beans.PropertyVetoException exception) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo activar el modulo Empleados.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                return;
            }
        }

        EmpleadoInternalFrame empleadoInternalFrame =
                new EmpleadoInternalFrame(
                        controllerFactory.empleadoController(),
                        controllerFactory.cargoController(),
                        controllerFactory.departamentoController()
                );

        desktopPane.add(empleadoInternalFrame);
        empleadoInternalFrame.setVisible(true);

        try {
            empleadoInternalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo seleccionar el modulo Empleados.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openAsignacionModule() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof AsignacionInternalFrame) {
                try {
                    frame.setSelected(true);
                    frame.toFront();
                } catch (java.beans.PropertyVetoException exception) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo activar el modulo Asignaciones.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                return;
            }
        }

        AsignacionInternalFrame asignacionInternalFrame =
                new AsignacionInternalFrame(
                        controllerFactory.asignacionController(),
                        controllerFactory.empleadoController(),
                        controllerFactory.proyectoController()
                );

        desktopPane.add(asignacionInternalFrame);
        asignacionInternalFrame.setVisible(true);

        try {
            asignacionInternalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo seleccionar el modulo Asignaciones.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void openUsuarioModule() {
        if (!AuthorizationService.can(Permission.MANAGE_USERS)) {
            JOptionPane.showMessageDialog(
                    this,
                    "No tiene permisos para administrar usuarios.",
                    "Acceso restringido",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof UsuarioInternalFrame) {
                try {
                    frame.setSelected(true);
                    frame.toFront();
                } catch (java.beans.PropertyVetoException exception) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo activar el modulo Usuarios.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                return;
            }
        }

        UsuarioInternalFrame usuarioInternalFrame =
                new UsuarioInternalFrame(
                        controllerFactory.usuarioController()
                );

        desktopPane.add(usuarioInternalFrame);
        usuarioInternalFrame.setVisible(true);

        try {
            usuarioInternalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo seleccionar el modulo Usuarios.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void showWelcomeMessage() {
        JOptionPane.showMessageDialog(
                this,
                "Sistema listo para trabajar.",
                "Inicio",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                appConfig.getApplicationName()
                + "\nVersion "
                + appConfig.getApplicationVersion(),
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * JDesktopPane personalizado que muestra una imagen de fondo.
     *
     * La imagen se escala manteniendo su proporción y cubriendo todo
     * el espacio disponible. Los JInternalFrame continúan mostrándose
     * normalmente sobre el fondo.
     */
    private static final class BackgroundDesktopPane
            extends JDesktopPane {

        private final Image backgroundImage;

        public BackgroundDesktopPane(String resourcePath) {
            URL imageUrl =
                    MainWindow.class.getResource(resourcePath);

            if (imageUrl != null) {
                backgroundImage =
                        new ImageIcon(imageUrl).getImage();
            } else {
                backgroundImage = null;

                System.err.println(
                        "No se encontró la imagen de fondo: "
                        + resourcePath
                );
            }

            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (backgroundImage == null) {
                return;
            }

            int panelWidth = getWidth();
            int panelHeight = getHeight();

            int imageWidth =
                    backgroundImage.getWidth(this);

            int imageHeight =
                    backgroundImage.getHeight(this);

            if (panelWidth <= 0
                    || panelHeight <= 0
                    || imageWidth <= 0
                    || imageHeight <= 0) {
                return;
            }

            /*
             * Se usa Math.max para cubrir todo el panel.
             * La imagen conserva su proporción original.
             */
            double scale = Math.max(
                    (double) panelWidth / imageWidth,
                    (double) panelHeight / imageHeight
            );

            int scaledWidth =
                    (int) Math.ceil(imageWidth * scale);

            int scaledHeight =
                    (int) Math.ceil(imageHeight * scale);

            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;

            graphics.drawImage(
                    backgroundImage,
                    x,
                    y,
                    scaledWidth,
                    scaledHeight,
                    this
            );
        }
    }
}
