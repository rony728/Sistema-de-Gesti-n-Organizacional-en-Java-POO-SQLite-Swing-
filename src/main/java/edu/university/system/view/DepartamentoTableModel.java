package edu.university.system.view;

import edu.university.system.model.Departamento;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class DepartamentoTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Pais", "Nombre", "Codigo"};
    private final List<Departamento> departamentos;

    DepartamentoTableModel() {
        this.departamentos = new ArrayList<>();
    }

    void setDepartamentos(List<Departamento> departamentos) {
        this.departamentos.clear();
        if (departamentos != null) {
            this.departamentos.addAll(departamentos);
        }
        fireTableDataChanged();
    }

    Departamento getDepartamentoAt(int rowIndex) {
        return departamentos.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return departamentos.size();
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
        return columnIndex == 0 ? Long.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Departamento departamento = departamentos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> departamento.getId();
            case 1 -> departamento.getPais() == null ? "" : departamento.getPais().getNombre();
            case 2 -> departamento.getNombre();
            case 3 -> departamento.getCodigo();
            default -> "";
        };
    }
}
