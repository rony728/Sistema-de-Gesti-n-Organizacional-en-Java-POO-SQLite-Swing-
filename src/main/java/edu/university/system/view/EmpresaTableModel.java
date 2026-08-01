package edu.university.system.view;

import edu.university.system.model.Empresa;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class EmpresaTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Nombre", "RTN", "Telefono", "Correo", "Pais", "Direccion"};
    private final List<Empresa> empresas;

    EmpresaTableModel() {
        this.empresas = new ArrayList<>();
    }

    void setEmpresas(List<Empresa> empresas) {
        this.empresas.clear();
        if (empresas != null) {
            this.empresas.addAll(empresas);
        }
        fireTableDataChanged();
    }

    Empresa getEmpresaAt(int rowIndex) {
        return empresas.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return empresas.size();
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
        Empresa empresa = empresas.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> empresa.getId();
            case 1 -> empresa.getNombre();
            case 2 -> empresa.getRtn();
            case 3 -> empresa.getTelefono();
            case 4 -> empresa.getCorreoElectronico();
            case 5 -> empresa.getPais() == null ? "" : empresa.getPais().getNombre();
            case 6 -> empresa.getDireccion();
            default -> "";
        };
    }
}
