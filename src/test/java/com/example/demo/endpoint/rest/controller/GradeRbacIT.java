package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.SEEDED_PASSWORD;
import static com.example.demo.conf.TestUtils.STUDENT_A_EMAIL;
import static com.example.demo.conf.TestUtils.STUDENT_A_ID;
import static com.example.demo.conf.TestUtils.STUDENT_B_EMAIL;
import static com.example.demo.conf.TestUtils.STUDENT_GRADES_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import com.example.demo.conf.FacadeIT;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;

class GradeRbacIT extends FacadeIT {

  @Test
  void student_can_view_their_own_grades() {
    var headers = utils.sessionHeaders(STUDENT_A_EMAIL, SEEDED_PASSWORD);

    var response =
        testRestTemplate.exchange(
            STUDENT_GRADES_URL, GET, new HttpEntity<>(headers), Object[].class, STUDENT_A_ID);

    assertEquals(OK, response.getStatusCode());
  }

  @Test
  void student_cannot_view_another_students_grades() {
    var headers = utils.sessionHeaders(STUDENT_B_EMAIL, SEEDED_PASSWORD);

    var response =
        testRestTemplate.exchange(
            STUDENT_GRADES_URL, GET, new HttpEntity<>(headers), Map.class, STUDENT_A_ID);

    assertEquals(FORBIDDEN, response.getStatusCode());
  }
}
