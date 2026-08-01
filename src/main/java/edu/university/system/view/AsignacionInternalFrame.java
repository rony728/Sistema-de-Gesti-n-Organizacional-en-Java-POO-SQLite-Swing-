package edu.university.system.view;

import edu.university.system.controller.AsignacionController;
import edu.university.system.controller.EmpleadoController;
import edu.university.system.controller.ProyectoController;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Asignacion;
import edu.university.system.model.Empleado;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class AsignacionInternalFrame extends JInternalFrame {

    private final AsignacionController asignacionController;
    private final EmpleadoController empleadoController;
    private final ProyectoController proyectoController;
    private final AsignacionTableModel tableModel;
    private final JTable asignacionTable;
    private final JTextField idField;
    private final JTextField rolField;
    private final JTextField fechaInicioField;
    private final JTextField fechaFinField;
    private final JTextField horasField;
    private final JTextField searchField;
    private final JComboBox<Empleado> empleadoComboBox;
    private final JComboBox<Proyecto> proyectoComboBox;
    private Long selectedAsignacionId;

    public AsignacionInternalFrame(
            AsignacionController asignacionController,
            EmpleadoController empleadoController,
            ProyectoController proyectoController
    ) {
        super("Modulo Asignaciones", true, true, true, true);
        this.asignacionController = asignacionController;
        this.empleadoController = empleadoController;
        this.proyectoController = proyectoController;
        this.tableModel = new AsignacionTableModel();
        this.asignacionTable = new JTable(tableModel);
        this.idField = new JTextField(8);
        this.rolField = new JTextField(18);
        this.fechaInicioField = new JTextField(10);
        this.fechaFinField = new JTextField(10);
        this.horasField = new JTextField(8);
        this.searchField = new JTextField(28);
        this.empleadoComboBox = new JComboBox<>();
        this.proyectoComboBox = new JComboBox<>();
        this.selectedAsignacionId = null;

        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadCatalogs();
        loadAsignaciones();
    }

    private void configureFrame() {
        WindowIconUtils.applyTo(this);
        setSize(980, 580);
        setMinimumSize(new java.awt.Dimension(860, 480));
    }

    private void configureTable() {
        ViewFeedback.configureTable(asignacionTable);

        TableRowSorter<AsignacionTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(4, SortOrder.DESCENDING)));
        asignacionTable.setRowSorter(sorter);

        asignacionTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedAsignacion();
            }
        });
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        rootPanel.add(createFormPanel(), BorderLayout.NORTH);
        rootPanel.add(new JScrollPane(asignacionTable), BorderLayout.CENTER);
        rootPanel.add(createActionPanel(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos de la asignacion"));
        idField.setEditable(false);
        idField.setHorizontalAlignment(SwingConstants.RIGHT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        addField(formPanel, c, 0, 0, "ID", idField);
        addCombo(formPanel, c, 2, 0, "Empleado", empleadoComboBox);
        addCombo(formPanel, c, 0, 1, "Proyecto", proyectoComboBox);
        addField(formPanel, c, 0, 2, "Rol", rolField);
        addField(formPanel, c, 2, 2, "Inicio", fechaInicioField);
        addField(formPanel, c, 4, 2, "Fin", fechaFinField);
        addField(formPanel, c, 0, 3, "Horas", horasField);

        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        formPanel.add(new JLabel("Buscar"), c);
        c.gridx = 3;
        c.gridwidth = 3;
        c.weightx = 1;
        formPanel.add(searchField, c);

        ViewFeedback.addInstantSearch(searchField, this::searchAsignaciones);

        return formPanel;
    }

    private void addField(JPanel panel, GridBagConstraints c, int x, int y, String label, JTextField field) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = x + 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private void addCombo(JPanel panel, GridBagConstraints c, int x, int y, String label, JComboBox<?> comboBox) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = x + 1;
        c.gridwidth = 3;
        c.weightx = 1;
        panel.add(comboBox, c);
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton newButton = new JButton("Nuevo");
        JButton saveButton = new JButton("Guardar");
        JButton deleteButton = new JButton("Eliminar");
        JButton refreshButton = new JButton("Refrescar");

        newButton.addActionListener(event -> clearForm());
        saveButton.addActionListener(event -> saveAsignacion());
        deleteButton.addActionListener(event -> deleteAsignacion());
        refreshButton.addActionListener(event -> refreshAsignaciones());

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

    private void loadCatalogs() {
        try {
            empleadoComboBox.removeAllItems();
            for (Empleado empleado : empleadoController.listar()) {
                empleadoComboBox.addItem(empleado);
            }
            proyectoComboBox.removeAllItems();
            for (Proyecto proyecto : proyectoController.listar()) {
                proyectoComboBox.addItem(proyecto);
            }
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los catalogos.", exception);
        }
    }

    private void loadAsignaciones() {
        try {
            tableModel.setAsignaciones(asignacionController.listar());
            clearForm();
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar las asignaciones.", exception);
        }
    }

    private void refreshAsignaciones() {
        searchField.setText("");
        loadCatalogs();
        loadAsignaciones();
    }

    private void searchAsignaciones() {
        try {
            tableModel.setAsignaciones(asignacionController.buscar(searchField.getText()));
        } catch (RuntimeException exception) {
            showError("No se pudo realizar la busqueda.", exception);
        }
    }

    private void loadSelectedAsignacion() {
        int selectedRow = asignacionTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        Asignacion asignacion = tableModel.getAsignacionAt(asignacionTable.convertRowIndexToModel(selectedRow));
        selectedAsignacionId = asignacion.getId();
        idField.setText(String.valueOf(asignacion.getId()));
        rolField.setText(asignacion.getRol());
        fechaInicioField.setText(asignacion.getFechaInicio() == null ? "" : asignacion.getFechaInicio().toString());
        fechaFinField.setText(asignacion.getFechaFin() == null ? "" : asignacion.getFechaFin().toString());
        horasField.setText(String.valueOf(asignacion.getHorasAsignadas()));
        selectEmpleado(asignacion.getEmpleado());
        selectProyecto(asignacion.getProyecto());
    }

    private void saveAsignacion() {
        try {
            if (!validateForm()) {
                return;
            }
            Asignacion asignacion = buildAsignacionFromForm();
            if (selectedAsignacionId == null) {
                asignacionController.insertar(asignacion);
                JOptionPane.showMessageDialog(this, "Asignacion registrada correctamente.", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                asignacionController.actualizar(asignacion);
                JOptionPane.showMessageDialog(this, "Asignacion actualizada correctamente.", "Actualizacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshAsignaciones();
        } catch (ValidationException | IllegalArgumentException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar la asignacion.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(empleadoComboBox, proyectoComboBox, rolField, fechaInicioField, fechaFinField, horasField);
        boolean invalid = false;
        invalid |= ViewFeedback.markEmptyCombo(empleadoComboBox, "Seleccione un empleado.");
        invalid |= ViewFeedback.markEmptyCombo(proyectoComboBox, "Seleccione un proyecto.");
        invalid |= ViewFeedback.markBlank(rolField, "Ingrese el rol de la asignacion.");
        invalid |= ViewFeedback.markInvalidRequiredDate(fechaInicioField, "Fecha de inicio");
        invalid |= ViewFeedback.markInvalidOptionalDate(fechaFinField, "Fecha de fin");
        invalid |= ViewFeedback.markInvalidInteger(horasField, "Horas asignadas");
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private Asignacion buildAsignacionFromForm() {
        String fechaFinText = fechaFinField.getText() == null ? "" : fechaFinField.getText().trim();
        return new Asignacion(
                selectedAsignacionId,
                (Empleado) empleadoComboBox.getSelectedItem(),
                (Proyecto) proyectoComboBox.getSelectedItem(),
                rolField.getText(),
                parseRequiredDate(fechaInicioField.getText(), "Fecha de inicio"),
                fechaFinText.isBlank() ? null : parseRequiredDate(fechaFinText, "Fecha de fin"),
                parseHours(horasField.getText())
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

    private int parseHours(String value) {
        try {
            if (value == null || value.isBlank()) {
                return 0;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Horas asignadas debe ser un numero entero.");
        }
    }

    private void deleteAsignacion() {
        if (selectedAsignacionId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una asignacion para eliminar.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this, "Desea eliminar la asignacion seleccionada?", "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            asignacionController.eliminar(selectedAsignacionId);
            JOptionPane.showMessageDialog(this, "Asignacion eliminada correctamente.", "Eliminacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshAsignaciones();
        } catch (RuntimeException exception) {
            showError("No se pudo eliminar la asignacion.", exception);
        }
    }

    private void clearForm() {
        selectedAsignacionId = null;
        idField.setText("");
        rolField.setText("");
        fechaInicioField.setText("");
        fechaFinField.setText("");
        horasField.setText("0");
        if (empleadoComboBox.getItemCount() > 0) {
            empleadoComboBox.setSelectedIndex(0);
        }
        if (proyectoComboBox.getItemCount() > 0) {
            proyectoComboBox.setSelectedIndex(0);
        }
        ViewFeedback.clearInvalid(empleadoComboBox, proyectoComboBox, rolField, fechaInicioField, fechaFinField, horasField);
        asignacionTable.clearSelection();
        rolField.requestFocusInWindow();
    }

    private void selectEmpleado(Empleado empleado) {
        if (empleado == null || empleado.getId() == null) {
            return;
        }
        for (int index = 0; index < empleadoComboBox.getItemCount(); index++) {
            Empleado item = empleadoComboBox.getItemAt(index);
            if (empleado.getId().equals(item.getId())) {
                empleadoComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void selectProyecto(Proyecto proyecto) {
        if (proyecto == null || proyecto.getId() == null) {
            return;
        }
        for (int index = 0; index < proyectoComboBox.getItemCount(); index++) {
            Proyecto item = proyectoComboBox.getItemAt(index);
            if (proyecto.getId().equals(item.getId())) {
                proyectoComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void showError(String message, RuntimeException exception) {
        ViewFeedback.showError(this, message, exception);
    }
}
