package edu.university.system.view;

import edu.university.system.controller.EmpresaController;
import edu.university.system.controller.ProyectoController;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Empresa;
import edu.university.system.model.Proyecto;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class ProyectoInternalFrame extends JInternalFrame {

    private final ProyectoController proyectoController;
    private final EmpresaController empresaController;
    private final ProyectoTableModel tableModel;
    private final JTable proyectoTable;
    private final JTextField idField;
    private final JTextField codigoField;
    private final JTextField nombreField;
    private final JTextField descripcionField;
    private final JTextField fechaInicioField;
    private final JTextField fechaFinField;
    private final JTextField presupuestoField;
    private final JTextField searchField;
    private final JComboBox<Empresa> empresaComboBox;
    private Long selectedProyectoId;

    public ProyectoInternalFrame(ProyectoController proyectoController, EmpresaController empresaController) {
        super("Modulo Proyectos", true, true, true, true);
        this.proyectoController = proyectoController;
        this.empresaController = empresaController;
        this.tableModel = new ProyectoTableModel();
        this.proyectoTable = new JTable(tableModel);
        this.idField = new JTextField(8);
        this.codigoField = new JTextField(12);
        this.nombreField = new JTextField(28);
        this.descripcionField = new JTextField(36);
        this.fechaInicioField = new JTextField(10);
        this.fechaFinField = new JTextField(10);
        this.presupuestoField = new JTextField(12);
        this.searchField = new JTextField(28);
        this.empresaComboBox = new JComboBox<>();
        this.selectedProyectoId = null;

        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadEmpresas();
        loadProyectos();
    }

    private void configureFrame() {
        WindowIconUtils.applyTo(this);
        setSize(980, 600);
        setMinimumSize(new java.awt.Dimension(880, 500));
    }

    private void configureTable() {
        ViewFeedback.configureTable(proyectoTable);
        proyectoTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        proyectoTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        proyectoTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        proyectoTable.getColumnModel().getColumn(3).setPreferredWidth(220);
        proyectoTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        proyectoTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        proyectoTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        TableRowSorter<ProyectoTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
        proyectoTable.setRowSorter(sorter);

        proyectoTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedProyecto();
            }
        });
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        rootPanel.add(createFormPanel(), BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(proyectoTable), BorderLayout.CENTER);
        rootPanel.add(createActionPanel(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del proyecto"));

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
        formPanel.add(new JLabel("Empresa"), constraints);

        constraints.gridx = 3;
        constraints.gridwidth = 3;
        constraints.weightx = 1;
        formPanel.add(empresaComboBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Codigo"), constraints);

        constraints.gridx = 1;
        formPanel.add(codigoField, constraints);

        constraints.gridx = 2;
        formPanel.add(new JLabel("Nombre"), constraints);

        constraints.gridx = 3;
        constraints.gridwidth = 3;
        constraints.weightx = 1;
        formPanel.add(nombreField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Inicio"), constraints);

        constraints.gridx = 1;
        formPanel.add(fechaInicioField, constraints);

        constraints.gridx = 2;
        formPanel.add(new JLabel("Fin"), constraints);

        constraints.gridx = 3;
        formPanel.add(fechaFinField, constraints);

        constraints.gridx = 4;
        formPanel.add(new JLabel("Presupuesto"), constraints);

        constraints.gridx = 5;
        formPanel.add(presupuestoField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        formPanel.add(new JLabel("Descripcion"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 5;
        constraints.weightx = 1;
        formPanel.add(descripcionField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Buscar"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 5;
        constraints.weightx = 1;
        formPanel.add(searchField, constraints);

        ViewFeedback.addInstantSearch(searchField, this::searchProyectos);

        return formPanel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton newButton = new JButton("Nuevo");
        JButton saveButton = new JButton("Guardar");
        JButton deleteButton = new JButton("Eliminar");
        JButton refreshButton = new JButton("Refrescar");
        JButton exportExcelButton = new JButton("Exportar Excel");
        JButton exportPdfButton = new JButton("Exportar PDF");

        newButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> saveProyecto());
        deleteButton.addActionListener(event -> deleteProyecto());
        refreshButton.addActionListener(event -> refreshProyectos());
        exportExcelButton.addActionListener(event -> exportProyectosExcel());
        exportPdfButton.addActionListener(event -> exportProyectosPdf());

        boolean canModify = AuthorizationService.can(Permission.CREATE);
        newButton.setEnabled(canModify);
        saveButton.setEnabled(canModify);
        deleteButton.setEnabled(AuthorizationService.can(Permission.DELETE));
        exportExcelButton.setEnabled(AuthorizationService.can(Permission.EXPORT));
        exportPdfButton.setEnabled(AuthorizationService.can(Permission.EXPORT));

        actionPanel.add(newButton);
        actionPanel.add(saveButton);
        actionPanel.add(deleteButton);
        actionPanel.add(refreshButton);
        actionPanel.add(exportExcelButton);
        actionPanel.add(exportPdfButton);
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

    private void loadProyectos() {
        try {
            tableModel.setProyectos(proyectoController.listar());
            clearForm();
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los proyectos.", exception);
        }
    }

    private void refreshProyectos() {
        searchField.setText("");
        loadEmpresas();
        loadProyectos();
    }

    private void searchProyectos() {
        try {
            tableModel.setProyectos(proyectoController.buscar(searchField.getText()));
        } catch (RuntimeException exception) {
            showError("No se pudo realizar la busqueda.", exception);
        }
    }

    private void loadSelectedProyecto() {
        int selectedRow = proyectoTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int modelRow = proyectoTable.convertRowIndexToModel(selectedRow);
        Proyecto proyecto = tableModel.getProyectoAt(modelRow);
        selectedProyectoId = proyecto.getId();
        idField.setText(String.valueOf(proyecto.getId()));
        codigoField.setText(proyecto.getCodigo());
        nombreField.setText(proyecto.getNombre());
        descripcionField.setText(proyecto.getDescripcion());
        fechaInicioField.setText(proyecto.getFechaInicio() == null ? "" : proyecto.getFechaInicio().toString());
        fechaFinField.setText(proyecto.getFechaFin() == null ? "" : proyecto.getFechaFin().toString());
        presupuestoField.setText(proyecto.getPresupuesto() == null ? "0" : proyecto.getPresupuesto().toPlainString());
        selectEmpresa(proyecto.getEmpresa());
    }

    private void saveProyecto() {
        try {
            if (!validateForm()) {
                return;
            }
            Proyecto proyecto = buildProyectoFromForm();
            if (selectedProyectoId == null) {
                proyectoController.insertar(proyecto);
                JOptionPane.showMessageDialog(this, "Proyecto registrado correctamente.", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                proyectoController.actualizar(proyecto);
                JOptionPane.showMessageDialog(this, "Proyecto actualizado correctamente.", "Actualizacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshProyectos();
        } catch (ValidationException | IllegalArgumentException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar el proyecto.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(empresaComboBox, codigoField, nombreField, fechaInicioField, fechaFinField, presupuestoField);
        boolean invalid = false;
        invalid |= ViewFeedback.markEmptyCombo(empresaComboBox, "Seleccione una empresa.");
        invalid |= ViewFeedback.markBlank(codigoField, "Ingrese el codigo del proyecto.");
        invalid |= ViewFeedback.markBlank(nombreField, "Ingrese el nombre del proyecto.");
        invalid |= ViewFeedback.markInvalidRequiredDate(fechaInicioField, "Fecha de inicio");
        invalid |= ViewFeedback.markInvalidOptionalDate(fechaFinField, "Fecha de fin");
        invalid |= ViewFeedback.markInvalidDecimal(presupuestoField, "Presupuesto");
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private Proyecto buildProyectoFromForm() {
        Empresa empresa = (Empresa) empresaComboBox.getSelectedItem();
        String fechaFinText = fechaFinField.getText() == null ? "" : fechaFinField.getText().trim();
        return new Proyecto(
                selectedProyectoId,
                codigoField.getText() == null ? null : codigoField.getText().trim().toUpperCase(),
                nombreField.getText(),
                descripcionField.getText(),
                parseRequiredDate(fechaInicioField.getText(), "Fecha de inicio"),
                fechaFinText.isBlank() ? null : parseRequiredDate(fechaFinText, "Fecha de fin"),
                parseBudget(presupuestoField.getText()),
                empresa
        );
    }

    private LocalDate parseRequiredDate(String value, String fieldName) {
        try {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(fieldName + " es requerida.");
            }
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(fieldName + " debe tener formato yyyy-MM-dd.");
        }
    }

    private BigDecimal parseBudget(String value) {
        try {
            if (value == null || value.isBlank()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Presupuesto debe ser numerico.");
        }
    }

    private void deleteProyecto() {
        if (selectedProyectoId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un proyecto para eliminar.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Desea eliminar el proyecto seleccionado?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            proyectoController.eliminar(selectedProyectoId);
            JOptionPane.showMessageDialog(this, "Proyecto eliminado correctamente.", "Eliminacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshProyectos();
        } catch (RuntimeException exception) {
            showError("No se pudo eliminar el proyecto. Verifique que no tenga asignaciones relacionadas.", exception);
        }
    }

    private void exportProyectosExcel() {
        ExportDialogUtils.exportTableToExcel(this, proyectoTable, "Reporte de Proyectos", "proyectos");
    }

    private void exportProyectosPdf() {
        ExportDialogUtils.exportTableToPdf(this, proyectoTable, "Reporte de Proyectos", "proyectos");
    }

    private void clearForm() {
        selectedProyectoId = null;
        idField.setText("");
        codigoField.setText("");
        nombreField.setText("");
        descripcionField.setText("");
        fechaInicioField.setText("");
        fechaFinField.setText("");
        presupuestoField.setText("0");
        if (empresaComboBox.getItemCount() > 0) {
            empresaComboBox.setSelectedIndex(0);
        }
        ViewFeedback.clearInvalid(empresaComboBox, codigoField, nombreField, fechaInicioField, fechaFinField, presupuestoField);
        proyectoTable.clearSelection();
        codigoField.requestFocusInWindow();
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
