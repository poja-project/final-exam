package com.example.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {

  private final HttpStatus status;

  public DomainException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

    public static DomainException badRequest(String message) {
    return new DomainException(HttpStatus.BAD_REQUEST, message);
  }

  public static DomainException forbidden(String message) {
    return new DomainException(HttpStatus.FORBIDDEN, message);
  }

  public static DomainException notFound(String message) {
    return new DomainException(HttpStatus.NOT_FOUND, message);
  }

  public static DomainException conflict(String message) {
    return new DomainException(HttpStatus.CONFLICT, message);
  }

  public static DomainException unprocessable(String message) {
    return new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, message);
  }
}
