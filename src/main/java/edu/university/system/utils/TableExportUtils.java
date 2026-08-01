package edu.university.system.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exporta el contenido visible de un JTable a Excel o PDF. Respeta filtros,
 * ordenamiento y valores nulos del modelo visual sin acoplarse a un DAO.
 */
public final class TableExportUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private TableExportUtils() {
    }

    public static void exportToExcel(JTable table, File file, String title) {
        validateExport(table, file);

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet(safeSheetName(title));

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);

            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Generado: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));

            Row headerRow = sheet.createRow(3);
            for (int column = 0; column < table.getColumnCount(); column++) {
                Cell cell = headerRow.createCell(column);
                cell.setCellValue(table.getColumnName(column));
                cell.setCellStyle(headerStyle);
            }

            for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
                Row row = sheet.createRow(viewRow + 4);
                for (int viewColumn = 0; viewColumn < table.getColumnCount(); viewColumn++) {
                    row.createCell(viewColumn).setCellValue(getDisplayValue(table, viewRow, viewColumn));
                }
            }

            for (int column = 0; column < table.getColumnCount(); column++) {
                sheet.autoSizeColumn(column);
            }

            workbook.write(outputStream);
        } catch (IOException exception) {
            throw new ExportException("No se pudo crear el archivo Excel.", exception);
        }
    }

    public static void exportToPdf(JTable table, File file, String title) {
        validateExport(table, file);

        Document document = new Document(PageSize.LETTER.rotate(), 28, 28, 28, 28);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            try {
                org.openpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
                org.openpdf.text.Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
                org.openpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

                Paragraph titleParagraph = new Paragraph(title, titleFont);
                titleParagraph.setAlignment(Element.ALIGN_CENTER);
                titleParagraph.setSpacingAfter(8);
                document.add(titleParagraph);

                Paragraph dateParagraph = new Paragraph("Generado: " + LocalDateTime.now().format(DATE_TIME_FORMATTER), textFont);
                dateParagraph.setAlignment(Element.ALIGN_RIGHT);
                dateParagraph.setSpacingAfter(12);
                document.add(dateParagraph);

                PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
                pdfTable.setWidthPercentage(100);

                for (int column = 0; column < table.getColumnCount(); column++) {
                    PdfPCell cell = new PdfPCell(new Phrase(table.getColumnName(column), headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(new Color(232, 237, 241));
                    cell.setPadding(5);
                    pdfTable.addCell(cell);
                }

                for (int row = 0; row < table.getRowCount(); row++) {
                    for (int column = 0; column < table.getColumnCount(); column++) {
                        PdfPCell cell = new PdfPCell(new Phrase(getDisplayValue(table, row, column), textFont));
                        cell.setPadding(4);
                        pdfTable.addCell(cell);
                    }
                }

                document.add(pdfTable);
            } finally {
                if (document.isOpen()) {
                    document.close();
                }
            }
        } catch (DocumentException | IOException exception) {
            throw new ExportException("No se pudo crear el archivo PDF.", exception);
        }
    }

    private static void validateExport(JTable table, File file) {
        if (table == null) {
            throw new ExportException("No hay una tabla para exportar.");
        }
        if (table.getRowCount() == 0) {
            throw new ExportException("No hay registros para exportar.");
        }
        if (file == null) {
            throw new ExportException("Seleccione una ubicacion valida para el archivo.");
        }
    }

    private static String getDisplayValue(JTable table, int viewRow, int viewColumn) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        int modelColumn = table.convertColumnIndexToModel(viewColumn);
        TableModel model = table.getModel();
        Object value = model.getValueAt(modelRow, modelColumn);
        return value == null ? "" : value.toString();
    }

    private static String safeSheetName(String title) {
        String cleanedTitle = title == null || title.isBlank() ? "Exportacion" : title.trim();
        cleanedTitle = cleanedTitle.replaceAll("[\\\\/?*\\[\\]:]", " ");
        return cleanedTitle.length() > 31 ? cleanedTitle.substring(0, 31) : cleanedTitle;
    }
}
