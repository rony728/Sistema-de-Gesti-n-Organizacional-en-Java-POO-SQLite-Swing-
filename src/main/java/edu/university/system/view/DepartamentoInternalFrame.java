package edu.university.system.view;

import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.DepartamentoController;
import edu.university.system.controller.EmpresaController;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Departamento;
import edu.university.system.model.Empresa;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import java.math.BigDecimal;
import java.util.List;

public final class DepartamentoInternalFrame extends JInternalFrame {

    private final DepartamentoController departamentoController;
    private final EmpresaController empresaController;
    private final DepartamentoTableModel tableModel;
    private final JTable departamentoTable;
    private final JTextField idField;
    private final JTextField nombreField;
    private final JTextField presupuestoField;
    private final JTextField searchField;
    private final JComboBox<Empresa> empresaComboBox;
    private final JCheckBox activoCheckBox;
    private Long selectedDepartamentoId;

    public DepartamentoInternalFrame(DepartamentoController departamentoController, EmpresaController empresaController) {
        super("Modulo Departamentos", true, true, true, true);
        this.departamentoController = departamentoController;
        this.empresaController = empresaController;
        this.tableModel = new DepartamentoTableModel();
        this.departamentoTable = new JTable(tableModel);
        this.idField = new JTextField(10);
        this.nombreField = new JTextField(26);
        this.presupuestoField = new JTextField(12);
        this.searchField = new JTextField(28);
        this.empresaComboBox = new JComboBox<>();
        this.activoCheckBox = new JCheckBox("Activo", true);
        this.selectedDepartamentoId = null;

        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadEmpresas();
        loadDepartamentos();
    }

    private void configureFrame() {
        WindowIconUtils.applyTo(this);
        setSize(860, 540);
        setMinimumSize(new java.awt.Dimension(760, 440));
    }

