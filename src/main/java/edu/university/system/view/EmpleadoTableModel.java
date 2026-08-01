package edu.university.system.view;

import edu.university.system.model.Empleado;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class EmpleadoTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Codigo", "Identidad", "Nombre", "Cargo", "Departamento", "Contratacion", "Salario"};
    private final List<Empleado> empleados;

    EmpleadoTableModel() {
        this.empleados = new ArrayList<>();
    }

    void setEmpleados(List<Empleado> empleados) {
        this.empleados.clear();
        if (empleados != null) {
            this.empleados.addAll(empleados);
        }
        fireTableDataChanged();
    }

    Empleado getEmpleadoAt(int rowIndex) {
        return empleados.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return empleados.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Long.class;
            case 7 -> java.math.BigDecimal.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Empleado empleado = empleados.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> empleado.getId();
            case 1 -> empleado.getCodigoEmpleado();
            case 2 -> empleado.getIdentidad();
            case 3 -> empleado.getNombreCompleto();
            case 4 -> empleado.getCargo() == null ? "" : empleado.getCargo().getNombre();
            case 5 -> empleado.getDepartamento() == null ? "" : empleado.getDepartamento().getNombre();
            case 6 -> empleado.getFechaContratacion();
            case 7 -> empleado.getSalario();
            default -> "";
        };
    }
}
