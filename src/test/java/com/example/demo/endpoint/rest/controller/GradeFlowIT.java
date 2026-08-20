package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

class GradeFlowIT extends FacadeIT {

  private final ObjectMapper mapper = new ObjectMapper();

  private String uid() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  private HttpEntity<String> jsonAsAdmin(Map<String, Object> body) throws Exception {
    return new HttpEntity<>(
        mapper.writeValueAsString(body), utils.sessionHeaders(ADMIN_EMAIL, ADMIN_PASSWORD));
  }

  private HttpEntity<String> jsonAsTeacher(Map<String, Object> body) throws Exception {
    return new HttpEntity<>(
        mapper.writeValueAsString(body), utils.sessionHeaders(TEACHER_EMAIL, SEEDED_PASSWORD));
  }

  private HttpEntity<String> jsonAsStudentA(Map<String, Object> body) throws Exception {
    return new HttpEntity<>(
        mapper.writeValueAsString(body), utils.sessionHeaders(STUDENT_A_EMAIL, SEEDED_PASSWORD));
  }

  private UUID extractId(String body) throws Exception {
    if (body == null || body.isBlank()) throw new IllegalStateException("Empty body: " + body);
    JsonNode node = mapper.readTree(body);
    if (node.isArray()) node = node.get(0);
    if (node == null || !node.has("id")) throw new IllegalStateException("No 'id' in: " + body);
    return UUID.fromString(node.get("id").asText());
  }

  private UUID createCohort() throws Exception {
    var res =
        testRestTemplate.postForEntity(
            "/admin/cohorts",
            jsonAsAdmin(Map.of("label", "GF-" + uid(), "entryYear", 2024)),
            String.class);
    return extractId(res.getBody());
  }

  private UUID createCourse(String suffix) throws Exception {
    var res =
        testRestTemplate.postForEntity(
            "/admin/courses",
            jsonAsAdmin(Map.of("reference", "GC-" + uid(), "title", "T-" + suffix, "credit", 4)),
            String.class);
    return extractId(res.getBody());
  }

