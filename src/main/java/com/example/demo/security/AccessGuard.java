package com.example.demo.security;

import com.example.demo.exception.DomainException;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import com.example.demo.repository.TeacherRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AccessGuard {

  private final CurrentUserService currentUserService;
  private final StudentRepository studentRepository;
  private final TeacherRepository teacherRepository;
  private final TeacherAssignmentRepository teacherAssignmentRepository;

  public User requireAdmin() {
    User user = currentUserService.requireUser();
    if (user.getRole() != Role.ADMIN) {
      throw DomainException.forbidden("Admin access required");
    }
    return user;
  }

  public User requireRole(Role role) {
    User user = currentUserService.requireUser();
    if (user.getRole() != role) {
      throw DomainException.forbidden("Access denied: " + role + " only");
    }
    return user;
  }

  public User requireAdminOrTeacher() {
    User user = currentUserService.requireUser();
    if (user.getRole() != Role.ADMIN && user.getRole() != Role.TEACHER) {
      throw DomainException.forbidden("Access denied: admin or teacher required");
    }
    return user;
  }

  public void ensureSelfOrAdmin(UUID targetStudentId) {
    User user = currentUserService.requireUser();
    if (user.getRole() == Role.ADMIN) {
      return;
    }
    if (user.getRole() != Role.STUDENT) {
      throw DomainException.forbidden("Access denied: student or admin required");
    }
    Student student =
        studentRepository
            .findByUser_Id(user.getId())
            .orElseThrow(() -> DomainException.forbidden("No student profile for this account"));
    if (!student.getId().equals(targetStudentId)) {
      throw DomainException.forbidden("Access denied: you can only access your own data");
    }
  }

  public void ensureAdminOrTeacherOfCourse(UUID courseId) {
    User user = currentUserService.requireUser();
    if (user.getRole() == Role.ADMIN) {
      return;
    }
    if (user.getRole() != Role.TEACHER) {
      throw DomainException.forbidden("Access denied: admin or assigned teacher required");
    }
    Teacher teacher =
        teacherRepository
            .findByUser_Id(user.getId())
            .orElseThrow(() -> DomainException.forbidden("No teacher profile for this account"));
    if (!teacherAssignmentRepository.existsByTeacher_IdAndCourse_Id(teacher.getId(), courseId)) {
      throw DomainException.forbidden("This course is not assigned to you");
    }
  }

  public Student requireStudentProfile() {
    User user = currentUserService.requireUser();
    if (user.getRole() != Role.STUDENT) {
      throw DomainException.forbidden("Access denied: student required");
    }
    return studentRepository
        .findByUser_Id(user.getId())
        .orElseThrow(() -> DomainException.forbidden("No student profile for this account"));
  }
}
