package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.exception.DomainException;
import com.example.demo.model.Cohort;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repository.CohortRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private TeacherRepository teacherRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private CohortRepository cohortRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AccountService accountService;

  @Test
  void createTeacher_encodesPassword_andSetsTeacherRole() {
    when(userRepository.existsByEmail("teacher@prog4.local")).thenReturn(false);
    when(passwordEncoder.encode("Pass1234!")).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));

    Teacher teacher =
        accountService.createTeacher("Dupont", "Jean", "teacher@prog4.local", "Pass1234!");

    assertEquals("Dupont", teacher.getLastName());
    assertEquals(Role.TEACHER, teacher.getUser().getRole());
    assertEquals("hashed", teacher.getUser().getPassword());
  }

  @Test
  void createTeacher_withExistingEmail_throwsConflict() {
    when(userRepository.existsByEmail("taken@prog4.local")).thenReturn(true);

    DomainException ex =
        assertThrows(
            DomainException.class,
            () -> accountService.createTeacher("A", "B", "taken@prog4.local", "Pass1234!"));

    assertEquals(org.springframework.http.HttpStatus.CONFLICT, ex.getStatus());
  }

  @Test
  void createStudent_generatesSequentialStudentNumber_basedOnCohortEntryYearAndExistingCount() {
    Cohort cohort = new Cohort();
    cohort.setId(UUID.randomUUID());
    cohort.setEntryYear(2023);
    when(cohortRepository.findById(cohort.getId())).thenReturn(Optional.of(cohort));
    when(userRepository.existsByEmail("student@prog4.local")).thenReturn(false);
    when(passwordEncoder.encode("Pass1234!")).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(studentRepository.findByCohort_Id(cohort.getId()))
        .thenReturn(List.of(new Student(), new Student()));
    when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

    Student student =
        accountService.createStudent(
            "Martin", "Sophie", "student@prog4.local", "Pass1234!", cohort.getId());

    assertEquals("STD-2023-0003", student.getStudentNumber());
    assertEquals(Role.STUDENT, student.getUser().getRole());
  }

  @Test
  void createStudent_withUnknownCohort_throwsNotFound() {
    UUID unknownCohortId = UUID.randomUUID();
    when(cohortRepository.findById(unknownCohortId)).thenReturn(Optional.empty());

    DomainException ex =
        assertThrows(
            DomainException.class,
            () ->
                accountService.createStudent(
                    "A", "B", "x@prog4.local", "Pass1234!", unknownCohortId));

    assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatus());
  }
}
