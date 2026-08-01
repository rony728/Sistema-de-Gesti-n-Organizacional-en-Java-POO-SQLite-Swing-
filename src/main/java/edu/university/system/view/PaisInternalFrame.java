package edu.university.system.view;

import edu.university.system.controller.PaisController;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Pais;

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

public final class PaisInternalFrame extends JInternalFrame {

    private final PaisController paisController;
    private final PaisTableModel tableModel;
    private final JTable paisTable;
    private final JTextField idField;
    private final JTextField nombreField;
    private final JTextField codigoIsoField;
    private final JTextField searchField;
    private Long selectedPaisId;

    public PaisInternalFrame(PaisController paisController) {
        super("Modulo Paises", true, true, true, true);
        this.paisController = paisController;
        this.tableModel = new PaisTableModel();
        this.paisTable = new JTable(tableModel);
        this.idField = new JTextField(10);
        this.nombreField = new JTextField(26);
        this.codigoIsoField = new JTextField(10);
        this.searchField = new JTextField(28);

        this.selectedPaisId = null;
        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadPaises();
    }

    private void configureFrame() {
        setSize(760, 500);
        setMinimumSize(new java.awt.Dimension(680, 420));
    }

    private void configureTable() {
        ViewFeedback.configureTable(paisTable);
        paisTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        paisTable.getColumnModel().getColumn(1).setPreferredWidth(360);
        paisTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        TableRowSorter<PaisTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        paisTable.setRowSorter(sorter);

        paisTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedPais();
            }
        });
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        rootPanel.add(createFormPanel(), BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(paisTable), BorderLayout.CENTER);
        rootPanel.add(createActionPanel(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del pais"));

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

        constraints.gridx = 4;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Codigo ISO"), constraints);

        constraints.gridx = 5;
        formPanel.add(codigoIsoField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        formPanel.add(new JLabel("Buscar"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 5;
        constraints.weightx = 1;
        formPanel.add(searchField, constraints);

        ViewFeedback.addInstantSearch(searchField, this::searchPaises);

        return formPanel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton newButton = new JButton("Nuevo");
        JButton saveButton = new JButton("Guardar");
        JButton deleteButton = new JButton("Eliminar");
        JButton refreshButton = new JButton("Refrescar");

        newButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> savePais());
        deleteButton.addActionListener(event -> deletePais());
        refreshButton.addActionListener(event -> refreshPaises());

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
            tableModel.setPaises(paisController.listar());
            clearForm();
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los paises.", exception);
        }
    }

    private void refreshPaises() {
        searchField.setText("");
        loadPaises();
    }

    private void searchPaises() {
        try {
            tableModel.setPaises(paisController.buscar(searchField.getText()));
        } catch (RuntimeException exception) {
            showError("No se pudo realizar la busqueda.", exception);
        }
    }

    private void loadSelectedPais() {
        int selectedRow = paisTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int modelRow = paisTable.convertRowIndexToModel(selectedRow);
        Pais pais = tableModel.getPaisAt(modelRow);
        selectedPaisId = pais.getId();
        idField.setText(String.valueOf(pais.getId()));
        nombreField.setText(pais.getNombre());
        codigoIsoField.setText(pais.getCodigoIso());
    }

    private void savePais() {
        try {
            if (!validateForm()) {
                return;
            }
            Pais pais = buildPaisFromForm();
            if (selectedPaisId == null) {
                paisController.insertar(pais);
                JOptionPane.showMessageDialog(this, "Pais registrado correctamente.", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                paisController.actualizar(pais);
                JOptionPane.showMessageDialog(this, "Pais actualizado correctamente.", "Actualizacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshPaises();
        } catch (ValidationException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar el pais.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(nombreField, codigoIsoField);
        boolean invalid = false;
        invalid |= ViewFeedback.markBlank(nombreField, "Ingrese el nombre del pais.");
        invalid |= ViewFeedback.markBlank(codigoIsoField, "Ingrese el codigo ISO.");
        if (codigoIsoField.getText() != null && codigoIsoField.getText().trim().length() > 3) {
            ViewFeedback.markInvalid(codigoIsoField, "El codigo ISO debe tener maximo 3 caracteres.");
            invalid = true;
        }
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private Pais buildPaisFromForm() {
        String codigoIso = codigoIsoField.getText() == null ? null : codigoIsoField.getText().trim().toUpperCase();
        Pais pais = new Pais(selectedPaisId, nombreField.getText(), codigoIso);
        if (selectedPaisId != null) {
            pais.setId(selectedPaisId);
        }
        return pais;
    }

    private void deletePais() {
        if (selectedPaisId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un pais para eliminar.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Desea eliminar el pais seleccionado?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            paisController.eliminar(selectedPaisId);
            JOptionPane.showMessageDialog(this, "Pais eliminado correctamente.", "Eliminacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshPaises();
        } catch (RuntimeException exception) {
            showError("No se pudo eliminar el pais. Verifique que no tenga registros relacionados.", exception);
        }
    }

    private void clearForm() {
        selectedPaisId = null;
        idField.setText("");
        nombreField.setText("");
        codigoIsoField.setText("");
        ViewFeedback.clearInvalid(nombreField, codigoIsoField);
        paisTable.clearSelection();
        nombreField.requestFocusInWindow();
    }

    private void showError(String message, RuntimeException exception) {
        ViewFeedback.showError(this, message, exception);
    }
}
