package com.example.demo.endpoint.rest.dto;

import com.example.demo.service.dto.CourseAverageResult;
import com.example.demo.service.dto.ReportCardResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CohortResultsDtos {

  private CohortResultsDtos() {}

  public record CourseItem(
      UUID courseId, String courseTitle, BigDecimal average, Integer credit, String status) {
    public static CourseItem from(CourseAverageResult c) {
      return new CourseItem(
          c.courseId(),
          c.courseTitle(),
          c.average(),
          c.credit(),
          c.complete() ? "COMPLETE" : "INCOMPLETE");
    }
  }

  public record YearResult(
      Integer yearNumber,
      List<CourseItem> courses,
      BigDecimal overallAverage,
      int creditsEarned,
      String overallStatus) {
    public static YearResult from(Integer yearNumber, ReportCardResult r) {
      return new YearResult(
          yearNumber,
          r.courses().stream().map(CourseItem::from).toList(),
          r.overallAverage(),
          r.creditsEarned(),
          r.complete() ? "COMPLETE" : "INCOMPLETE");
    }
  }

  public record StudentResults(
      UUID studentId,
      String studentNumber,
      String lastName,
      String firstName,
      List<YearResult> years) {}
}
