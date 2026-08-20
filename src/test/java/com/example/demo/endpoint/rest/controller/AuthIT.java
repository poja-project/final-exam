package com.example.demo.endpoint.rest.controller;

import static com.example.demo.conf.TestUtils.ADMIN_EMAIL;
import static com.example.demo.conf.TestUtils.ADMIN_PASSWORD;
import static com.example.demo.conf.TestUtils.ADMIN_USERS_URL;
import static com.example.demo.conf.TestUtils.LOGIN_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.example.demo.conf.FacadeIT;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

class AuthIT extends FacadeIT {

  @Test
  void unauthenticated_api_call_is_rejected() {
    var response = testRestTemplate.getForEntity(ADMIN_USERS_URL, Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE));
  }

  @Test
  void bootstrap_admin_can_login_and_reuse_session_cookie() {
    Map<String, String> credentials = new LinkedHashMap<>();
    credentials.put("email", ADMIN_EMAIL);
    credentials.put("password", ADMIN_PASSWORD);

    var loginResponse = testRestTemplate.postForEntity(LOGIN_URL, credentials, Void.class);
    assertEquals(OK, loginResponse.getStatusCode());

    String cookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertNotNull(cookie);

    var headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, cookie);
    var response =
        testRestTemplate.exchange(ADMIN_USERS_URL, GET, new HttpEntity<>(headers), String.class);

    assertEquals(OK, response.getStatusCode());
  }

  @Test
  void bootstrap_admin_can_login_via_basic_auth_without_session() {
    var response =
        testRestTemplate
            .withBasicAuth(ADMIN_EMAIL, ADMIN_PASSWORD)
            .getForEntity(ADMIN_USERS_URL, String.class);

    assertEquals(OK, response.getStatusCode());
  }
}
