package edu.university.system.view;

import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.EmpresaController;
import edu.university.system.controller.PaisController;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Empresa;
import edu.university.system.model.Pais;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
 * Formulario MDI para el CRUD de empresas. Consume controladores y adapta las
 * acciones de escritura segun permisos de la sesion.
 */
public final class EmpresaInternalFrame extends JInternalFrame {

    private final EmpresaController empresaController;
    private final PaisController paisController;
    private final EmpresaTableModel tableModel;
    private final JTable empresaTable;
    private final JTextField idField;
    private final JTextField nombreField;
    private final JTextField rtnField;
    private final JTextField telefonoField;
    private final JTextField correoField;
    private final JTextField direccionField;
    private final JTextField searchField;
    private final JComboBox<Pais> paisComboBox;
    private Long selectedEmpresaId;

    public EmpresaInternalFrame(EmpresaController empresaController, PaisController paisController) {
        super("Modulo Empresas", true, true, true, true);
        this.empresaController = empresaController;
        this.paisController = paisController;
        this.tableModel = new EmpresaTableModel();
        this.empresaTable = new JTable(tableModel);
        this.idField = new JTextField(8);
        this.nombreField = new JTextField(28);
        this.rtnField = new JTextField(18);
        this.telefonoField = new JTextField(14);
        this.correoField = new JTextField(24);
        this.direccionField = new JTextField(36);
        this.searchField = new JTextField(28);
        this.paisComboBox = new JComboBox<>();
        this.selectedEmpresaId = null;

        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadCatalogs();
        loadEmpresas();
    }

    private void configureFrame() {
        WindowIconUtils.applyTo(this);
        setSize(1040, 600);
        setMinimumSize(new java.awt.Dimension(900, 500));
    }

