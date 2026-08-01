package edu.university.system.view;

import edu.university.system.model.Pais;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class PaisTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Nombre", "Codigo ISO"};
    private final List<Pais> paises;

    PaisTableModel() {
        this.paises = new ArrayList<>();
    }

    void setPaises(List<Pais> paises) {
        this.paises.clear();
        if (paises != null) {
            this.paises.addAll(paises);
        }
        fireTableDataChanged();
    }

    Pais getPaisAt(int rowIndex) {
        return paises.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return paises.size();
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
        Pais pais = paises.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> pais.getId();
            case 1 -> pais.getNombre();
            case 2 -> pais.getCodigoIso();
            default -> "";
        };
    }
}
