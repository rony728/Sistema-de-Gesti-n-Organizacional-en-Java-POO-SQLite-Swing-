package edu.university.system.view;

import edu.university.system.controller.UsuarioController;
import edu.university.system.controller.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Dialogo modal para cambio voluntario u obligatorio de contrasena. Delega las
 * reglas de validacion y persistencia al UsuarioController.
 */
public final class PasswordChangeDialog extends JDialog {

    private final UsuarioController usuarioController;
    private final boolean mandatory;
    private final JPasswordField currentPasswordField;
    private final JPasswordField newPasswordField;
    private final JPasswordField confirmationField;
    private boolean passwordChanged;

    public PasswordChangeDialog(Frame owner, UsuarioController usuarioController, boolean mandatory) {
        super(owner, "Cambiar contrasena", true);
        this.usuarioController = usuarioController;
        this.mandatory = mandatory;
        this.currentPasswordField = new JPasswordField(24);
        this.newPasswordField = new JPasswordField(24);
        this.confirmationField = new JPasswordField(24);
        this.passwordChanged = false;

        WindowIconUtils.applyTo(this);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(createContent());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));

        JLabel titleLabel = new JLabel(mandatory
                ? "Debe cambiar su contrasena para continuar."
                : "Actualice su contrasena de acceso.");

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        formPanel.add(new JLabel("Contrasena actual"), constraints);
        constraints.gridx = 1;
        formPanel.add(currentPasswordField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        formPanel.add(new JLabel("Nueva contrasena"), constraints);
        constraints.gridx = 1;
        formPanel.add(newPasswordField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        formPanel.add(new JLabel("Confirmar contrasena"), constraints);
        constraints.gridx = 1;
        formPanel.add(confirmationField, constraints);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancelButton = new JButton("Cancelar");
        JButton saveButton = new JButton("Guardar");
        cancelButton.addActionListener(event -> dispose());
        saveButton.addActionListener(event -> changePassword());
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        rootPanel.add(titleLabel, BorderLayout.NORTH);
        rootPanel.add(formPanel, BorderLayout.CENTER);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);
        return rootPanel;
    }

    private void changePassword() {
        ViewFeedback.clearInvalid(currentPasswordField, newPasswordField, confirmationField);
        char[] currentPassword = currentPasswordField.getPassword();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmation = confirmationField.getPassword();
        try {
            usuarioController.cambiarPasswordPropia(currentPassword, newPassword, confirmation);
            passwordChanged = true;
            JOptionPane.showMessageDialog(this, "Contrasena actualizada correctamente.", "Operacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ValidationException exception) {
            ViewFeedback.showValidation(this.getRootPane(), exception.getMessage());
        } catch (RuntimeException exception) {
            ViewFeedback.showError(this.getRootPane(), "No se pudo cambiar la contrasena.", exception);
        }
    }
}
