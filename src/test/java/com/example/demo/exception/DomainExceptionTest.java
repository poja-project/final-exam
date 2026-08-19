package com.example.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DomainExceptionTest {

  @Test
  void badRequest_setsStatus400() {
    DomainException ex = DomainException.badRequest("bad");
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    assertEquals("bad", ex.getMessage());
  }

  @Test
  void forbidden_setsStatus403() {
    assertEquals(HttpStatus.FORBIDDEN, DomainException.forbidden("nope").getStatus());
  }

  @Test
  void notFound_setsStatus404() {
    assertEquals(HttpStatus.NOT_FOUND, DomainException.notFound("missing").getStatus());
  }

  @Test
  void conflict_setsStatus409() {
    assertEquals(HttpStatus.CONFLICT, DomainException.conflict("dup").getStatus());
  }

  @Test
  void unprocessable_setsStatus422() {
    assertEquals(
        HttpStatus.UNPROCESSABLE_ENTITY, DomainException.unprocessable("invalid").getStatus());
  }
}
