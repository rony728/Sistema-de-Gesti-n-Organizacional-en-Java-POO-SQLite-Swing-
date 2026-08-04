package edu.university.system.view;

import edu.university.system.model.Cargo;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class CargoTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Nombre", "Descripcion", "Salario base"};
    private final List<Cargo> cargos;

    CargoTableModel() {
        this.cargos = new ArrayList<>();
    }

    void setCargos(List<Cargo> cargos) {
        this.cargos.clear();
        if (cargos != null) {
            this.cargos.addAll(cargos);
        }
        fireTableDataChanged();
    }

    Cargo getCargoAt(int rowIndex) {
        return cargos.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return cargos.size();
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
            case 3 -> BigDecimal.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Cargo cargo = cargos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> cargo.getId();
            case 1 -> cargo.getNombre();
            case 2 -> cargo.getDescripcion();
            case 3 -> cargo.getSalarioBase();
            default -> "";
        };
    }
}
