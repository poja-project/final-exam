package com.example.demo.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleDomainException_notFound() {
    DomainException ex = DomainException.notFound("not found");
    ResponseEntity<Object> response = handler.handleDomainException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    Map<?, ?> body = (Map<?, ?>) response.getBody();
    assertThat(body.get("message")).isEqualTo("not found");
  }

  @Test
  void handleDomainException_forbidden() {
    DomainException ex = DomainException.forbidden("forbidden");
    ResponseEntity<Object> response = handler.handleDomainException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void handleDomainException_conflict() {
    DomainException ex = DomainException.conflict("conflict");
    ResponseEntity<Object> response = handler.handleDomainException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void handleDomainException_badRequest() {
    DomainException ex = DomainException.badRequest("bad request");
    ResponseEntity<Object> response = handler.handleDomainException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void handleDomainException_unprocessable() {
    DomainException ex = DomainException.unprocessable("unprocessable");
    ResponseEntity<Object> response = handler.handleDomainException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  void handleIllegalArgument() {
    IllegalArgumentException ex = new IllegalArgumentException("invalid arg");
    ResponseEntity<Object> response = handler.handleIllegalArgument(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map<?, ?> body = (Map<?, ?>) response.getBody();
    assertThat(body.get("message")).isEqualTo("invalid arg");
  }

  @Test
  void handleDataIntegrity_returns409() {
    DataIntegrityViolationException ex = new DataIntegrityViolationException("uk_email");
    ResponseEntity<Object> response = handler.handleDataIntegrity(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }
}
