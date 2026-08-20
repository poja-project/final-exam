package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class RoleBasedSuccessHandlerTest {

  private final RoleBasedSuccessHandler handler = new RoleBasedSuccessHandler();

  @Test
  void adminRedirectsToCohorts() throws IOException {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();
    var auth =
        new UsernamePasswordAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    handler.onAuthenticationSuccess(request, response, auth);

    assertThat(response.getRedirectedUrl()).isEqualTo("/web/cohorts");
  }

  @Test
  void teacherRedirectsToCourses() throws IOException {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();
    var auth =
        new UsernamePasswordAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority("ROLE_TEACHER")));

    handler.onAuthenticationSuccess(request, response, auth);

    assertThat(response.getRedirectedUrl()).isEqualTo("/web/teacher/courses");
  }

  @Test
  void studentRedirectsToGrades() throws IOException {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();
    var auth =
        new UsernamePasswordAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));

    handler.onAuthenticationSuccess(request, response, auth);

    assertThat(response.getRedirectedUrl()).isEqualTo("/web/student/grades");
  }
}
