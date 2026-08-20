package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.service.dto.GraduateResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class XlsxGraduateExporterTest {

  @Test
  void export_emptyList_producesValidXlsx() {
    byte[] result = XlsxGraduateExporter.export(List.of());
    assertThat(result).isNotEmpty();
    assertThat(result.length).isGreaterThan(0);
  }

  @Test
  void export_withGraduates_producesValidXlsx() {
    var graduates =
        List.of(
            new GraduateResult(
                1, UUID.randomUUID(), "STD-001", "Dupont", "Jean", new BigDecimal("15.50")),
            new GraduateResult(
                2, UUID.randomUUID(), "STD-002", "Martin", "Sophie", new BigDecimal("14.25")));
    byte[] result = XlsxGraduateExporter.export(graduates);
    assertThat(result).isNotEmpty();
    assertThat(result.length).isGreaterThan(100);
  }

  @Test
  void export_singleGraduate() {
    var graduates =
        List.of(
            new GraduateResult(
                1, UUID.randomUUID(), "STD-1", "Test", "One", new BigDecimal("12.00")));
    byte[] result = XlsxGraduateExporter.export(graduates);
    assertThat(result).isNotEmpty();
  }
}
