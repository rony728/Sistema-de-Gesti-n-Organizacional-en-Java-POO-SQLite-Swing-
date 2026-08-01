package edu.university.system.view;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

final class ViewFeedback {

    private static final Color INVALID_BACKGROUND = new Color(255, 245, 245);
    private static final Border INVALID_BORDER = BorderFactory.createLineBorder(new Color(198, 60, 60), 1);

    private ViewFeedback() {
    }

    static void configureTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        table.setGridColor(AppTheme.BORDER);
        table.setSelectionBackground(AppTheme.BLUE);
        table.setSelectionForeground(AppTheme.WHITE);
        table.getTableHeader().setBackground(AppTheme.NAVY);
        table.getTableHeader().setForeground(AppTheme.WHITE);
    }

    static void addInstantSearch(JTextField searchField, Runnable searchAction) {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                searchAction.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                searchAction.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                searchAction.run();
            }
        });
    }

    static void markInvalid(JComponent component, String message) {
        if (component.getClientProperty("defaultBorder") == null) {
            component.putClientProperty("defaultBorder", component.getBorder());
            component.putClientProperty("defaultBackground", component.getBackground());
        }
        component.setBorder(INVALID_BORDER);
        component.setBackground(INVALID_BACKGROUND);
        component.setToolTipText(message);
    }

    static void clearInvalid(JComponent... components) {
        for (JComponent component : components) {
            Object defaultBorder = component.getClientProperty("defaultBorder");
            Object defaultBackground = component.getClientProperty("defaultBackground");
            if (defaultBorder instanceof Border border) {
                component.setBorder(border);
            }
            if (defaultBackground instanceof Color color) {
                component.setBackground(color);
            }
            component.setToolTipText(null);
        }
    }

    static boolean markBlank(JTextField field, String message) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            markInvalid(field, message);
            return true;
        }
        return false;
    }

    static boolean markEmptyCombo(JComboBox<?> comboBox, String message) {
        if (comboBox.getSelectedItem() == null) {
            markInvalid(comboBox, message);
            return true;
        }
        return false;
    }

    static boolean markInvalidRequiredDate(JTextField field, String fieldName) {
        if (markBlank(field, "Ingrese " + fieldName + " con formato yyyy-MM-dd.")) {
            return true;
        }
        try {
            LocalDate.parse(field.getText().trim());
            return false;
        } catch (DateTimeParseException exception) {
            markInvalid(field, fieldName + " debe tener formato yyyy-MM-dd.");
            return true;
        }
    }

    static boolean markInvalidOptionalDate(JTextField field, String fieldName) {
        if (field.getText() == null || field.getText().isBlank()) {
            return false;
        }
        try {
            LocalDate.parse(field.getText().trim());
            return false;
        } catch (DateTimeParseException exception) {
            markInvalid(field, fieldName + " debe tener formato yyyy-MM-dd.");
            return true;
        }
    }

    static boolean markInvalidDecimal(JTextField field, String fieldName) {
        if (field.getText() == null || field.getText().isBlank()) {
            return false;
        }
        try {
            new BigDecimal(field.getText().trim());
            return false;
        } catch (NumberFormatException exception) {
            markInvalid(field, fieldName + " debe ser numerico.");
            return true;
        }
    }

    static boolean markInvalidInteger(JTextField field, String fieldName) {
        if (field.getText() == null || field.getText().isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(field.getText().trim());
            return false;
        } catch (NumberFormatException exception) {
            markInvalid(field, fieldName + " debe ser un numero entero.");
            return true;
        }
    }

    static void showValidation(JComponent parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Revise los datos", JOptionPane.WARNING_MESSAGE);
    }

    static void showError(JComponent parent, String contextMessage, RuntimeException exception) {
        JOptionPane.showMessageDialog(
                parent,
                contextMessage + "\n" + friendlyMessage(exception),
                "No se pudo completar la accion",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static String friendlyMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException || current.getClass().getName().contains("SQLException")) {
                return "La operacion no pudo completarse por una restriccion de la base de datos. Verifique datos duplicados o registros relacionados.";
            }
            current = current.getCause();
        }
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return "Intente nuevamente o revise los datos ingresados.";
        }
        return message;
    }
}
