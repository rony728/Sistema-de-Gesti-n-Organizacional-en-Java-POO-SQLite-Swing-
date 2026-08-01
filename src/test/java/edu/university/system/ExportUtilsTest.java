package edu.university.system;

import edu.university.system.utils.ExportException;
import edu.university.system.utils.TableExportUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.FileInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExportUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void exportsVisibleTableDataToXlsxAndKeepsHeadersAndNulls() throws Exception {
        JTable table = sampleTable();
        Path file = tempDir.resolve("empleados.xlsx");

        TableExportUtils.exportToExcel(table, file.toFile(), "Reporte de Empleados");

        assertTrue(file.toFile().isFile());
        assertTrue(file.toFile().length() > 0);
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file.toFile()))) {
            assertEquals("Reporte de Empleados", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
            assertEquals("ID", workbook.getSheetAt(0).getRow(3).getCell(0).getStringCellValue());
            assertEquals("Nombre", workbook.getSheetAt(0).getRow(3).getCell(1).getStringCellValue());
            assertEquals("", workbook.getSheetAt(0).getRow(4).getCell(1).getStringCellValue());
            assertEquals("Beta", workbook.getSheetAt(0).getRow(5).getCell(1).getStringCellValue());
        }
    }

    @Test
    void exportsVisibleTableDataToPdf() {
        JTable table = sampleTable();
        Path file = tempDir.resolve("proyectos.pdf");

        TableExportUtils.exportToPdf(table, file.toFile(), "Reporte de Proyectos");

        assertTrue(file.toFile().isFile());
        assertTrue(file.toFile().length() > 0);
    }

    @Test
    void rejectsEmptyTables() {
        JTable table = new JTable(new DefaultTableModel(new Object[]{"ID", "Nombre"}, 0));
        assertThrows(ExportException.class, () -> TableExportUtils.exportToExcel(table, tempDir.resolve("empty.xlsx").toFile(), "Vacio"));
        assertThrows(ExportException.class, () -> TableExportUtils.exportToPdf(table, tempDir.resolve("empty.pdf").toFile(), "Vacio"));
    }

    private JTable sampleTable() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Nombre"}, 0);
        model.addRow(new Object[]{2L, "Beta"});
        model.addRow(new Object[]{1L, null});
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.getRowSorter().toggleSortOrder(0);
        return table;
    }
}
