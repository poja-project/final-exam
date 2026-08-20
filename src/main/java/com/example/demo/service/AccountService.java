package com.example.demo.service;

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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final UserRepository userRepository;
  private final TeacherRepository teacherRepository;
  private final StudentRepository studentRepository;
  private final CohortRepository cohortRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public Teacher createTeacher(
      String lastName, String firstName, String email, String rawPassword) {
    User user = createUserAccount(email, rawPassword, Role.TEACHER);
    Teacher teacher = new Teacher();
    teacher.setLastName(lastName);
    teacher.setFirstName(firstName);
    teacher.setUser(user);
    return teacherRepository.save(teacher);
  }

  @Transactional
  public Student createStudent(
      String lastName, String firstName, String email, String rawPassword, UUID cohortId) {
    Cohort cohort =
        cohortRepository
            .findById(cohortId)
            .orElseThrow(() -> DomainException.notFound("Cohort not found: " + cohortId));

    User user = createUserAccount(email, rawPassword, Role.STUDENT);

    Student student = new Student();
    student.setLastName(lastName);
    student.setFirstName(firstName);
    student.setUser(user);
    student.setCohort(cohort);
    student.setStudentNumber(generateStudentNumber(cohort));
    return studentRepository.save(student);
  }

  private User createUserAccount(String email, String rawPassword, Role role) {
    if (userRepository.existsByEmail(email)) {
      throw DomainException.conflict("An account with this email already exists: " + email);
    }
    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setRole(role);
    return userRepository.save(user);
  }

  private String generateStudentNumber(Cohort cohort) {
    // student_number is globally UNIQUE; do not key only on cohort-local count
    long seq = studentRepository.count() + 1;
    String candidate = "STD-%d-%04d".formatted(cohort.getEntryYear(), seq);
    while (studentRepository.existsByStudentNumber(candidate)) {
      seq++;
      candidate = "STD-%d-%04d".formatted(cohort.getEntryYear(), seq);
    }
    return candidate;
  }
}
