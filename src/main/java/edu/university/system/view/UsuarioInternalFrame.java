package edu.university.system.view;

import edu.university.system.controller.UsuarioController;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Rol;
import edu.university.system.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Formulario MDI de administracion de usuarios. Permite altas, cambios de rol,
 * activacion/desactivacion y restablecimiento de contrasenas sin mostrar hashes
 * ni contrasenas.
 */
public final class UsuarioInternalFrame extends JInternalFrame {

    private final UsuarioController usuarioController;
    private final UsuarioTableModel tableModel;
    private final JTable usuarioTable;
    private final JTextField idField;
    private final JTextField usernameField;
    private final JTextField displayNameField;
    private final JTextField emailField;
    private final JTextField searchField;
    private final JComboBox<Rol> roleComboBox;
    private Long selectedUsuarioId;
    private boolean selectedActive;

    public UsuarioInternalFrame(UsuarioController usuarioController) {
        super("Modulo Usuarios", true, true, true, true);
        this.usuarioController = usuarioController;
        this.tableModel = new UsuarioTableModel();
        this.usuarioTable = new JTable(tableModel);
        this.idField = new JTextField(8);
        this.usernameField = new JTextField(18);
        this.displayNameField = new JTextField(26);
        this.emailField = new JTextField(26);
        this.searchField = new JTextField(28);
        this.roleComboBox = new JComboBox<>();
        this.selectedUsuarioId = null;
        this.selectedActive = true;

        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadRoles();
        loadUsuarios();
    }

    private void configureFrame() {
        setSize(980, 580);
        setMinimumSize(new java.awt.Dimension(850, 480));
    }

