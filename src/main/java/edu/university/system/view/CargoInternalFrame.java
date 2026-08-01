package edu.university.system.view;

import edu.university.system.controller.CargoController;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Cargo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

public final class CargoInternalFrame extends JInternalFrame {

    private final CargoController cargoController;
    private final CargoTableModel tableModel;
    private final JTable cargoTable;
    private final JTextField idField;
    private final JTextField nombreField;
    private final JTextField descripcionField;
    private final JTextField searchField;
    private Long selectedCargoId;

    public CargoInternalFrame(CargoController cargoController) {
        super("Modulo Cargos", true, true, true, true);
        this.cargoController = cargoController;
        this.tableModel = new CargoTableModel();
        this.cargoTable = new JTable(tableModel);
        this.idField = new JTextField(10);
        this.nombreField = new JTextField(26);
        this.descripcionField = new JTextField(36);
        this.searchField = new JTextField(28);
        this.selectedCargoId = null;

        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadCargos();
    }

    private void configureFrame() {
        WindowIconUtils.applyTo(this);
        setSize(760, 500);
        setMinimumSize(new java.awt.Dimension(680, 420));
    }

    private void configureTable() {
        ViewFeedback.configureTable(cargoTable);
        cargoTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        cargoTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        cargoTable.getColumnModel().getColumn(2).setPreferredWidth(390);

        TableRowSorter<CargoTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        cargoTable.setRowSorter(sorter);

        cargoTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedCargo();
            }
        });
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        rootPanel.add(createFormPanel(), BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(cargoTable), BorderLayout.CENTER);
        rootPanel.add(createActionPanel(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del cargo"));

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
        formPanel.add(new JLabel("Nombre"), constraints);

        constraints.gridx = 3;
        constraints.weightx = 1;
        formPanel.add(nombreField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Descripcion"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.weightx = 1;
        formPanel.add(descripcionField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Buscar"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 3;
        constraints.weightx = 1;
        formPanel.add(searchField, constraints);

        ViewFeedback.addInstantSearch(searchField, this::searchCargos);

        return formPanel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton newButton = new JButton("Nuevo");
        JButton saveButton = new JButton("Guardar");
        JButton deleteButton = new JButton("Eliminar");
        JButton refreshButton = new JButton("Refrescar");

        newButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> saveCargo());
        deleteButton.addActionListener(event -> deleteCargo());
        refreshButton.addActionListener(event -> refreshCargos());

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

    private void loadCargos() {
        try {
            tableModel.setCargos(cargoController.listar());
            clearForm();
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los cargos.", exception);
        }
    }

    private void refreshCargos() {
        searchField.setText("");
        loadCargos();
    }

    private void searchCargos() {
        try {
            tableModel.setCargos(cargoController.buscar(searchField.getText()));
        } catch (RuntimeException exception) {
            showError("No se pudo realizar la busqueda.", exception);
        }
    }

    private void loadSelectedCargo() {
        int selectedRow = cargoTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        int modelRow = cargoTable.convertRowIndexToModel(selectedRow);
        Cargo cargo = tableModel.getCargoAt(modelRow);
        selectedCargoId = cargo.getId();
        idField.setText(String.valueOf(cargo.getId()));
        nombreField.setText(cargo.getNombre());
        descripcionField.setText(cargo.getDescripcion());
    }

    private void saveCargo() {
        try {
            if (!validateForm()) {
                return;
            }
            Cargo cargo = new Cargo(selectedCargoId, nombreField.getText(), descripcionField.getText());
            if (selectedCargoId == null) {
                cargoController.insertar(cargo);
                JOptionPane.showMessageDialog(this, "Cargo registrado correctamente.", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                cargoController.actualizar(cargo);
                JOptionPane.showMessageDialog(this, "Cargo actualizado correctamente.", "Actualizacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshCargos();
        } catch (ValidationException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar el cargo.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(nombreField);
        boolean invalid = ViewFeedback.markBlank(nombreField, "Ingrese el nombre del cargo.");
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private void deleteCargo() {
        if (selectedCargoId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cargo para eliminar.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Desea eliminar el cargo seleccionado?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            cargoController.eliminar(selectedCargoId);
            JOptionPane.showMessageDialog(this, "Cargo eliminado correctamente.", "Eliminacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshCargos();
        } catch (RuntimeException exception) {
            showError("No se pudo eliminar el cargo. Verifique que no tenga empleados relacionados.", exception);
        }
    }

    private void clearForm() {
        selectedCargoId = null;
        idField.setText("");
        nombreField.setText("");
        descripcionField.setText("");
        ViewFeedback.clearInvalid(nombreField);
        cargoTable.clearSelection();
        nombreField.requestFocusInWindow();
    }

    private void showError(String message, RuntimeException exception) {
        ViewFeedback.showError(this, message, exception);
    }
}
