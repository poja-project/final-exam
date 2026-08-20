package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.Cohort;
import com.example.demo.model.Course;
import com.example.demo.model.CourseOffering;
import com.example.demo.model.SchoolYear;
import com.example.demo.model.Semester;
import com.example.demo.model.Track;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public final class AcademicReferenceDtos {

  private AcademicReferenceDtos() {}

  public record CreateCohortRequest(@NotBlank String label, @NotNull Integer entryYear) {}

  public record CohortResponse(UUID id, String label, Integer entryYear) {
    public static CohortResponse from(Cohort c) {
      return new CohortResponse(c.getId(), c.getLabel(), c.getEntryYear());
    }
  }

  public record CreateSchoolYearRequest(
      @NotBlank String label, @NotNull @Min(1) @Max(3) Integer yearNumber) {}

  public record SchoolYearResponse(UUID id, String label, Integer yearNumber, UUID cohortId) {
    public static SchoolYearResponse from(SchoolYear y) {
      return new SchoolYearResponse(
          y.getId(), y.getLabel(), y.getYearNumber(), y.getCohort().getId());
    }
  }

  public record CreateSemesterRequest(
      @NotNull @Min(1) @Max(6) Integer number,
      @NotNull LocalDate startDate,
      @NotNull LocalDate endDate) {}

  public record SemesterResponse(
      UUID id, Integer number, UUID schoolYearId, LocalDate startDate, LocalDate endDate) {
    public static SemesterResponse from(Semester s) {
      return new SemesterResponse(
          s.getId(), s.getNumber(), s.getSchoolYear().getId(), s.getStartDate(), s.getEndDate());
    }
  }

  public record CreateCourseRequest(
      @NotBlank String reference, @NotBlank String title, @NotNull @Min(1) Integer credit) {}

  public record CourseResponse(UUID id, String reference, String title, Integer credit) {
    public static CourseResponse from(Course c) {
      return new CourseResponse(c.getId(), c.getReference(), c.getTitle(), c.getCredit());
    }
  }

  public record CreateCourseOfferingRequest(@NotNull UUID courseId, Track track) {}

  public record CourseOfferingResponse(UUID id, UUID courseId, UUID semesterId, Track track) {
    public static CourseOfferingResponse from(CourseOffering o) {
      return new CourseOfferingResponse(
          o.getId(), o.getCourse().getId(), o.getSemester().getId(), o.getTrack());
    }
  }
}
