package com.example.demo.service;

import com.example.demo.service.dto.GraduateResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class XlsxGraduateExporter {

  private static final String[] HEADERS = {
    "Rank", "Student Number", "Last Name", "First Name", "Overall Average"
  };

  private XlsxGraduateExporter() {}

  public static byte[] export(List<GraduateResult> graduates) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Graduates");

      CellStyle headerStyle = workbook.createCellStyle();
      Font boldFont = workbook.createFont();
      boldFont.setBold(true);
      headerStyle.setFont(boldFont);

      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < HEADERS.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(HEADERS[i]);
        cell.setCellStyle(headerStyle);
      }

      int rowIndex = 1;
      for (GraduateResult graduate : graduates) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(graduate.rank());
        row.createCell(1).setCellValue(graduate.studentNumber());
        row.createCell(2).setCellValue(graduate.lastName());
        row.createCell(3).setCellValue(graduate.firstName());
        row.createCell(4).setCellValue(graduate.overallAverage().doubleValue());
      }

      for (int i = 0; i < HEADERS.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to generate graduates XLSX", e);
    }
  }
}
