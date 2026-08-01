package edu.university.system.view;

import edu.university.system.controller.DepartamentoController;
import edu.university.system.controller.PaisController;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Departamento;
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
import javax.swing.ListSelectionModel;
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

public final class DepartamentoInternalFrame extends JInternalFrame {

    private final DepartamentoController departamentoController;
    private final PaisController paisController;
    private final DepartamentoTableModel tableModel;
    private final JTable departamentoTable;
    private final JTextField idField;
    private final JTextField nombreField;
    private final JTextField codigoField;
    private final JTextField searchField;
    private final JComboBox<Pais> paisComboBox;
    private Long selectedDepartamentoId;

    public DepartamentoInternalFrame(DepartamentoController departamentoController, PaisController paisController) {
        super("Modulo Departamentos", true, true, true, true);
        this.departamentoController = departamentoController;
        this.paisController = paisController;
        this.tableModel = new DepartamentoTableModel();
        this.departamentoTable = new JTable(tableModel);
        this.idField = new JTextField(10);
        this.nombreField = new JTextField(26);
        this.codigoField = new JTextField(10);
        this.searchField = new JTextField(28);
        this.paisComboBox = new JComboBox<>();
        this.selectedDepartamentoId = null;

        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadPaises();
        loadDepartamentos();
    }

    private void configureFrame() {
        setSize(860, 540);
        setMinimumSize(new java.awt.Dimension(760, 440));
    }

    private void configureTable() {
        ViewFeedback.configureTable(departamentoTable);
        departamentoTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        departamentoTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        departamentoTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        departamentoTable.getColumnModel().getColumn(3).setPreferredWidth(120);

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

        constraints.gridx = 0;
        constraints.gridy = 0;
        formPanel.add(new JLabel("ID"), constraints);

        constraints.gridx = 1;
        formPanel.add(idField, constraints);

        constraints.gridx = 2;
        formPanel.add(new JLabel("Pais"), constraints);

        constraints.gridx = 3;
        constraints.weightx = 1;
        formPanel.add(paisComboBox, constraints);

        constraints.gridx = 4;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Codigo"), constraints);

        constraints.gridx = 5;
        formPanel.add(codigoField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        formPanel.add(new JLabel("Nombre"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 5;
        constraints.weightx = 1;
        formPanel.add(nombreField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Buscar"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 5;
        constraints.weightx = 1;
        formPanel.add(searchField, constraints);

        ViewFeedback.addInstantSearch(searchField, this::searchDepartamentos);

        return formPanel;
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

    private void loadPaises() {
        try {
            paisComboBox.removeAllItems();
            for (Pais pais : paisController.listar()) {
                paisComboBox.addItem(pais);
            }
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los paises.", exception);
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
        loadPaises();
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
        codigoField.setText(departamento.getCodigo());
        selectPais(departamento.getPais());
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
        } catch (ValidationException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar el departamento.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(nombreField, codigoField, paisComboBox);
        boolean invalid = false;
        invalid |= ViewFeedback.markEmptyCombo(paisComboBox, "Seleccione un pais.");
        invalid |= ViewFeedback.markBlank(nombreField, "Ingrese el nombre del departamento.");
        invalid |= ViewFeedback.markBlank(codigoField, "Ingrese el codigo del departamento.");
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private Departamento buildDepartamentoFromForm() {
        Pais pais = (Pais) paisComboBox.getSelectedItem();
        String codigo = codigoField.getText() == null ? null : codigoField.getText().trim().toUpperCase();
        Departamento departamento = new Departamento(selectedDepartamentoId, nombreField.getText(), codigo, pais);
        if (selectedDepartamentoId != null) {
            departamento.setId(selectedDepartamentoId);
        }
        return departamento;
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
            showError("No se pudo eliminar el departamento. Verifique que no tenga registros relacionados.", exception);
        }
    }

    private void clearForm() {
        selectedDepartamentoId = null;
        idField.setText("");
        nombreField.setText("");
        codigoField.setText("");
        if (paisComboBox.getItemCount() > 0) {
            paisComboBox.setSelectedIndex(0);
        }
        ViewFeedback.clearInvalid(nombreField, codigoField, paisComboBox);
        departamentoTable.clearSelection();
        nombreField.requestFocusInWindow();
    }

    private void selectPais(Pais pais) {
        if (pais == null || pais.getId() == null) {
            return;
        }
        for (int index = 0; index < paisComboBox.getItemCount(); index++) {
            Pais item = paisComboBox.getItemAt(index);
            if (pais.getId().equals(item.getId())) {
                paisComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void showError(String message, RuntimeException exception) {
        ViewFeedback.showError(this, message, exception);
    }
}