    private void configureTable() {
        ViewFeedback.configureTable(usuarioTable);
        TableRowSorter<UsuarioTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        usuarioTable.setRowSorter(sorter);
        usuarioTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedUsuario();
            }
        });
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        rootPanel.add(createFormPanel(), BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(usuarioTable), BorderLayout.CENTER);
        rootPanel.add(createActionPanel(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));
        idField.setEditable(false);
        idField.setHorizontalAlignment(SwingConstants.RIGHT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 6, 5, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addField(formPanel, constraints, 0, 0, "ID", idField);
        addField(formPanel, constraints, 2, 0, "Usuario", usernameField);
        addField(formPanel, constraints, 4, 0, "Rol", roleComboBox);
        addField(formPanel, constraints, 0, 1, "Nombre", displayNameField);
        addField(formPanel, constraints, 2, 1, "Correo", emailField);
        addField(formPanel, constraints, 0, 2, "Buscar", searchField);
        ViewFeedback.addInstantSearch(searchField, this::searchUsuarios);
        return formPanel;
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int x, int y, String label, java.awt.Component component) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = x + 1;
        constraints.weightx = 1;
        panel.add(component, constraints);
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton newButton = new JButton("Nuevo");
        JButton saveButton = new JButton("Guardar");
        JButton toggleButton = new JButton("Activar/Desactivar");
        JButton resetPasswordButton = new JButton("Restablecer contrasena");
        JButton refreshButton = new JButton("Refrescar");
        newButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> saveUsuario());
        toggleButton.addActionListener(event -> toggleUsuario());
        resetPasswordButton.addActionListener(event -> resetPassword());
        refreshButton.addActionListener(event -> refreshUsuarios());
        actionPanel.add(newButton);
        actionPanel.add(saveButton);
        actionPanel.add(toggleButton);
        actionPanel.add(resetPasswordButton);
        actionPanel.add(refreshButton);
        return actionPanel;
    }

    private void loadRoles() {
        try {
            roleComboBox.removeAllItems();
            for (Rol rol : usuarioController.listarRolesActivos()) {
                roleComboBox.addItem(rol);
            }
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los roles.", exception);
        }
    }

    private void loadUsuarios() {
        try {
            tableModel.setUsuarios(usuarioController.listar());
            clearForm();
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los usuarios.", exception);
        }
    }

    private void refreshUsuarios() {
        searchField.setText("");
        loadRoles();
        loadUsuarios();
    }

    private void searchUsuarios() {
        try {
            tableModel.setUsuarios(usuarioController.buscar(searchField.getText()));
        } catch (RuntimeException exception) {
            showError("No se pudo realizar la busqueda.", exception);
        }
    }

    private void loadSelectedUsuario() {
        int selectedRow = usuarioTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        Usuario usuario = tableModel.getUsuarioAt(usuarioTable.convertRowIndexToModel(selectedRow));
        selectedUsuarioId = usuario.getId();
        selectedActive = usuario.isActivo();
        idField.setText(String.valueOf(usuario.getId()));
        usernameField.setText(usuario.getNombreUsuario());
        usernameField.setEditable(false);
        displayNameField.setText(usuario.getNombreMostrar());
        emailField.setText(usuario.getCorreoElectronico());
        selectRol(usuario.getRol());
    }

    private void saveUsuario() {
        try {
            if (!validateForm()) {
                return;
            }
            Usuario usuario = buildUsuarioFromForm();
            if (selectedUsuarioId == null) {
                PasswordInput passwordInput = requestPassword("Contrasena inicial", true);
                if (passwordInput == null) {
                    return;
                }
                usuarioController.insertar(usuario, passwordInput.password(), passwordInput.confirmation());
                JOptionPane.showMessageDialog(this, "Usuario registrado correctamente.", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                usuarioController.actualizarDatosGenerales(usuario);
                JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente.", "Actualizacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshUsuarios();
        } catch (ValidationException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar el usuario.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(usernameField, displayNameField, emailField, roleComboBox);
        boolean invalid = false;
        if (selectedUsuarioId == null) {
            invalid |= ViewFeedback.markBlank(usernameField, "Ingrese el usuario.");
        }
        invalid |= ViewFeedback.markBlank(displayNameField, "Ingrese el nombre para mostrar.");
        invalid |= ViewFeedback.markEmptyCombo(roleComboBox, "Seleccione un rol.");
        if (!emailField.getText().isBlank() && !emailField.getText().contains("@")) {
            ViewFeedback.markInvalid(emailField, "Correo invalido.");
            invalid = true;
        }
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private Usuario buildUsuarioFromForm() {
        Usuario usuario = new Usuario();
        usuario.setId(selectedUsuarioId);
        usuario.setNombreUsuario(usernameField.getText());
        usuario.setNombreMostrar(displayNameField.getText());
        usuario.setCorreoElectronico(emailField.getText());
        usuario.setRol((Rol) roleComboBox.getSelectedItem());
        usuario.setActivo(selectedActive);
        return usuario;
    }

    private void toggleUsuario() {
        if (selectedUsuarioId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            usuarioController.activarDesactivar(selectedUsuarioId, !selectedActive);
            JOptionPane.showMessageDialog(this, "Estado del usuario actualizado.", "Operacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshUsuarios();
        } catch (ValidationException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo actualizar el estado.", exception);
        }
    }

    private void resetPassword() {
        if (selectedUsuarioId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PasswordInput passwordInput = requestPassword("Restablecer contrasena", false);
        if (passwordInput == null) {
            return;
        }
        try {
            usuarioController.restablecerPassword(selectedUsuarioId, passwordInput.password(), passwordInput.confirmation(), passwordInput.forceChange());
            JOptionPane.showMessageDialog(this, "Contrasena restablecida correctamente.", "Operacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshUsuarios();
        } catch (ValidationException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo restablecer la contrasena.", exception);
        }
    }

    private PasswordInput requestPassword(String title, boolean forceChangeDefault) {
        JPasswordField passwordField = new JPasswordField(18);
        JPasswordField confirmationField = new JPasswordField(18);
        JCheckBox forceChangeBox = new JCheckBox("Debe cambiar contrasena en el siguiente acceso", forceChangeDefault);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(new JLabel("Contrasena"), constraints);
        constraints.gridx = 1;
        panel.add(passwordField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(new JLabel("Confirmacion"), constraints);
        constraints.gridx = 1;
        panel.add(confirmationField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        panel.add(forceChangeBox, constraints);
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        return new PasswordInput(passwordField.getPassword(), confirmationField.getPassword(), forceChangeBox.isSelected());
    }

    private void clearForm() {
        selectedUsuarioId = null;
        selectedActive = true;
        idField.setText("");
        usernameField.setText("");
        usernameField.setEditable(true);
        displayNameField.setText("");
        emailField.setText("");
        if (roleComboBox.getItemCount() > 0) {
            roleComboBox.setSelectedIndex(0);
        }
        ViewFeedback.clearInvalid(usernameField, displayNameField, emailField, roleComboBox);
        usuarioTable.clearSelection();
    }

    private void selectRol(Rol rol) {
        if (rol == null || rol.getId() == null) {
            return;
        }
        for (int index = 0; index < roleComboBox.getItemCount(); index++) {
            if (rol.getId().equals(roleComboBox.getItemAt(index).getId())) {
                roleComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void showError(String message, RuntimeException exception) {
        ViewFeedback.showError(this, message, exception);
    }

    private record PasswordInput(char[] password, char[] confirmation, boolean forceChange) {
    }
}
