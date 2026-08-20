package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.GroupEnrollment;
import com.example.demo.model.StudentGroup;
import com.example.demo.model.TeacherAssignment;
import com.example.demo.model.Track;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class GroupDtos {

  private GroupDtos() {}

  public record CreateGroupRequest(
      @NotBlank String reference, Track track, @NotNull UUID cohortId) {}

  public record GroupResponse(UUID id, String reference, Track track, UUID cohortId) {
    public static GroupResponse from(StudentGroup g) {
      return new GroupResponse(g.getId(), g.getReference(), g.getTrack(), g.getCohort().getId());
    }
  }

  public record CreateEnrollmentRequest(@NotNull UUID groupId, @NotNull Instant startDate) {}

  public record GroupEnrollmentResponse(UUID id, UUID groupId, Instant startDate, Instant endDate) {
    public static GroupEnrollmentResponse from(GroupEnrollment e) {
      return new GroupEnrollmentResponse(
          e.getId(), e.getGroup().getId(), e.getStartDate(), e.getEndDate());
    }
  }

  public record CreateAssignmentRequest(
      @NotNull UUID teacherId,
      @NotNull UUID courseId,
      @NotNull UUID groupId,
      @NotNull UUID semesterId) {}

  public record AssignmentResponse(
      UUID id, UUID teacherId, UUID courseId, UUID groupId, UUID semesterId) {
    public static AssignmentResponse from(TeacherAssignment a) {
      return new AssignmentResponse(
          a.getId(),
          a.getTeacher().getId(),
          a.getCourse().getId(),
          a.getGroup().getId(),
          a.getSemester().getId());
    }
  }
}
