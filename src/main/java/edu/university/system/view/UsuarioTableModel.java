package edu.university.system.view;

import edu.university.system.model.Usuario;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class UsuarioTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Usuario", "Nombre", "Correo", "Rol", "Activo", "Debe cambiar", "Ultimo acceso"};
    private final List<Usuario> usuarios;

    UsuarioTableModel() {
        this.usuarios = new ArrayList<>();
    }

    void setUsuarios(List<Usuario> usuarios) {
        this.usuarios.clear();
        if (usuarios != null) {
            this.usuarios.addAll(usuarios);
        }
        fireTableDataChanged();
    }

    Usuario getUsuarioAt(int rowIndex) {
        return usuarios.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return usuarios.size();
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
            case 5, 6 -> Boolean.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Usuario usuario = usuarios.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> usuario.getId();
            case 1 -> usuario.getNombreUsuario();
            case 2 -> usuario.getNombreMostrar();
            case 3 -> usuario.getCorreoElectronico();
            case 4 -> usuario.getRol() == null ? "" : usuario.getRol().getNombre();
            case 5 -> usuario.isActivo();
            case 6 -> usuario.isDebeCambiarPassword();
            case 7 -> usuario.getUltimoAcceso() == null ? "" : usuario.getUltimoAcceso().toString();
            default -> "";
        };
    }
}
