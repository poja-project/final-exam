package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class GroupControllerIT extends FacadeIT {

  private final ObjectMapper mapper = new ObjectMapper();
  private int cohortSeq = 100;

  private HttpHeaders adminHeaders() {
    var h = utils.sessionHeaders(ADMIN_EMAIL, ADMIN_PASSWORD);
    h.setContentType(MediaType.APPLICATION_JSON);
    return h;
  }

  private UUID extractId(String body) throws Exception {
    return UUID.fromString(mapper.readTree(body).get("id").asText());
  }

  private UUID createCohort(String label) throws Exception {
    var res =
        testRestTemplate.postForEntity(
            "/admin/groups/../cohorts",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", label, "entryYear", cohortSeq++)),
                adminHeaders()),
            String.class);
    return extractId(res.getBody());
  }

  private UUID createCohortViaApi(String label, int entryYear) throws Exception {
    var res =
        testRestTemplate.postForEntity(
            "/admin/cohorts",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", label, "entryYear", entryYear)),
                adminHeaders()),
            String.class);
    return extractId(res.getBody());
  }

  private UUID createGroup(UUID cohortId, String ref) throws Exception {
    var res =
        testRestTemplate.postForEntity(
            "/admin/groups",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("reference", ref, "cohortId", cohortId.toString())),
                adminHeaders()),
            String.class);
    return extractId(res.getBody());
  }

  @Test
  void createGroup_returns201() throws Exception {
    UUID cohortId = createCohortViaApi("GRP-COHORT-1", 2030);
    var body = Map.of("reference", "GRP-TEST-1", "cohortId", cohortId.toString());
    var res =
        testRestTemplate.postForEntity(
            "/admin/groups",
            new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(extractId(res.getBody())).isNotNull();
  }

  @Test
  void createGroup_cohortNotFound_returns404() throws Exception {
    var body = Map.of("reference", "GRP-404", "cohortId", UUID.randomUUID().toString());
    var res =
        testRestTemplate.postForEntity(
            "/admin/groups",
            new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void enrollStudent_andListHistory() throws Exception {
    UUID studentId = UUID.fromString(STUDENT_A_ID);
    UUID cohortId = UUID.fromString(COHORT_ID);
    UUID groupId = createGroup(cohortId, "GRP-ENR-1");

    var enrollBody = Map.of("groupId", groupId.toString(), "startDate", Instant.now().toString());
    var enrollRes =
        testRestTemplate.postForEntity(
            "/admin/students/" + studentId + "/enrollments",
            new HttpEntity<>(mapper.writeValueAsString(enrollBody), adminHeaders()),
            String.class);
    assertThat(enrollRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var histRes =
        testRestTemplate.exchange(
            "/students/" + studentId + "/enrollments",
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders()),
            String.class);
    assertThat(histRes.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void enrollStudent_studentNotFound_returns404() throws Exception {
    UUID cohortId = UUID.fromString(COHORT_ID);
    UUID groupId = createGroup(cohortId, "GRP-404-E");

    var enrollBody = Map.of("groupId", groupId.toString(), "startDate", Instant.now().toString());
    var res =
        testRestTemplate.postForEntity(
            "/admin/students/" + UUID.randomUUID() + "/enrollments",
            new HttpEntity<>(mapper.writeValueAsString(enrollBody), adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void enrollStudent_groupNotFound_returns404() throws Exception {
    UUID studentId = UUID.fromString(STUDENT_A_ID);
    var enrollBody =
        Map.of("groupId", UUID.randomUUID().toString(), "startDate", Instant.now().toString());
    var res =
        testRestTemplate.postForEntity(
            "/admin/students/" + studentId + "/enrollments",
            new HttpEntity<>(mapper.writeValueAsString(enrollBody), adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void createAssignment_returns201() throws Exception {
    UUID teacherId = UUID.fromString("00000000-0000-0000-0000-000000000005");
    UUID cohortId = createCohortViaApi("ASG-COHORT", 2031);
    UUID courseId = createCourse("ASG-C-1", "Assignment Course", 3);
    UUID groupId = createGroup(cohortId, "ASG-G-1");
    UUID semId = createSemester(cohortId, "ASG-Y1", 1);

    var body =
        Map.of(
            "teacherId",
            teacherId.toString(),
            "courseId",
            courseId.toString(),
            "groupId",
            groupId.toString(),
            "semesterId",
            semId.toString());
    var res =
        testRestTemplate.postForEntity(
            "/admin/assignments",
            new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void createAssignment_teacherNotFound_returns404() throws Exception {
    UUID cohortId = createCohortViaApi("ASG-NF-COHORT", 2032);
    UUID courseId = createCourse("ASG-NF", "NF Course", 3);
    UUID groupId = createGroup(cohortId, "ASG-NFG");
    UUID semId = createSemester(cohortId, "NF-Y1", 1);

    var body =
        Map.of(
            "teacherId",
            UUID.randomUUID().toString(),
            "courseId",
            courseId.toString(),
            "groupId",
            groupId.toString(),
            "semesterId",
            semId.toString());
    var res =
        testRestTemplate.postForEntity(
            "/admin/assignments",
            new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void createAssignment_groupNotFound_returns404() throws Exception {
    UUID teacherId = UUID.fromString("00000000-0000-0000-0000-000000000005");
    UUID cohortId = createCohortViaApi("ASG-GNF-COHORT", 2033);
    UUID courseId = createCourse("ASG-GNF", "GNF Course", 3);
    UUID semId = createSemester(cohortId, "GNF-Y1", 1);

    var body =
        Map.of(
            "teacherId",
            teacherId.toString(),
            "courseId",
            courseId.toString(),
            "groupId",
            UUID.randomUUID().toString(),
            "semesterId",
            semId.toString());
    var res =
        testRestTemplate.postForEntity(
            "/admin/assignments",
            new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void createAssignment_semesterNotFound_returns404() throws Exception {
    UUID teacherId = UUID.fromString("00000000-0000-0000-0000-000000000005");
    UUID cohortId = createCohortViaApi("ASG-SNF-COHORT", 2034);
    UUID courseId = createCourse("ASG-SNF", "SNF Course", 3);
    UUID groupId = createGroup(cohortId, "ASG-SNFG");

    var body =
        Map.of(
            "teacherId",
            teacherId.toString(),
            "courseId",
            courseId.toString(),
            "groupId",
            groupId.toString(),
            "semesterId",
            UUID.randomUUID().toString());
    var res =
        testRestTemplate.postForEntity(
            "/admin/assignments",
            new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void listAssignments_all() {
    var res =
        testRestTemplate.exchange(
            "/admin/assignments", HttpMethod.GET, new HttpEntity<>(adminHeaders()), String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void studentCannotCreateGroup() throws Exception {
    var body = Map.of("reference", "NOPE", "cohortId", COHORT_ID);
    var res =
        testRestTemplate.postForEntity(
            "/admin/groups",
            new HttpEntity<>(
                mapper.writeValueAsString(body),
                utils.sessionHeaders(STUDENT_A_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private UUID createCourse(String ref, String title, int credit) throws Exception {
    var res =
        testRestTemplate.postForEntity(
            "/admin/courses",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("reference", ref, "title", title, "credit", credit)),
                adminHeaders()),
            String.class);
    return extractId(res.getBody());
  }

  private UUID createSemester(UUID cohortId, String yearLabel, int yearNumber) throws Exception {
    var yearRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts/" + cohortId + "/school-years",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", yearLabel, "yearNumber", yearNumber)),
                adminHeaders()),
            String.class);
    UUID yearId = extractId(yearRes.getBody());
    var semRes =
        testRestTemplate.postForEntity(
            "/admin/school-years/" + yearId + "/semesters",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("number", 1, "startDate", "2023-09-01", "endDate", "2024-01-31")),
                adminHeaders()),
            String.class);
    return extractId(semRes.getBody());
  }
}
