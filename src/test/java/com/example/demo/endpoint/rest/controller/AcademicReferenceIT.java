package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.ADMIN_EMAIL;
import static com.example.demo.conf.TestUtils.ADMIN_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class AcademicReferenceIT extends FacadeIT {

  private final ObjectMapper mapper = new ObjectMapper();

  private HttpHeaders adminHeaders() {
    var h = utils.sessionHeaders(ADMIN_EMAIL, ADMIN_PASSWORD);
    h.setContentType(MediaType.APPLICATION_JSON);
    return h;
  }

  private UUID extractId(String body) throws Exception {
    JsonNode node = mapper.readTree(body);
    return UUID.fromString(node.get("id").asText());
  }

  @Test
  void createCohort_returns201() throws Exception {
    var body = Map.of("label", "2025 Cohort", "entryYear", 2025);
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response = testRestTemplate.postForEntity("/admin/cohorts", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UUID id = extractId(response.getBody());
    assertThat(id).isNotNull();
  }

  @Test
  void listCohorts_returns200() {
    var request = new HttpEntity<>(adminHeaders());
    var response = testRestTemplate.exchange("/cohorts", HttpMethod.GET, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotBlank();
  }

  @Test
  void createSchoolYear_returns201() throws Exception {
    var cohortBody = Map.of("label", "SY Test Cohort", "entryYear", 2024);
    var cohortReq = new HttpEntity<>(mapper.writeValueAsString(cohortBody), adminHeaders());
    var cohortRes = testRestTemplate.postForEntity("/admin/cohorts", cohortReq, String.class);
    UUID cohortId = extractId(cohortRes.getBody());

    var body = Map.of("label", "2024-2025", "yearNumber", 1);
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response =
        testRestTemplate.postForEntity(
            "/admin/cohorts/" + cohortId + "/school-years", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UUID yearId = extractId(response.getBody());
    assertThat(yearId).isNotNull();
  }

  @Test
  void createSemester_returns201() throws Exception {
    var cohortRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", "Sem Cohort", "entryYear", 2022)),
                adminHeaders()),
            String.class);
    UUID cohortId = extractId(cohortRes.getBody());

    var yearRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts/" + cohortId + "/school-years",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", "2022-2023", "yearNumber", 1)),
                adminHeaders()),
            String.class);
    UUID yearId = extractId(yearRes.getBody());

    var body = Map.of("number", 1, "startDate", "2022-09-01", "endDate", "2023-01-31");
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response =
        testRestTemplate.postForEntity(
            "/admin/school-years/" + yearId + "/semesters", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void createCourse_returns201() throws Exception {
    var body = Map.of("reference", "MATH-101", "title", "Mathematics", "credit", 6);
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response = testRestTemplate.postForEntity("/admin/courses", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void addCourseToSemester_returns201() throws Exception {
    var courseRes =
        testRestTemplate.postForEntity(
            "/admin/courses",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("reference", "CS-201", "title", "Data Structures", "credit", 4)),
                adminHeaders()),
            String.class);
    UUID courseId = extractId(courseRes.getBody());

    var cohortRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", "Offer Cohort", "entryYear", 2021)),
                adminHeaders()),
            String.class);
    UUID cohortId = extractId(cohortRes.getBody());
    var yearRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts/" + cohortId + "/school-years",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", "2021-2022", "yearNumber", 1)),
                adminHeaders()),
            String.class);
    UUID yearId = extractId(yearRes.getBody());
    var semRes =
        testRestTemplate.postForEntity(
            "/admin/school-years/" + yearId + "/semesters",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("number", 1, "startDate", "2021-09-01", "endDate", "2022-01-31")),
                adminHeaders()),
            String.class);
    UUID semId = extractId(semRes.getBody());

    var body = Map.of("courseId", courseId.toString());
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response =
        testRestTemplate.postForEntity(
            "/admin/semesters/" + semId + "/courses", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void createGroup_returns201() throws Exception {
    var cohortRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", "Grp Cohort", "entryYear", 2023)),
                adminHeaders()),
            String.class);
    UUID cohortId = extractId(cohortRes.getBody());

    var body = Map.of("reference", "GRP-A", "cohortId", cohortId.toString());
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response = testRestTemplate.postForEntity("/admin/groups", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void listAssignments_all_returns200() {
    var request = new HttpEntity<>(adminHeaders());
    var response =
        testRestTemplate.exchange("/admin/assignments", HttpMethod.GET, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void listAssignments_byTeacher_returns200() {
    var request = new HttpEntity<>(adminHeaders());
    var response =
        testRestTemplate.exchange(
            "/admin/assignments?teacherId=" + UUID.randomUUID(),
            HttpMethod.GET,
            request,
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void listAssignments_bySemester_returns200() {
    var request = new HttpEntity<>(adminHeaders());
    var response =
        testRestTemplate.exchange(
            "/admin/assignments?semesterId=" + UUID.randomUUID(),
            HttpMethod.GET,
            request,
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void createSchoolYear_cohortNotFound_returns404() throws Exception {
    var body = Map.of("label", "2024-2025", "yearNumber", 1);
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response =
        testRestTemplate.postForEntity(
            "/admin/cohorts/" + UUID.randomUUID() + "/school-years", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void createSemester_schoolYearNotFound_returns404() throws Exception {
    var body = Map.of("number", 1, "startDate", "2024-09-01", "endDate", "2025-01-31");
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response =
        testRestTemplate.postForEntity(
            "/admin/school-years/" + UUID.randomUUID() + "/semesters", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void addCourseToSemester_semesterNotFound_returns404() throws Exception {
    var courseRes =
        testRestTemplate.postForEntity(
            "/admin/courses",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("reference", "MISS-101", "title", "Missing", "credit", 3)),
                adminHeaders()),
            String.class);
    UUID courseId = extractId(courseRes.getBody());

    var body = Map.of("courseId", courseId.toString());
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response =
        testRestTemplate.postForEntity(
            "/admin/semesters/" + UUID.randomUUID() + "/courses", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void createCourse_validationFails_returns400() throws Exception {
    var body = Map.of("title", "No reference");
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response = testRestTemplate.postForEntity("/admin/courses", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
