package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class ReportCardIT extends FacadeIT {
  @org.springframework.boot.test.mock.mockito.MockBean
  private com.example.demo.endpoint.event.EventProducer<
          com.example.demo.endpoint.event.model.ReportCardPdfRequestedEvent>
      eventProducer;

  private final ObjectMapper mapper = new ObjectMapper();
  private static UUID sharedYearId;

  private HttpHeaders adminHeaders() {
    var h = utils.sessionHeaders(ADMIN_EMAIL, ADMIN_PASSWORD);
    h.setContentType(MediaType.APPLICATION_JSON);
    return h;
  }

  private UUID extractId(String body) throws Exception {
    return UUID.fromString(mapper.readTree(body).get("id").asText());
  }

  @BeforeEach
  void ensureSharedSchoolYearExists() throws Exception {
    if (sharedYearId != null) return;
    UUID cohortId = UUID.fromString(COHORT_ID);
    var yearRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts/" + cohortId + "/school-years",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", "RC-Shared-Year", "yearNumber", 1)),
                adminHeaders()),
            String.class);
    sharedYearId = extractId(yearRes.getBody());
  }

  @Test
  void getReportCard_asAdmin_returns200() {
    UUID studentId = UUID.fromString(STUDENT_A_ID);
    var res =
        testRestTemplate.exchange(
            "/students/" + studentId + "/report-card/" + sharedYearId,
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getReportCard_asOwnStudent_returns200() {
    UUID studentId = UUID.fromString(STUDENT_A_ID);
    var res =
        testRestTemplate.exchange(
            "/students/" + studentId + "/report-card/" + sharedYearId,
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(STUDENT_A_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getReportCard_studentCannotViewOther_returns403() {
    UUID studentId = UUID.fromString(STUDENT_A_ID);
    var res =
        testRestTemplate.exchange(
            "/students/" + studentId + "/report-card/" + sharedYearId,
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(STUDENT_B_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void sendReportCardPdf_returns202() {
    UUID studentId = UUID.fromString(STUDENT_A_ID);
    var res =
        testRestTemplate.postForEntity(
            "/students/" + studentId + "/report-card/" + sharedYearId + "/pdf",
            new HttpEntity<>(adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  void getCohortResults_returns200() {
    UUID cohortId = UUID.fromString(COHORT_ID);
    var res =
        testRestTemplate.exchange(
            "/admin/cohorts/" + cohortId + "/results",
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getCohortResults_studentCannotAccess_returns403() {
    var res =
        testRestTemplate.exchange(
            "/admin/cohorts/" + UUID.randomUUID() + "/results",
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(STUDENT_A_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
