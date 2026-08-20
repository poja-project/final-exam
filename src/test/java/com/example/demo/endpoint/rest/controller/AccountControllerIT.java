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

class AccountControllerIT extends FacadeIT {

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
  void createTeacher_returns201() throws Exception {
    var body =
        Map.of(
            "lastName",
            "Dupont",
            "firstName",
            "Jean",
            "email",
            "jean.dupont@prog4.local",
            "password",
            "Pass1234!");
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response = testRestTemplate.postForEntity("/admin/users/teachers", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UUID id = extractId(response.getBody());
    assertThat(id).isNotNull();
  }

  @Test
  void createStudent_returns201() throws Exception {
    var cohortRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts",
            new HttpEntity<>(
                mapper.writeValueAsString(Map.of("label", "Acct Cohort", "entryYear", 2024)),
                adminHeaders()),
            String.class);
    UUID cohortId = extractId(cohortRes.getBody());

    var body =
        Map.of(
            "lastName",
            "Martin",
            "firstName",
            "Sophie",
            "email",
            "sophie.martin@prog4.local",
            "password",
            "Pass1234!",
            "cohortId",
            cohortId.toString());
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response = testRestTemplate.postForEntity("/admin/users/students", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UUID id = extractId(response.getBody());
    assertThat(id).isNotNull();
    JsonNode node = mapper.readTree(response.getBody());
    assertThat(node.get("studentNumber").asText()).isNotBlank();
    assertThat(node.get("cohortId").asText()).isEqualTo(cohortId.toString());
  }

  @Test
  void createStudent_duplicateEmail_returns409() throws Exception {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    var cohortRes =
        testRestTemplate.postForEntity(
            "/admin/cohorts",
            new HttpEntity<>(
                mapper.writeValueAsString(
                    Map.of("label", "Dup Cohort " + suffix, "entryYear", 2024)),
                adminHeaders()),
            String.class);
    assertThat(cohortRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UUID cohortId = extractId(cohortRes.getBody());

    var body =
        Map.of(
            "lastName",
            "Test",
            "firstName",
            "Dup",
            "email",
            "dup.test." + suffix + "@prog4.local",
            "password",
            "Pass1234!",
            "cohortId",
            cohortId.toString());
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var first = testRestTemplate.postForEntity("/admin/users/students", request, String.class);
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var response =
        testRestTemplate.postForEntity(
            "/admin/users/students",
            new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders()),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void listUsers_returns200() {
    var request = new HttpEntity<>(adminHeaders());
    var response = testRestTemplate.exchange("/admin/users", HttpMethod.GET, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotBlank();
  }

  @Test
  void listUsers_withRoleFilter_returns200() {
    var request = new HttpEntity<>(adminHeaders());
    var response =
        testRestTemplate.exchange("/admin/users?role=ADMIN", HttpMethod.GET, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void createTeacher_validationFails_returns400() throws Exception {
    var body = Map.of("lastName", "Test");
    var request = new HttpEntity<>(mapper.writeValueAsString(body), adminHeaders());
    var response = testRestTemplate.postForEntity("/admin/users/teachers", request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
