package edu.university.system.view;

import edu.university.system.utils.ExportException;
import edu.university.system.utils.TableExportUtils;
import edu.university.system.controller.AuthorizationService;
import edu.university.system.controller.Permission;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

final class ExportDialogUtils {

    private ExportDialogUtils() {
    }

    static void exportTableToExcel(JComponent parent, JTable table, String reportTitle, String defaultFileName) {
        if (!AuthorizationService.can(Permission.EXPORT)) {
            ViewFeedback.showValidation(parent, "No tiene permisos para exportar.");
            return;
        }
        File file = chooseFile(parent, defaultFileName, "xlsx", "Archivo Excel (*.xlsx)");
        if (file == null) {
            return;
        }
        try {
            TableExportUtils.exportToExcel(table, file, reportTitle);
            showSuccess(parent, file);
        } catch (ExportException exception) {
            ViewFeedback.showValidation(parent, exception.getMessage());
        } catch (RuntimeException exception) {
            ViewFeedback.showError(parent, "No se pudo exportar a Excel.", exception);
        }
    }

    static void exportTableToPdf(JComponent parent, JTable table, String reportTitle, String defaultFileName) {
        if (!AuthorizationService.can(Permission.EXPORT)) {
            ViewFeedback.showValidation(parent, "No tiene permisos para exportar.");
            return;
        }
        File file = chooseFile(parent, defaultFileName, "pdf", "Archivo PDF (*.pdf)");
        if (file == null) {
            return;
        }
        try {
            TableExportUtils.exportToPdf(table, file, reportTitle);
            showSuccess(parent, file);
        } catch (ExportException exception) {
            ViewFeedback.showValidation(parent, exception.getMessage());
        } catch (RuntimeException exception) {
            ViewFeedback.showError(parent, "No se pudo exportar a PDF.", exception);
        }
    }

    private static File chooseFile(JComponent parent, String defaultFileName, String extension, String description) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar " + description);
        fileChooser.setSelectedFile(new File(defaultFileName + "." + extension));
        fileChooser.setFileFilter(new FileNameExtensionFilter(description, extension));

        int result = fileChooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selectedFile = fileChooser.getSelectedFile();
        File file = ensureExtension(selectedFile, extension);

        if (file.exists()) {
            int confirmation = JOptionPane.showConfirmDialog(
                    parent,
                    "El archivo ya existe. Desea reemplazarlo?",
                    "Confirmar reemplazo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirmation != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        return file;
    }

    private static File ensureExtension(File file, String extension) {
        if (file.getName().toLowerCase().endsWith("." + extension)) {
            return file;
        }
        File parent = file.getParentFile();
        return parent == null
                ? new File(file.getName() + "." + extension)
                : new File(parent, file.getName() + "." + extension);
    }

    private static void showSuccess(JComponent parent, File file) {
        JOptionPane.showMessageDialog(
                parent,
                "Archivo exportado correctamente:\n" + file.getAbsolutePath(),
                "Exportacion exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
