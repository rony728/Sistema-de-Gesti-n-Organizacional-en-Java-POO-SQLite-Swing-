package edu.university.system.view;

import edu.university.system.model.Asignacion;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class AsignacionTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Empleado", "Proyecto", "Rol", "Inicio", "Fin", "Horas"};
    private final List<Asignacion> asignaciones;

    AsignacionTableModel() {
        this.asignaciones = new ArrayList<>();
    }

    void setAsignaciones(List<Asignacion> asignaciones) {
        this.asignaciones.clear();
        if (asignaciones != null) {
            this.asignaciones.addAll(asignaciones);
        }
        fireTableDataChanged();
    }

    Asignacion getAsignacionAt(int rowIndex) {
        return asignaciones.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return asignaciones.size();
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
            case 6 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Asignacion asignacion = asignaciones.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> asignacion.getId();
            case 1 -> asignacion.getEmpleado() == null ? "" : asignacion.getEmpleado().toString();
            case 2 -> asignacion.getProyecto() == null ? "" : asignacion.getProyecto().getNombre();
            case 3 -> asignacion.getRol();
            case 4 -> asignacion.getFechaInicio();
            case 5 -> asignacion.getFechaFin();
            case 6 -> asignacion.getHorasAsignadas();
            default -> "";
        };
    }
}
