package com.example.demo.service.event;

import com.example.demo.endpoint.event.model.ReportCardPdfRequestedEvent;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.service.ReportCardPdfService;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ReportCardPdfRequestedEventService implements Consumer<ReportCardPdfRequestedEvent> {

  private final ReportCardPdfService reportCardPdfService;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @Override
  public void accept(ReportCardPdfRequestedEvent event) {
    UUID studentId = UUID.fromString(event.getStudentId());
    UUID schoolYearId = UUID.fromString(event.getSchoolYearId());
    String studentEmail = event.getStudentEmail();

    log.info("Generating PDF report card for student={} schoolYear={}", studentId, schoolYearId);

    byte[] pdfBytes = reportCardPdfService.generatePdf(studentId, schoolYearId);

    String bucketKey = "report-cards/%s/%s.pdf".formatted(studentId, schoolYearId);

    File tempFile = writeToTempFile(pdfBytes, "report-card", ".pdf");
    try {
      bucketComponent.upload(tempFile, bucketKey);
    } finally {
      tempFile.delete();
    }

    String presignedUrl = bucketComponent.presign(bucketKey, Duration.ofHours(24)).toString();

    sendEmail(studentEmail, presignedUrl, studentId, schoolYearId);

    log.info("PDF report card sent for student={} schoolYear={}", studentId, schoolYearId);
  }

  private void sendEmail(String toEmail, String downloadUrl, UUID studentId, UUID schoolYearId) {
    try {
      Email email =
          new Email(
              new InternetAddress(toEmail),
              List.of(),
              List.of(),
              "Votre releve de notes - " + schoolYearId,
              "<html><body>"
                  + "<h2>Releve de notes</h2>"
                  + "<p>Bonjour,</p>"
                  + "<p>Votre releve de notes pour l'annee scolaire <strong>"
                  + schoolYearId
                  + "</strong> est maintenant disponible.</p>"
                  + "<p><a href=\""
                  + downloadUrl
                  + "\">Telecharger votre releve de notes (PDF)</a></p>"
                  + "<p>Ce lien est valide pendant 24 heures.</p>"
                  + "<p>Cordialement,<br/>L'administration</p>"
                  + "</body></html>",
              List.of());
      mailer.accept(email);
    } catch (Exception e) {
      log.error("Failed to send report card email to {}", toEmail, e);
    }
  }

  private File writeToTempFile(byte[] content, String prefix, String suffix) {
    try {
      File file = File.createTempFile(prefix, suffix);
      Files.write(file.toPath(), content);
      return file;
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write temp file for PDF upload", e);
    }
  }
}
