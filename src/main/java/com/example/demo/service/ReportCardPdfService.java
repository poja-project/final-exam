package com.example.demo.service;

import com.example.demo.service.dto.CourseAverageResult;
import com.example.demo.service.dto.ReportCardResult;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportCardPdfService {

  private final ReportCardService reportCardService;

  public byte[] generatePdf(UUID studentId, UUID schoolYearId) {
    ReportCardResult reportCard = reportCardService.buildReportCard(studentId, schoolYearId);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 36, 36, 36, 36);

    try {
      PdfWriter.getInstance(document, out);
      document.open();

      Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
      Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
      Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

      Paragraph title = new Paragraph("Releve de Notes", titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      title.setSpacingAfter(20);
      document.add(title);

      Paragraph info =
          new Paragraph(
              "Etudiant: "
                  + reportCard.studentId()
                  + " | Annee scolaire: "
                  + reportCard.schoolYearId(),
              normalFont);
      info.setSpacingAfter(10);
      document.add(info);

      PdfPTable table = new PdfPTable(5);
      table.setWidthPercentage(100);
      table.setWidths(new float[] {35, 25, 15, 10, 15});

      addHeaderCell(table, "Cours", headerFont);
      addHeaderCell(table, "Moyenne", headerFont);
      addHeaderCell(table, "Credits", headerFont);
      addHeaderCell(table, "Statut", headerFont);
      addHeaderCell(table, "Note/20", headerFont);

      for (CourseAverageResult course : reportCard.courses()) {
        addCell(table, course.courseTitle(), normalFont);
        addCell(table, course.average() != null ? course.average().toString() : "N/A", normalFont);
        addCell(table, String.valueOf(course.credit()), normalFont);
        addCell(table, course.complete() ? "Complet" : "Incomplet", normalFont);
        addCell(
            table,
            course.average() != null ? course.average().toString() + "/20" : "N/A",
            normalFont);
      }

      document.add(table);
      document.add(new Paragraph(" ", normalFont));

      Paragraph summary = new Paragraph("Resultats", headerFont);
      summary.setSpacingAfter(5);
      document.add(summary);

      Paragraph averageLine =
          new Paragraph(
              "Moyenne generale: "
                  + (reportCard.overallAverage() != null
                      ? reportCard.overallAverage() + "/20"
                      : "N/A"),
              normalFont);
      averageLine.setSpacingAfter(3);
      document.add(averageLine);

      Paragraph creditsLine =
          new Paragraph("Credits obtenus: " + reportCard.creditsEarned(), normalFont);
      creditsLine.setSpacingAfter(3);
      document.add(creditsLine);

      Paragraph statusLine =
          new Paragraph("Statut: " + (reportCard.complete() ? "Complet" : "Incomplet"), normalFont);
      document.add(statusLine);

      document.close();
    } catch (DocumentException e) {
      throw new RuntimeException("Failed to generate PDF report card", e);
    }

    return out.toByteArray();
  }

  private void addHeaderCell(PdfPTable table, String text, Font font) {
    PdfPCell cell = new PdfPCell(new Paragraph(text, font));
    cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
    cell.setPadding(5);
    table.addCell(cell);
  }

  private void addCell(PdfPTable table, String text, Font font) {
    PdfPCell cell = new PdfPCell(new Paragraph(text, font));
    cell.setPadding(5);
    table.addCell(cell);
  }
}
