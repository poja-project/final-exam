package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.Grade;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class GradeDtos {

  private GradeDtos() {}

  public record CreateGradeRequest(
      @NotNull UUID studentId, @NotNull UUID examId, @NotNull BigDecimal value, String reason) {}

  public record GradeResponse(
      UUID id,
      UUID studentId,
      UUID examId,
      BigDecimal value,
      Instant enteredAt,
      String reason,
      UUID authorId) {
    public static GradeResponse from(Grade g) {
      return new GradeResponse(
          g.getId(),
          g.getStudent().getId(),
          g.getExam().getId(),
          g.getValue(),
          g.getEnteredAt(),
          g.getReason(),
          g.getAuthor().getId());
    }
  }
}
