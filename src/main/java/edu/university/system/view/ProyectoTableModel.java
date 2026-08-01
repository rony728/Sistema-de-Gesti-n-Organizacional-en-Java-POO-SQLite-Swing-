package edu.university.system.view;

import edu.university.system.model.Proyecto;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class ProyectoTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Codigo", "Nombre", "Empresa", "Inicio", "Fin", "Presupuesto"};
    private final List<Proyecto> proyectos;

    ProyectoTableModel() {
        this.proyectos = new ArrayList<>();
    }

    void setProyectos(List<Proyecto> proyectos) {
        this.proyectos.clear();
        if (proyectos != null) {
            this.proyectos.addAll(proyectos);
        }
        fireTableDataChanged();
    }

    Proyecto getProyectoAt(int rowIndex) {
        return proyectos.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return proyectos.size();
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
            case 6 -> java.math.BigDecimal.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Proyecto proyecto = proyectos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> proyecto.getId();
            case 1 -> proyecto.getCodigo();
            case 2 -> proyecto.getNombre();
            case 3 -> proyecto.getEmpresa() == null ? "" : proyecto.getEmpresa().getNombre();
            case 4 -> proyecto.getFechaInicio();
            case 5 -> proyecto.getFechaFin();
            case 6 -> proyecto.getPresupuesto();
            default -> "";
        };
    }
}
