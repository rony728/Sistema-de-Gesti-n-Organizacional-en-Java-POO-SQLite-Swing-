package edu.university.system.view;

import edu.university.system.controller.CargoController;
import edu.university.system.controller.DepartamentoController;
import edu.university.system.controller.EmpleadoController;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.Permission;
import edu.university.system.controller.ValidationException;
import edu.university.system.model.Cargo;
import edu.university.system.model.Departamento;
import edu.university.system.model.Empleado;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class EmpleadoInternalFrame extends JInternalFrame {

    private final EmpleadoController empleadoController;
    private final CargoController cargoController;
    private final DepartamentoController departamentoController;
    private final EmpleadoTableModel tableModel;
    private final JTable empleadoTable;
    private final JTextField idField;
    private final JTextField codigoField;
    private final JTextField identidadField;
    private final JTextField nombresField;
    private final JTextField apellidosField;
    private final JTextField telefonoField;
    private final JTextField correoField;
    private final JTextField direccionField;
    private final JTextField fechaNacimientoField;
    private final JTextField fechaContratacionField;
    private final JTextField salarioField;
    private final JTextField fotografiaField;
    private final JTextField searchField;
    private final JLabel fotografiaPreviewLabel;
    private final JComboBox<Cargo> cargoComboBox;
    private final JComboBox<Departamento> departamentoComboBox;
    private Long selectedEmpleadoId;

    public EmpleadoInternalFrame(
            EmpleadoController empleadoController,
            CargoController cargoController,
            DepartamentoController departamentoController
    ) {
        super("Modulo Empleados", true, true, true, true);
        this.empleadoController = empleadoController;
        this.cargoController = cargoController;
        this.departamentoController = departamentoController;
        this.tableModel = new EmpleadoTableModel();
        this.empleadoTable = new JTable(tableModel);
        this.idField = new JTextField(8);
        this.codigoField = new JTextField(12);
        this.identidadField = new JTextField(16);
        this.nombresField = new JTextField(22);
        this.apellidosField = new JTextField(22);
        this.telefonoField = new JTextField(14);
        this.correoField = new JTextField(24);
        this.direccionField = new JTextField(30);
        this.fechaNacimientoField = new JTextField(10);
        this.fechaContratacionField = new JTextField(10);
        this.salarioField = new JTextField(12);
        this.fotografiaField = new JTextField(28);
        this.searchField = new JTextField(28);
        this.fotografiaPreviewLabel = new JLabel("Sin fotografia", SwingConstants.CENTER);
        this.cargoComboBox = new JComboBox<>();
        this.departamentoComboBox = new JComboBox<>();
        this.selectedEmpleadoId = null;

        configureFrame();
        configureTable();
        setContentPane(createContent());
        loadCatalogs();
        loadEmpleados();
    }

    private void configureFrame() {
        WindowIconUtils.applyTo(this);
        setSize(1120, 660);
        setMinimumSize(new java.awt.Dimension(980, 560));
    }

    private void configureTable() {
        ViewFeedback.configureTable(empleadoTable);

        TableRowSorter<EmpleadoTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(3, SortOrder.ASCENDING)));
        empleadoTable.setRowSorter(sorter);

        empleadoTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedEmpleado();
            }
        });
    }

    private JPanel createContent() {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        rootPanel.add(createFormPanel(), BorderLayout.NORTH);
        rootPanel.add(createPhotoPanel(), BorderLayout.EAST);
        rootPanel.add(new JScrollPane(empleadoTable), BorderLayout.CENTER);
        rootPanel.add(createActionPanel(), BorderLayout.SOUTH);
        return rootPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));
        idField.setEditable(false);
        idField.setHorizontalAlignment(SwingConstants.RIGHT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        addField(formPanel, c, 0, 0, "ID", idField);
        addField(formPanel, c, 2, 0, "Codigo", codigoField);
        addField(formPanel, c, 4, 0, "Identidad", identidadField);
        addWideField(formPanel, c, 0, 1, "Nombres", nombresField);
        addWideField(formPanel, c, 3, 1, "Apellidos", apellidosField);
        addField(formPanel, c, 0, 2, "Telefono", telefonoField);
        addWideField(formPanel, c, 2, 2, "Correo", correoField);
        addWideField(formPanel, c, 0, 3, "Direccion", direccionField);
        addField(formPanel, c, 0, 4, "Nacimiento", fechaNacimientoField);
        addField(formPanel, c, 2, 4, "Contratacion", fechaContratacionField);
        addField(formPanel, c, 4, 4, "Salario", salarioField);
        addCombo(formPanel, c, 0, 5, "Cargo", cargoComboBox);
        addCombo(formPanel, c, 3, 5, "Departamento", departamentoComboBox);
        addPhotoField(formPanel, c);
        addSearchField(formPanel, c);

        ViewFeedback.addInstantSearch(searchField, this::searchEmpleados);

        return formPanel;
    }

    private JPanel createPhotoPanel() {
        JPanel photoPanel = new JPanel(new BorderLayout(8, 8));
        photoPanel.setBorder(BorderFactory.createTitledBorder("Fotografia"));

        fotografiaPreviewLabel.setBorder(BorderFactory.createEtchedBorder());
        fotografiaPreviewLabel.setPreferredSize(new java.awt.Dimension(170, 210));
        fotografiaPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fotografiaPreviewLabel.setVerticalAlignment(SwingConstants.CENTER);

        JButton selectPhotoButton = new JButton("Seleccionar");
        JButton clearPhotoButton = new JButton("Quitar");
        selectPhotoButton.addActionListener(event -> selectPhoto());
        clearPhotoButton.addActionListener(event -> clearPhoto());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttonPanel.add(selectPhotoButton);
        buttonPanel.add(clearPhotoButton);

        photoPanel.add(fotografiaPreviewLabel, BorderLayout.CENTER);
        photoPanel.add(buttonPanel, BorderLayout.SOUTH);
        return photoPanel;
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

    private void addWideField(JPanel panel, GridBagConstraints c, int x, int y, String label, JTextField field) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = x + 1;
        c.gridwidth = 2;
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
        c.gridwidth = 2;
        c.weightx = 1;
        panel.add(comboBox, c);
    }

    private void addSearchField(JPanel panel, GridBagConstraints c) {
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel("Buscar"), c);
        c.gridx = 1;
        c.gridwidth = 5;
        c.weightx = 1;
        panel.add(searchField, c);
    }

    private void addPhotoField(JPanel panel, GridBagConstraints c) {
        fotografiaField.setEditable(false);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel("Ruta foto"), c);
        c.gridx = 1;
        c.gridwidth = 5;
        c.weightx = 1;
        panel.add(fotografiaField, c);
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
        saveButton.addActionListener(event -> saveEmpleado());
        deleteButton.addActionListener(event -> deleteEmpleado());
        refreshButton.addActionListener(event -> refreshEmpleados());
        exportExcelButton.addActionListener(event -> exportEmpleadosExcel());
        exportPdfButton.addActionListener(event -> exportEmpleadosPdf());

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

    private void loadCatalogs() {
        try {
            cargoComboBox.removeAllItems();
            for (Cargo cargo : cargoController.listar()) {
                cargoComboBox.addItem(cargo);
            }
            departamentoComboBox.removeAllItems();
            for (Departamento departamento : departamentoController.listar()) {
                departamentoComboBox.addItem(departamento);
            }
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los catalogos.", exception);
        }
    }

    private void loadEmpleados() {
        try {
            tableModel.setEmpleados(empleadoController.listar());
            clearForm();
        } catch (RuntimeException exception) {
            showError("No se pudieron cargar los empleados.", exception);
        }
    }

    private void refreshEmpleados() {
        searchField.setText("");
        loadCatalogs();
        loadEmpleados();
    }

    private void searchEmpleados() {
        try {
            tableModel.setEmpleados(empleadoController.buscar(searchField.getText()));
        } catch (RuntimeException exception) {
            showError("No se pudo realizar la busqueda.", exception);
        }
    }

    private void loadSelectedEmpleado() {
        int selectedRow = empleadoTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        Empleado empleado = tableModel.getEmpleadoAt(empleadoTable.convertRowIndexToModel(selectedRow));
        selectedEmpleadoId = empleado.getId();
        idField.setText(String.valueOf(empleado.getId()));
        codigoField.setText(empleado.getCodigoEmpleado());
        identidadField.setText(empleado.getIdentidad());
        nombresField.setText(empleado.getNombres());
        apellidosField.setText(empleado.getApellidos());
        telefonoField.setText(empleado.getTelefono());
        correoField.setText(empleado.getCorreoElectronico());
        direccionField.setText(empleado.getDireccion());
        fechaNacimientoField.setText(empleado.getFechaNacimiento() == null ? "" : empleado.getFechaNacimiento().toString());
        fechaContratacionField.setText(empleado.getFechaContratacion() == null ? "" : empleado.getFechaContratacion().toString());
        salarioField.setText(empleado.getSalario() == null ? "0" : empleado.getSalario().toPlainString());
        fotografiaField.setText(empleado.getRutaFotografia());
        updatePhotoPreview(empleado.getRutaFotografia());
        selectCargo(empleado.getCargo());
        selectDepartamento(empleado.getDepartamento());
    }

    private void saveEmpleado() {
        try {
            if (!validateForm()) {
                return;
            }
            Empleado empleado = buildEmpleadoFromForm();
            if (selectedEmpleadoId == null) {
                empleadoController.insertar(empleado);
                JOptionPane.showMessageDialog(this, "Empleado registrado correctamente.", "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                empleadoController.actualizar(empleado);
                JOptionPane.showMessageDialog(this, "Empleado actualizado correctamente.", "Actualizacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshEmpleados();
        } catch (ValidationException | IllegalArgumentException exception) {
            ViewFeedback.showValidation(this, exception.getMessage());
        } catch (RuntimeException exception) {
            showError("No se pudo guardar el empleado.", exception);
        }
    }

    private boolean validateForm() {
        ViewFeedback.clearInvalid(
                codigoField,
                identidadField,
                nombresField,
                apellidosField,
                correoField,
                fechaNacimientoField,
                fechaContratacionField,
                salarioField,
                cargoComboBox,
                departamentoComboBox
        );
        boolean invalid = false;
        invalid |= ViewFeedback.markBlank(codigoField, "Ingrese el codigo del empleado.");
        invalid |= ViewFeedback.markBlank(identidadField, "Ingrese la identidad.");
        invalid |= ViewFeedback.markBlank(nombresField, "Ingrese los nombres.");
        invalid |= ViewFeedback.markBlank(apellidosField, "Ingrese los apellidos.");
        invalid |= ViewFeedback.markInvalidOptionalDate(fechaNacimientoField, "Fecha de nacimiento");
        invalid |= ViewFeedback.markInvalidRequiredDate(fechaContratacionField, "Fecha de contratacion");
        invalid |= ViewFeedback.markInvalidDecimal(salarioField, "Salario");
        invalid |= ViewFeedback.markEmptyCombo(cargoComboBox, "Seleccione un cargo.");
        invalid |= ViewFeedback.markEmptyCombo(departamentoComboBox, "Seleccione un departamento.");
        if (correoField.getText() != null && !correoField.getText().isBlank() && !correoField.getText().contains("@")) {
            ViewFeedback.markInvalid(correoField, "Ingrese un correo electronico valido.");
            invalid = true;
        }
        if (invalid) {
            ViewFeedback.showValidation(this, "Revise los campos resaltados antes de guardar.");
        }
        return !invalid;
    }

    private Empleado buildEmpleadoFromForm() {
        return new Empleado(
                selectedEmpleadoId,
                identidadField.getText(),
                nombresField.getText(),
                apellidosField.getText(),
                telefonoField.getText(),
                correoField.getText(),
                direccionField.getText(),
                parseOptionalDate(fechaNacimientoField.getText(), "Fecha de nacimiento"),
                codigoField.getText() == null ? null : codigoField.getText().trim().toUpperCase(),
                parseRequiredDate(fechaContratacionField.getText(), "Fecha de contratacion"),
                parseMoney(salarioField.getText(), "Salario"),
                (Cargo) cargoComboBox.getSelectedItem(),
                (Departamento) departamentoComboBox.getSelectedItem(),
                fotografiaField.getText()
        );
    }

    private LocalDate parseOptionalDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseRequiredDate(value, fieldName);
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

    private BigDecimal parseMoney(String value, String fieldName) {
        try {
            if (value == null || value.isBlank()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " debe ser numerico.");
        }
    }

    private void deleteEmpleado() {
        if (selectedEmpleadoId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado para eliminar.", "Seleccion requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this, "Desea eliminar el empleado seleccionado?", "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            empleadoController.eliminar(selectedEmpleadoId);
            JOptionPane.showMessageDialog(this, "Empleado eliminado correctamente.", "Eliminacion exitosa", JOptionPane.INFORMATION_MESSAGE);
            refreshEmpleados();
        } catch (RuntimeException exception) {
            showError("No se pudo eliminar el empleado. Verifique que no tenga asignaciones relacionadas.", exception);
        }
    }

    private void exportEmpleadosExcel() {
        ExportDialogUtils.exportTableToExcel(this, empleadoTable, "Reporte de Empleados", "empleados");
    }

    private void exportEmpleadosPdf() {
        ExportDialogUtils.exportTableToPdf(this, empleadoTable, "Reporte de Empleados", "empleados");
    }

    private void clearForm() {
        selectedEmpleadoId = null;
        idField.setText("");
        codigoField.setText("");
        identidadField.setText("");
        nombresField.setText("");
        apellidosField.setText("");
        telefonoField.setText("");
        correoField.setText("");
        direccionField.setText("");
        fechaNacimientoField.setText("");
        fechaContratacionField.setText("");
        salarioField.setText("0");
        fotografiaField.setText("");
        updatePhotoPreview(null);
        ViewFeedback.clearInvalid(
                codigoField,
                identidadField,
                nombresField,
                apellidosField,
                correoField,
                fechaNacimientoField,
                fechaContratacionField,
                salarioField,
                cargoComboBox,
                departamentoComboBox
        );
        if (cargoComboBox.getItemCount() > 0) {
            cargoComboBox.setSelectedIndex(0);
        }
        if (departamentoComboBox.getItemCount() > 0) {
            departamentoComboBox.setSelectedIndex(0);
        }
        empleadoTable.clearSelection();
        codigoField.requestFocusInWindow();
    }

    private void selectPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar fotografia");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imagenes JPG, PNG, GIF o BMP", "jpg", "jpeg", "png", "gif", "bmp"));
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile == null || !selectedFile.isFile() || !selectedFile.canRead()) {
            JOptionPane.showMessageDialog(this, "Seleccione una imagen valida y legible.", "Fotografia invalida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        fotografiaField.setText(selectedFile.getAbsolutePath());
        updatePhotoPreview(selectedFile.getAbsolutePath());
    }

    private void clearPhoto() {
        fotografiaField.setText("");
        updatePhotoPreview(null);
    }

    private void updatePhotoPreview(String photoPath) {
        if (photoPath == null || photoPath.isBlank()) {
            fotografiaPreviewLabel.setIcon(null);
            fotografiaPreviewLabel.setText("Sin fotografia");
            return;
        }

        File imageFile = new File(photoPath);
        if (!imageFile.isFile() || !imageFile.canRead()) {
            fotografiaPreviewLabel.setIcon(null);
            fotografiaPreviewLabel.setText("No disponible");
            return;
        }

        ImageIcon imageIcon = new ImageIcon(photoPath);
        if (imageIcon.getIconWidth() <= 0 || imageIcon.getIconHeight() <= 0) {
            fotografiaPreviewLabel.setIcon(null);
            fotografiaPreviewLabel.setText("Imagen invalida");
            return;
        }

        Image scaledImage = imageIcon.getImage().getScaledInstance(160, 200, Image.SCALE_SMOOTH);
        fotografiaPreviewLabel.setText("");
        fotografiaPreviewLabel.setIcon(new ImageIcon(scaledImage));
    }

    private void selectCargo(Cargo cargo) {
        if (cargo == null || cargo.getId() == null) {
            return;
        }
        for (int index = 0; index < cargoComboBox.getItemCount(); index++) {
            Cargo item = cargoComboBox.getItemAt(index);
            if (cargo.getId().equals(item.getId())) {
                cargoComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void selectDepartamento(Departamento departamento) {
        if (departamento == null || departamento.getId() == null) {
            return;
        }
        for (int index = 0; index < departamentoComboBox.getItemCount(); index++) {
            Departamento item = departamentoComboBox.getItemAt(index);
            if (departamento.getId().equals(item.getId())) {
                departamentoComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void showError(String message, RuntimeException exception) {
        ViewFeedback.showError(this, message, exception);
    }
}
