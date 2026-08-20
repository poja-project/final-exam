package com.example.demo.endpoint.rest.dto;

import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public final class AccountDtos {

  private AccountDtos() {}

  public record CreateTeacherRequest(
      @NotBlank String lastName,
      @NotBlank String firstName,
      @NotBlank @Email String email,
      @NotBlank String password) {}

  public record TeacherResponse(UUID id, String lastName, String firstName, String email) {
    public static TeacherResponse from(Teacher t) {
      return new TeacherResponse(
          t.getId(), t.getLastName(), t.getFirstName(), t.getUser().getEmail());
    }
  }

  public record CreateStudentRequest(
      @NotBlank String lastName,
      @NotBlank String firstName,
      @NotBlank @Email String email,
      @NotBlank String password,
      @NotNull UUID cohortId) {}

  public record StudentResponse(
      UUID id,
      String studentNumber,
      String lastName,
      String firstName,
      String email,
      UUID cohortId) {
    public static StudentResponse from(Student s) {
      return new StudentResponse(
          s.getId(),
          s.getStudentNumber(),
          s.getLastName(),
          s.getFirstName(),
          s.getUser().getEmail(),
          s.getCohort().getId());
    }
  }

  public record UserResponse(UUID id, String email, Role role) {
    public static UserResponse from(User u) {
      return new UserResponse(u.getId(), u.getEmail(), u.getRole());
    }
  }
}