    private void configureTable() {
        ViewFeedback.configureTable(departamentoTable);
        departamentoTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        departamentoTable.getColumnModel().getColumn(1).setPreferredWidth(240);
        departamentoTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        departamentoTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        departamentoTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        TableRowSorter<DepartamentoTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING), new RowSorter.SortKey(2, SortOrder.ASCENDING)));
        departamentoTable.setRowSorter(sorter);

        departamentoTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedDepartamento();
            }
        });
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        rootPanel.add(createFormPanel(), BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(departamentoTable), BorderLayout.CENTER);
        rootPanel.add(createActionPanel(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del departamento"));

        idField.setEditable(false);
        idField.setHorizontalAlignment(SwingConstants.RIGHT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 6, 5, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addField(formPanel, constraints, 0, 0, "ID", idField);
        addField(formPanel, constraints, 2, 0, "Empresa", empresaComboBox);
        addField(formPanel, constraints, 0, 1, "Nombre", nombreField);
        addField(formPanel, constraints, 2, 1, "Presupuesto", presupuestoField);

        constraints.gridx = 4;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        formPanel.add(activoCheckBox, constraints);

        addField(formPanel, constraints, 0, 2, "Buscar", searchField, 5);
        ViewFeedback.addInstantSearch(searchField, this::searchDepartamentos);

        return formPanel;
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int x, int y, String label, java.awt.Component component) {
        addField(panel, constraints, x, y, label, component, 1);
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int x, int y, String label, java.awt.Component component, int componentGridWidth) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = x + 1;
        constraints.gridwidth = componentGridWidth;
        constraints.weightx = 1;
        panel.add(component, constraints);
        constraints.gridwidth = 1;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton newButton = new JButton("Nuevo");
        JButton saveButton = new JButton("Guardar");
        JButton deleteButton = new JButton("Eliminar");
        JButton refreshButton = new JButton("Refrescar");

        newButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> saveDepartamento());
        deleteButton.addActionListener(event -> deleteDepartamento());
        refreshButton.addActionListener(event -> refreshDepartamentos());

        boolean canModify = AuthorizationService.can(Permission.CREATE);
        newButton.setEnabled(canModify);
        saveButton.setEnabled(canModify);
        deleteButton.setEnabled(AuthorizationService.can(Permission.DELETE));

        actionPanel.add(newButton);
        actionPanel.add(saveButton);
        actionPanel.add(deleteButton);
        actionPanel.add(refreshButton);
        return actionPanel;
    }

    private void loadEmpresas() {
        try {
            empresaComboBox.removeAllItems();
            for (Empresa empresa : empresaController.listar()) {
                empresaComboBox.addItem(empresa);
            }
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar las empresas.", exception);
        }
    }

    private void loadDepartamentos() {
        try {
            tableModel.setDepartamentos(departamentoController.listar());
            clearForm();
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los departamentos.", exception);
        }
    }

    private void refreshDepartamentos() {
        searchField.setText("");
        loadEmpresas();
        loadDepartamentos();
    }

    private void searchDepartamentos() {
        try {
            tableModel.setDepartamentos(departamentoController.buscar(searchField.getText()));
        } catch (RuntimeException exception) {
            showError("No se pudo realizar la busqueda.", exception);
        }
    }

    private void loadSelectedDepartamento() {
        int selectedRow = departamentoTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int modelRow = departamentoTable.convertRowIndexToModel(selectedRow);
        Departamento departamento = tableModel.getDepartamentoAt(modelRow);
        selectedDepartamentoId = departamento.getId();
        idField.setText(String.valueOf(departamento.getId()));
        nombreField.setText(departamento.getNombre());
        presupuestoField.setText(departamento.getPresupuesto() == null ? "0" : departamento.getPresupuesto().toPlainString());
        activoCheckBox.setSelected(departamento.isActivo());
        selectEmpresa(departamento.getEmpresa());
    }

    private void saveDepartamento() {
        try {
            if (!validateForm()) {
                return;
            }
            Departamento departamento = buildDepartamentoFromForm();
            if (selectedDepartamentoId == null) {
                departamentoController.insertar(departamento);
                JOptionPane.showMessageDialog(this, "Departamento registrado correctamente.", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                departamentoController.actualizar(departamento);
                JOptionPane.showMessageDialog(this, "Departamento actualizado correctamente.", "Actualizacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshDepartamentos();
        } catch (ValidationException | IllegalArgumentException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar el departamento.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(nombreField, presupuestoField, empresaComboBox);
        boolean invalid = false;
        invalid |= ViewFeedback.markEmptyCombo(empresaComboBox, "Seleccione una empresa.");
        invalid |= ViewFeedback.markBlank(nombreField, "Ingrese el nombre del departamento.");
        invalid |= ViewFeedback.markInvalidDecimal(presupuestoField, "Presupuesto");
        if (!invalid && parseMoney(presupuestoField.getText()).compareTo(BigDecimal.ZERO) < 0) {
            ViewFeedback.markInvalid(presupuestoField, "El presupuesto no puede ser negativo.");
            invalid = true;
        }
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private Departamento buildDepartamentoFromForm() {
        Departamento departamento = new Departamento(
                selectedDepartamentoId,
                (Empresa) empresaComboBox.getSelectedItem(),
                nombreField.getText(),
                parseMoney(presupuestoField.getText()),
                activoCheckBox.isSelected()
        );
        if (selectedDepartamentoId != null) {
            departamento.setId(selectedDepartamentoId);
        }
        return departamento;
    }

    private BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Presupuesto debe ser numerico.");
        }
    }

    private void deleteDepartamento() {
        if (selectedDepartamentoId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un departamento para eliminar.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Desea eliminar el departamento seleccionado?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            departamentoController.eliminar(selectedDepartamentoId);
            JOptionPane.showMessageDialog(this, "Departamento eliminado correctamente.", "Eliminacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshDepartamentos();
        } catch (RuntimeException exception) {
            showError("No se pudo eliminar el departamento. Verifique que no tenga empleados relacionados.", exception);
        }
    }

    private void clearForm() {
        selectedDepartamentoId = null;
        idField.setText("");
        nombreField.setText("");
        presupuestoField.setText("0");
        activoCheckBox.setSelected(true);
        if (empresaComboBox.getItemCount() > 0) {
            empresaComboBox.setSelectedIndex(0);
        }
        ViewFeedback.clearInvalid(nombreField, presupuestoField, empresaComboBox);
        departamentoTable.clearSelection();
        nombreField.requestFocusInWindow();
    }

    private void selectEmpresa(Empresa empresa) {
        if (empresa == null || empresa.getId() == null) {
            return;
        }
        for (int index = 0; index < empresaComboBox.getItemCount(); index++) {
            Empresa item = empresaComboBox.getItemAt(index);
            if (empresa.getId().equals(item.getId())) {
                empresaComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void showError(String message, RuntimeException exception) {
        ViewFeedback.showError(this, message, exception);
    }
}
