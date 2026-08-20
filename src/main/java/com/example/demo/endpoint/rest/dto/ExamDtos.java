package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.Exam;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ExamDtos {

  private ExamDtos() {}

  public record ExamItem(@NotNull Instant date, @NotNull BigDecimal coeff) {}

  public record CreateExamsRequest(
      @NotNull UUID courseId, @NotNull UUID semesterId, @NotEmpty List<ExamItem> exams) {}

  public record ExamResponse(
      UUID id, UUID courseId, UUID semesterId, Instant date, BigDecimal coeff) {
    public static ExamResponse from(Exam e) {
      return new ExamResponse(
          e.getId(), e.getCourse().getId(), e.getSemester().getId(), e.getDate(), e.getCoeff());
    }
  }
}
