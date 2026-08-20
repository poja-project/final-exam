package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.demo.exception.DomainException;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import com.example.demo.repository.TeacherRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessGuardTest {

  @Mock private CurrentUserService currentUserService;
  @Mock private StudentRepository studentRepository;
  @Mock private TeacherRepository teacherRepository;
  @Mock private TeacherAssignmentRepository teacherAssignmentRepository;

  @InjectMocks private AccessGuard accessGuard;

  private User userWithRole(Role role) {
    User u = new User();
    u.setId(UUID.randomUUID());
    u.setRole(role);
    return u;
  }

  @Test
  void requireAdmin_succeeds_forAdmin() {
    User admin = userWithRole(Role.ADMIN);
    when(currentUserService.requireUser()).thenReturn(admin);

    assertDoesNotThrow(() -> accessGuard.requireAdmin());
  }

  @Test
  void requireAdmin_throwsForbidden_forNonAdmin() {
    when(currentUserService.requireUser()).thenReturn(userWithRole(Role.TEACHER));

    DomainException ex = assertThrows(DomainException.class, () -> accessGuard.requireAdmin());
    assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, ex.getStatus());
  }

  @Test
  void ensureSelfOrAdmin_succeeds_forAdmin_regardlessOfTargetStudent() {
    when(currentUserService.requireUser()).thenReturn(userWithRole(Role.ADMIN));

    assertDoesNotThrow(() -> accessGuard.ensureSelfOrAdmin(UUID.randomUUID()));
  }

  @Test
  void ensureSelfOrAdmin_succeeds_whenStudentAccessesTheirOwnData() {
    User user = userWithRole(Role.STUDENT);
    when(currentUserService.requireUser()).thenReturn(user);
    Student self = new Student();
    UUID studentId = UUID.randomUUID();
    self.setId(studentId);
    when(studentRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(self));

    assertDoesNotThrow(() -> accessGuard.ensureSelfOrAdmin(studentId));
  }

  @Test
  void ensureSelfOrAdmin_throwsForbidden_whenStudentAccessesSomeoneElsesData() {
    User user = userWithRole(Role.STUDENT);
    when(currentUserService.requireUser()).thenReturn(user);
    Student self = new Student();
    self.setId(UUID.randomUUID());
    when(studentRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(self));

    UUID someoneElseId = UUID.randomUUID();
    assertThrows(DomainException.class, () -> accessGuard.ensureSelfOrAdmin(someoneElseId));
  }

  @Test
  void ensureSelfOrAdmin_throwsForbidden_forTeacher() {
    when(currentUserService.requireUser()).thenReturn(userWithRole(Role.TEACHER));

    assertThrows(DomainException.class, () -> accessGuard.ensureSelfOrAdmin(UUID.randomUUID()));
  }

  @Test
  void ensureAdminOrTeacherOfCourse_succeeds_forAdmin() {
    when(currentUserService.requireUser()).thenReturn(userWithRole(Role.ADMIN));

    assertDoesNotThrow(
        () -> accessGuard.ensureAdminOrTeacherOfCourse(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void ensureAdminOrTeacherOfCourse_succeeds_whenTeacherIsAssignedForThatExactSemester() {
    User user = userWithRole(Role.TEACHER);
    when(currentUserService.requireUser()).thenReturn(user);
    Teacher teacher = new Teacher();
    teacher.setId(UUID.randomUUID());
    when(teacherRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(teacher));

    UUID courseId = UUID.randomUUID();
    UUID semesterId = UUID.randomUUID();
    when(teacherAssignmentRepository.existsByTeacher_IdAndCourse_IdAndSemester_Id(
            teacher.getId(), courseId, semesterId))
        .thenReturn(true);

    assertDoesNotThrow(() -> accessGuard.ensureAdminOrTeacherOfCourse(courseId, semesterId));
  }

  @Test
  void ensureAdminOrTeacherOfCourse_throwsForbidden_whenTeacherIsAssignedForADifferentSemester() {
    User user = userWithRole(Role.TEACHER);
    when(currentUserService.requireUser()).thenReturn(user);
    Teacher teacher = new Teacher();
    teacher.setId(UUID.randomUUID());
    when(teacherRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(teacher));

    UUID courseId = UUID.randomUUID();
    UUID otherSemesterId = UUID.randomUUID();
    when(teacherAssignmentRepository.existsByTeacher_IdAndCourse_IdAndSemester_Id(
            teacher.getId(), courseId, otherSemesterId))
        .thenReturn(false);

    assertThrows(
        DomainException.class,
        () -> accessGuard.ensureAdminOrTeacherOfCourse(courseId, otherSemesterId));
  }

  @Test
  void ensureAdminOrTeacherOfCourse_throwsForbidden_forStudent() {
    when(currentUserService.requireUser()).thenReturn(userWithRole(Role.STUDENT));

    assertThrows(
        DomainException.class,
        () -> accessGuard.ensureAdminOrTeacherOfCourse(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void requireStudentProfile_returnsTheStudent_whenRoleMatches() {
    User user = userWithRole(Role.STUDENT);
    when(currentUserService.requireUser()).thenReturn(user);
    Student student = new Student();
    when(studentRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(student));

    assertEquals(student, accessGuard.requireStudentProfile());
  }

  @Test
  void requireStudentProfile_throwsForbidden_forNonStudent() {
    when(currentUserService.requireUser()).thenReturn(userWithRole(Role.TEACHER));

    assertThrows(DomainException.class, () -> accessGuard.requireStudentProfile());
  }

  @Test
  void requireTeacherProfile_returnsTheTeacher_whenRoleMatches() {
    User user = userWithRole(Role.TEACHER);
    when(currentUserService.requireUser()).thenReturn(user);
    Teacher teacher = new Teacher();
    when(teacherRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(teacher));

    assertEquals(teacher, accessGuard.requireTeacherProfile());
  }

  @Test
  void requireTeacherProfile_throwsForbidden_forNonTeacher() {
    when(currentUserService.requireUser()).thenReturn(userWithRole(Role.STUDENT));

    assertThrows(DomainException.class, () -> accessGuard.requireTeacherProfile());
  }
}
