package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

class WebControllerIT extends FacadeIT {

  @Test
  void loginPage_returns200() {
    var res = testRestTemplate.getForEntity("/web/login", String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void cohortsPage_returns200() {
    var res =
        testRestTemplate.exchange(
            "/web/cohorts",
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(ADMIN_EMAIL, ADMIN_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void studentGradesPage_returns200() {
    var res =
        testRestTemplate.exchange(
            "/web/student/grades",
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(STUDENT_A_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void teacherCoursesPage_returns200() {
    var res =
        testRestTemplate.exchange(
            "/web/teacher/courses",
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(TEACHER_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void webLogout_returns200() {
    var res =
        testRestTemplate.postForEntity(
            "/auth/logout",
            new HttpEntity<>(utils.sessionHeaders(ADMIN_EMAIL, ADMIN_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
