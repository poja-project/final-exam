package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

class GraduateControllerIT extends FacadeIT {
  @org.springframework.boot.test.mock.mockito.MockBean
  private com.example.demo.file.bucket.BucketComponent bucketComponent;

  private HttpHeaders adminHeaders() {
    return utils.sessionHeaders(ADMIN_EMAIL, ADMIN_PASSWORD);
  }

  @Test
  void listGraduates_returns200() {
    UUID cohortId = UUID.fromString(COHORT_ID);
    var res =
        testRestTemplate.exchange(
            "/cohorts/" + cohortId + "/graduates?track=TN",
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void listGraduates_EL_track_returns200() {
    UUID cohortId = UUID.fromString(COHORT_ID);
    var res =
        testRestTemplate.exchange(
            "/cohorts/" + cohortId + "/graduates?track=EL",
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void exportGraduates_returns201() {
    UUID cohortId = UUID.fromString(COHORT_ID);
    var res =
        testRestTemplate.postForEntity(
            "/cohorts/" + cohortId + "/graduates/export?track=TN",
            new HttpEntity<>(adminHeaders()),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void studentCannotAccessGraduates() {
    UUID cohortId = UUID.fromString(COHORT_ID);
    var res =
        testRestTemplate.exchange(
            "/cohorts/" + cohortId + "/graduates?track=TN",
            HttpMethod.GET,
            new HttpEntity<>(utils.sessionHeaders(STUDENT_A_EMAIL, SEEDED_PASSWORD)),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
