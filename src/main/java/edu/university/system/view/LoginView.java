package edu.university.system.view;

import edu.university.system.config.AppConfig;
import edu.university.system.controller.AuthenticationResult;
import edu.university.system.controller.LoginController;
import edu.university.system.model.UserSession;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

public final class LoginView extends JDialog {

    private final LoginController loginController;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private boolean authenticated;
    private UserSession session;

    public LoginView(JFrame owner, AppConfig appConfig, LoginController loginController) {
        super(owner, "Inicio de sesion", true);
        this.loginController = loginController;
        this.usernameField = new JTextField(22);
        this.passwordField = new JPasswordField(22);
        this.authenticated = false;
        this.session = null;

        WindowIconUtils.applyTo(this);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(createContent(appConfig));
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public UserSession getSession() {
        return session;
    }

    private JPanel createContent(AppConfig appConfig) {
        JPanel rootPanel = new JPanel(new BorderLayout(16, 16));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));
        rootPanel.setBackground(AppTheme.BLUE_PALE);

        JLabel titleLabel = new JLabel(appConfig.getApplicationName());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppTheme.NAVY);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(7, 0, 7, 0);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        formPanel.add(new JLabel("Usuario"), constraints);

        constraints.gridy = 1;
        formPanel.add(usernameField, constraints);

        constraints.gridy = 2;
        formPanel.add(new JLabel("Contrasena"), constraints);

        constraints.gridy = 3;
        formPanel.add(passwordField, constraints);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        JButton cancelButton = new JButton("Cancelar");
        JButton loginButton = new JButton("Ingresar");
        loginButton.setBackground(AppTheme.BLUE);
        loginButton.setForeground(AppTheme.WHITE);
        cancelButton.setBackground(AppTheme.WHITE);
        cancelButton.setForeground(AppTheme.NAVY);

        cancelButton.addActionListener(event -> dispose());
        loginButton.addActionListener(event -> authenticate());
        passwordField.addActionListener(event -> authenticate());

        buttonPanel.add(cancelButton);
        buttonPanel.add(loginButton);

        rootPanel.add(titleLabel, BorderLayout.NORTH);
        rootPanel.add(formPanel, BorderLayout.CENTER);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);
        return rootPanel;
    }

    private void authenticate() {
        ViewFeedback.clearInvalid(usernameField, passwordField);
        boolean invalid = false;
        invalid |= ViewFeedback.markBlank(usernameField, "Ingrese el usuario.");
        invalid |= ViewFeedback.markBlank(passwordField, "Ingrese la contrasena.");
        if (invalid) {
            ViewFeedback.showValidation(this.getRootPane(), "Ingrese usuario y contrasena para continuar.");
            return;
        }

        char[] password = passwordField.getPassword();
        try {
            AuthenticationResult result = loginController.authenticate(usernameField.getText(), password);
            authenticated = result.isAuthenticated();
            if (authenticated) {
                session = result.getSession();
                dispose();
            } else {
                ViewFeedback.showValidation(this.getRootPane(), result.getMessage());
            }
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
