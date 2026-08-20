package com.example.demo.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

class AuthControllerIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void unauthenticatedApiCall_returnsBasicAuthChallenge() {
    ResponseEntity<String> response = restTemplate.getForEntity("/admin/users", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE)).isNotNull();
  }

  @Test
  void bootstrapAdmin_canLoginViaJsonEndpoint_andReuseSessionCookie() {
    Map<String, String> credentials = new LinkedHashMap<>();
    credentials.put("email", "admin@prog4.local");
    credentials.put("password", "ChangeMoi123!");

    ResponseEntity<Void> loginResponse =
        restTemplate.postForEntity("/auth/login", credentials, Void.class);
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(sessionCookie).isNotNull();

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, sessionCookie);
    ResponseEntity<String> usersResponse =
        restTemplate.exchange(
            "/admin/users", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(usersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void bootstrapAdmin_canLoginViaBasicAuth_withoutSession() {
    ResponseEntity<String> response =
        restTemplate
            .withBasicAuth("admin@prog4.local", "ChangeMoi123!")
            .getForEntity("/admin/users", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
