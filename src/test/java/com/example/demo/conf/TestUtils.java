package com.example.demo.conf;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;

@AllArgsConstructor
public class TestUtils {

  public static final String ADMIN_EMAIL = "admin@prog4.local";
  public static final String ADMIN_PASSWORD = "ChangeMoi123!";

  public static final String TEACHER_EMAIL = "teacher-it@prog4.local";
  public static final String STUDENT_A_EMAIL = "student-it-a@prog4.local";
  public static final String STUDENT_B_EMAIL = "student-it-b@prog4.local";
  public static final String SEEDED_PASSWORD = "TestPass123!";

  public static final String STUDENT_A_ID = "00000000-0000-0000-0000-000000000006";
  public static final String STUDENT_B_ID = "00000000-0000-0000-0000-000000000007";
  public static final String COHORT_ID = "00000000-0000-0000-0000-000000000001";

  public static final String LOGIN_URL = "/auth/login";
  public static final String ADMIN_USERS_URL = "/admin/users";
  public static final String GRADES_URL = "/grades";
  public static final String STUDENT_GRADES_URL = "/students/{id}/grades";
  public static final String EXAMS_URL = "/exams";

  private final TestRestTemplate testRestTemplate;

  public String sessionCookieOf(String email, String password) {
    Map<String, String> credentials = new LinkedHashMap<>();
    credentials.put("email", email);
    credentials.put("password", password);

    var response = testRestTemplate.postForEntity(LOGIN_URL, credentials, Void.class);
    return response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
  }

  public HttpHeaders sessionHeaders(String email, String password) {
    var headers = jsonHeaders();
    headers.add(HttpHeaders.COOKIE, sessionCookieOf(email, password));
    return headers;
  }

  public static HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