  private UUID createSemester(UUID cohortId) throws Exception {
    var yearRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts/" + cohortId + "/school-years",
            jsonAsAdmin(Map.of("label", "SY-" + uid(), "yearNumber", 1)),
            String.class);
    UUID yearId = extractId(yearRes.getBody());
    var semRes =
        testRestTemplate.postForEntity(
            "/admin/school-years/" + yearId + "/semesters",
            jsonAsAdmin(Map.of("number", 1, "startDate", "2023-09-01", "endDate", "2024-01-31")),
            String.class);
    return extractId(semRes.getBody());
  }

  private UUID createExam(UUID courseId, UUID semesterId) throws Exception {
    var res =
        testRestTemplate.postForEntity(
            "/exams",
            jsonAsAdmin(
                Map.of(
                    "courseId",
                    courseId.toString(),
                    "semesterId",
                    semesterId.toString(),
                    "exams",
                    List.of(
                        Map.of("date", Instant.now().toString(), "coeff", new BigDecimal("1.0"))))),
            String.class);
    return extractId(res.getBody());
  }

  private void createAssignment(UUID courseId, UUID semesterId) throws Exception {
    UUID teacherId = UUID.fromString("00000000-0000-0000-0000-000000000005");
    UUID seedCohortId = UUID.fromString(COHORT_ID);
    var groupRes =
        testRestTemplate.postForEntity(
            "/admin/groups",
            jsonAsAdmin(Map.of("reference", "GG-" + uid(), "cohortId", seedCohortId.toString())),
            String.class);
    UUID groupId = extractId(groupRes.getBody());
    testRestTemplate.postForEntity(
        "/admin/assignments",
        jsonAsAdmin(
            Map.of(
                "teacherId",
                teacherId.toString(),
                "courseId",
                courseId.toString(),
                "groupId",
                groupId.toString(),
                "semesterId",
                semesterId.toString())),
        String.class);
  }

  @Test
  void createAndListExams() throws Exception {
    UUID courseId = createCourse("EX");
    UUID cohortId = createCohort();
    UUID semesterId = createSemester(cohortId);
    createAssignment(courseId, semesterId);

    UUID examId = createExam(courseId, semesterId);
    assertThat(examId).isNotNull();

    var listRes =
        testRestTemplate.exchange(
            "/courses/" + courseId + "/exams?semesterId=" + semesterId,
            HttpMethod.GET,
            jsonAsAdmin(Map.of()),
            String.class);
    assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void createExams_duplicateConflict_returns409() throws Exception {
    UUID courseId = createCourse("DUP");
    UUID cohortId = createCohort();
    UUID semesterId = createSemester(cohortId);
    createAssignment(courseId, semesterId);

    Map<String, Object> examBody =
        Map.of(
            "courseId",
            courseId.toString(),
            "semesterId",
            semesterId.toString(),
            "exams",
            List.of(Map.of("date", Instant.now().toString(), "coeff", new BigDecimal("1.0"))));
    testRestTemplate.postForEntity("/exams", jsonAsAdmin(examBody), String.class);

    var res = testRestTemplate.postForEntity("/exams", jsonAsAdmin(examBody), String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void submitGrade_andListForStudent() throws Exception {
    UUID courseId = createCourse("GR");
    UUID cohortId = createCohort();
    UUID semesterId = createSemester(cohortId);
    createAssignment(courseId, semesterId);

    UUID examId = createExam(courseId, semesterId);
    UUID studentId = UUID.fromString(STUDENT_A_ID);

    var gradeRes =
        testRestTemplate.postForEntity(
            "/grades",
            jsonAsAdmin(
                Map.of(
                    "studentId",
                    studentId.toString(),
                    "examId",
                    examId.toString(),
                    "value",
                    new BigDecimal("15.5"))),
            String.class);
    assertThat(gradeRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var listRes =
        testRestTemplate.exchange(
            "/students/" + studentId + "/grades",
            HttpMethod.GET,
            jsonAsAdmin(Map.of()),
            String.class);
    assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void gradeHistory_returns200() throws Exception {
    UUID courseId = createCourse("GH");
    UUID cohortId = createCohort();
    UUID semesterId = createSemester(cohortId);
    createAssignment(courseId, semesterId);

    UUID examId = createExam(courseId, semesterId);
    UUID studentId = UUID.fromString(STUDENT_A_ID);

    testRestTemplate.postForEntity(
        "/grades",
        jsonAsAdmin(
            Map.of(
                "studentId",
                studentId.toString(),
                "examId",
                examId.toString(),
                "value",
                new BigDecimal("12.0"))),
        String.class);

    var histRes =
        testRestTemplate.exchange(
            "/exams/" + examId + "/grades/" + studentId + "/history",
            HttpMethod.GET,
            jsonAsAdmin(Map.of()),
            String.class);
    assertThat(histRes.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void gradesForExam_returns200() throws Exception {
    UUID courseId = createCourse("FE");
    UUID cohortId = createCohort();
    UUID semesterId = createSemester(cohortId);
    createAssignment(courseId, semesterId);

    UUID examId = createExam(courseId, semesterId);

    var listRes =
        testRestTemplate.exchange(
            "/exams/" + examId + "/grades", HttpMethod.GET, jsonAsAdmin(Map.of()), String.class);
    assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void teacherCanSubmitGradesForAssignedCourse() throws Exception {
    UUID courseId = createCourse("TC");
    UUID cohortId = createCohort();
    UUID semesterId = createSemester(cohortId);
    createAssignment(courseId, semesterId);

    UUID examId = createExam(courseId, semesterId);

    var gradeRes =
        testRestTemplate.postForEntity(
            "/grades",
            jsonAsTeacher(
                Map.of(
                    "studentId",
                    STUDENT_A_ID,
                    "examId",
                    examId.toString(),
                    "value",
                    new BigDecimal("18.0"))),
            String.class);
    assertThat(gradeRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void studentCannotSubmitGrades() throws Exception {
    UUID courseId = createCourse("SC");
    UUID cohortId = createCohort();
    UUID semesterId = createSemester(cohortId);
    createAssignment(courseId, semesterId);

    UUID examId = createExam(courseId, semesterId);

    var gradeRes =
        testRestTemplate.postForEntity(
            "/grades",
            jsonAsStudentA(
                Map.of(
                    "studentId",
                    UUID.randomUUID().toString(),
                    "examId",
                    examId.toString(),
                    "value",
                    new BigDecimal("10.0"))),
            String.class);
    assertThat(gradeRes.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void unauthenticatedExamCreate_returns401() {
    var res =
        testRestTemplate.postForEntity(
            "/exams",
            Map.of(
                "courseId",
                UUID.randomUUID().toString(),
                "semesterId",
                UUID.randomUUID().toString(),
                "exams",
                List.of(Map.of("date", Instant.now().toString(), "coeff", new BigDecimal("1.0")))),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void gradeForStudent_endpoint_withAdminAccess() throws Exception {
    UUID courseId = createCourse("GF");
    UUID cohortId = createCohort();
    UUID semesterId = createSemester(cohortId);
    createAssignment(courseId, semesterId);

    UUID examId = createExam(courseId, semesterId);
    UUID studentId = UUID.fromString(STUDENT_A_ID);

    testRestTemplate.postForEntity(
        "/grades",
        jsonAsAdmin(
            Map.of(
                "studentId",
                studentId.toString(),
                "examId",
                examId.toString(),
                "value",
                new BigDecimal("14.0"))),
        String.class);

    var res =
        testRestTemplate.exchange(
            "/students/" + studentId + "/grades",
            HttpMethod.GET,
            jsonAsAdmin(Map.of()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
