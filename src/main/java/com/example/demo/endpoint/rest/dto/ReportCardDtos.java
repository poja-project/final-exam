package com.example.demo.endpoint.rest.dto;

import com.example.demo.service.dto.CourseAverageResult;
import com.example.demo.service.dto.ReportCardResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ReportCardDtos {

  private ReportCardDtos() {}

  public record CourseItem(
      UUID courseId, String courseTitle, BigDecimal average, Integer credit, String status) {
    public static CourseItem from(CourseAverageResult c) {
      return new CourseItem(
          c.courseId(),
          c.courseTitle(),
          c.average(),
          c.credit(),
          c.complete() ? "COMPLETED" : "INCOMPLETE");
    }
  }

  public record ReportCardResponse(
      UUID studentId,
      UUID schoolYearId,
      List<CourseItem> courses,
      BigDecimal generalAverage,
      int creditsEarned,
      String globalStatus) {
    public static ReportCardResponse from(ReportCardResult r) {
      return new ReportCardResponse(
          r.studentId(),
          r.schoolYearId(),
          r.courses().stream().map(CourseItem::from).toList(),
          r.overallAverage(),
          r.creditsEarned(),
          r.complete() ? "COMPLETED" : "INCOMPLETE");
    }
  }
}
