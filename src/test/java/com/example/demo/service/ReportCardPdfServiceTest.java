package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.demo.service.dto.CourseAverageResult;
import com.example.demo.service.dto.ReportCardResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportCardPdfServiceTest {

  @Mock private ReportCardService reportCardService;

  @InjectMocks private ReportCardPdfService reportCardPdfService;

  @Test
  void generatePdf_returnsBytes() {
    UUID studentId = UUID.randomUUID();
    UUID schoolYearId = UUID.randomUUID();

    CourseAverageResult courseResult =
        new CourseAverageResult(UUID.randomUUID(), "Algebra", 6, new BigDecimal("15.00"), true);
    ReportCardResult reportCard =
        new ReportCardResult(
            studentId, schoolYearId, List.of(courseResult), new BigDecimal("15.00"), 6, true);

    when(reportCardService.buildReportCard(eq(studentId), eq(schoolYearId))).thenReturn(reportCard);

    byte[] pdf = reportCardPdfService.generatePdf(studentId, schoolYearId);
    assertThat(pdf).isNotNull();
    assertThat(pdf.length).isGreaterThan(0);
    assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
  }

  @Test
  void generatePdf_noCourses_returnsValidPdf() {
    UUID studentId = UUID.randomUUID();
    UUID schoolYearId = UUID.randomUUID();

    ReportCardResult reportCard =
        new ReportCardResult(studentId, schoolYearId, List.of(), null, 0, false);

    when(reportCardService.buildReportCard(eq(studentId), eq(schoolYearId))).thenReturn(reportCard);

    byte[] pdf = reportCardPdfService.generatePdf(studentId, schoolYearId);
    assertThat(pdf).isNotNull();
    assertThat(pdf.length).isGreaterThan(0);
  }
}
