package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class TeacherControllerIT extends FacadeIT {

  private final ObjectMapper mapper = new ObjectMapper();

  private HttpHeaders adminHeaders() {
    var h = utils.sessionHeaders(ADMIN_EMAIL, ADMIN_PASSWORD);
    h.setContentType(MediaType.APPLICATION_JSON);
    return h;
  }

  private UUID extractId(String body) throws Exception {
    return UUID.fromString(mapper.readTree(body).get("id").asText());
  }

  @Test
  void myCourses_asTeacher_returns200() {
    var res =
        testRestTemplate.exchange(
            "/teachers/me/courses",
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(TEACHER_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void myCourses_asStudent_returns403() {
    var res =
        testRestTemplate.exchange(
            "/teachers/me/courses",
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(STUDENT_A_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void myCourses_withAssignment_returnsCourses() throws Exception {
    UUID teacherId = UUID.fromString("00000000-0000-0000-0000-000000000005");
    UUID cohortId = UUID.fromString(COHORT_ID);
    var courseRes =
        testRestTemplate.postForEntity(
            "/admin/courses",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("reference", "TC-C-1", "title", "Teacher Course", "credit", 4)),
                adminHeaders()),
            String.class);
    UUID courseId = extractId(courseRes.getBody());

    var groupRes =
        testRestTemplate.postForEntity(
            "/admin/groups",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("reference", "TC-G-1", "cohortId", cohortId.toString())),
                adminHeaders()),
            String.class);
    UUID groupId = extractId(groupRes.getBody());

    var yearRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts/" + cohortId + "/school-years",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", "TC-Y1", "yearNumber", 1)),
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
    UUID semId = extractId(semRes.getBody());

    var asgBody =
        Map.of(
            "teacherId",
            teacherId.toString(),
            "courseId",
            courseId.toString(),
            "groupId",
            groupId.toString(),
            "semesterId",
            semId.toString());
    testRestTemplate.postForEntity(
        "/admin/assignments",
        new HttpEntity<>(mapper.writeValueAsString(asgBody), adminHeaders()),
        String.class);

    var res =
        testRestTemplate.exchange(
            "/teachers/me/courses",
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(TEACHER_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(res.getBody()).contains(courseId.toString());
  }
}