    private void configureTable() {
        ViewFeedback.configureTable(empresaTable);
        TableRowSorter<EmpresaTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        empresaTable.setRowSorter(sorter);
        empresaTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedEmpresa();
            }
        });
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        rootPanel.add(createFormPanel(), BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(empresaTable), BorderLayout.CENTER);
        rootPanel.add(createActionPanel(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos de la empresa"));
        idField.setEditable(false);
        idField.setHorizontalAlignment(SwingConstants.RIGHT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 6, 5, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addField(formPanel, constraints, 0, 0, "ID", idField);
        addField(formPanel, constraints, 2, 0, "Pais", paisComboBox);
        addField(formPanel, constraints, 0, 1, "Nombre", nombreField);
        addField(formPanel, constraints, 2, 1, "RTN", rtnField);
        addField(formPanel, constraints, 4, 1, "Telefono", telefonoField);
        addField(formPanel, constraints, 0, 2, "Correo", correoField);
        addField(formPanel, constraints, 2, 2, "Direccion", direccionField);
        addField(formPanel, constraints, 0, 3, "Buscar", searchField);

        ViewFeedback.addInstantSearch(searchField, this::searchEmpresas);
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
        JButton deleteButton = new JButton("Eliminar");
        JButton refreshButton = new JButton("Refrescar");
        newButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> saveEmpresa());
        deleteButton.addActionListener(event -> deleteEmpresa());
        refreshButton.addActionListener(event -> refreshEmpresas());
        boolean canManage = AuthorizationService.can(Permission.MANAGE_COMPANIES);
        newButton.setEnabled(canManage);
        saveButton.setEnabled(canManage);
        deleteButton.setEnabled(canManage);
        actionPanel.add(newButton);
        actionPanel.add(saveButton);
        actionPanel.add(deleteButton);
        actionPanel.add(refreshButton);
        return actionPanel;
    }

    private void loadCatalogs() {
        try {
            paisComboBox.removeAllItems();
            for (Pais pais : paisController.listar()) {
                paisComboBox.addItem(pais);
            }
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los paises.", exception);
        }
    }

    private void loadEmpresas() {
        try {
            tableModel.setEmpresas(empresaController.listar());
            clearForm();
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar las empresas.", exception);
        }
    }

    private void refreshEmpresas() {
        searchField.setText("");
        loadCatalogs();
        loadEmpresas();
    }

    private void searchEmpresas() {
        try {
            tableModel.setEmpresas(empresaController.buscar(searchField.getText()));
        } catch (RuntimeException exception) {
            showError("No se pudo realizar la busqueda.", exception);
        }
    }

    private void loadSelectedEmpresa() {
        int selectedRow = empresaTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        Empresa empresa = tableModel.getEmpresaAt(empresaTable.convertRowIndexToModel(selectedRow));
        selectedEmpresaId = empresa.getId();
        idField.setText(String.valueOf(empresa.getId()));
        nombreField.setText(empresa.getNombre());
        rtnField.setText(empresa.getRtn());
        telefonoField.setText(empresa.getTelefono());
        correoField.setText(empresa.getCorreoElectronico());
        direccionField.setText(empresa.getDireccion());
        selectPais(empresa.getPais());
    }

    private void saveEmpresa() {
        try {
            if (!validateForm()) {
                return;
            }
            Empresa empresa = buildEmpresaFromForm();
            if (selectedEmpresaId == null) {
                empresaController.insertar(empresa);
                JOptionPane.showMessageDialog(this, "Empresa registrada correctamente.", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                empresaController.actualizar(empresa);
                JOptionPane.showMessageDialog(this, "Empresa actualizada correctamente.", "Actualizacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshEmpresas();
        } catch (ValidationException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar la empresa.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(nombreField, rtnField, telefonoField, correoField, direccionField, paisComboBox);
        boolean invalid = false;
        invalid |= ViewFeedback.markEmptyCombo(paisComboBox, "Seleccione un pais.");
        invalid |= ViewFeedback.markBlank(nombreField, "Ingrese el nombre de la empresa.");
        invalid |= ViewFeedback.markBlank(rtnField, "Ingrese el RTN.");
        invalid |= ViewFeedback.markBlank(direccionField, "Ingrese la direccion.");
        if (!telefonoField.getText().isBlank() && !telefonoField.getText().trim().matches("[0-9+()\\-\\s]{7,20}")) {
            ViewFeedback.markInvalid(telefonoField, "Telefono invalido.");
            invalid = true;
        }
        if (!correoField.getText().isBlank() && !correoField.getText().contains("@")) {
            ViewFeedback.markInvalid(correoField, "Correo invalido.");
            invalid = true;
        }
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private Empresa buildEmpresaFromForm() {
        return new Empresa(
                selectedEmpresaId,
                nombreField.getText(),
                rtnField.getText() == null ? null : rtnField.getText().trim(),
                telefonoField.getText(),
                correoField.getText(),
                direccionField.getText(),
                (Pais) paisComboBox.getSelectedItem()
        );
    }

    private void deleteEmpresa() {
        if (selectedEmpresaId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una empresa para eliminar.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this, "Desea eliminar la empresa seleccionada?", "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            empresaController.eliminar(selectedEmpresaId);
            JOptionPane.showMessageDialog(this, "Empresa eliminada correctamente.", "Eliminacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshEmpresas();
        } catch (RuntimeException exception) {
            showError("No se pudo eliminar la empresa. Verifique que no tenga proyectos relacionados.", exception);
        }
    }

    private void clearForm() {
        selectedEmpresaId = null;
        idField.setText("");
        nombreField.setText("");
        rtnField.setText("");
        telefonoField.setText("");
        correoField.setText("");
        direccionField.setText("");
        if (paisComboBox.getItemCount() > 0) {
            paisComboBox.setSelectedIndex(0);
        }
        ViewFeedback.clearInvalid(nombreField, rtnField, telefonoField, correoField, direccionField, paisComboBox);
        empresaTable.clearSelection();
    }

    private void selectPais(Pais pais) {
        if (pais == null || pais.getId() == null) {
            return;
        }
        for (int index = 0; index < paisComboBox.getItemCount(); index++) {
            if (pais.getId().equals(paisComboBox.getItemAt(index).getId())) {
                paisComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void showError(String message, RuntimeException exception) {
        ViewFeedback.showError(this, message, exception);
    }
}
