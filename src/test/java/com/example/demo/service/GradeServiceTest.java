package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.exception.DomainException;
import com.example.demo.model.Course;
import com.example.demo.model.Exam;
import com.example.demo.model.Grade;
import com.example.demo.model.Semester;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.security.AccessGuard;
import com.example.demo.security.CurrentUserService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

  @Mock private GradeRepository gradeRepository;
  @Mock private ExamRepository examRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private AccessGuard accessGuard;
  @Mock private CurrentUserService currentUserService;

  @InjectMocks private GradeService gradeService;

  private final UUID studentId = UUID.randomUUID();
  private final UUID examId = UUID.randomUUID();

  private Exam examWithCourseAndSemester() {
    Exam exam = new Exam();
    exam.setId(examId);
    Course course = new Course();
    course.setId(UUID.randomUUID());
    exam.setCourse(course);
    Semester semester = new Semester();
    semester.setId(UUID.randomUUID());
    exam.setSemester(semester);
    return exam;
  }

  @Test
  void submitGrade_succeeds_forFirstEntry_withoutARequiredReason() {
    Exam exam = examWithCourseAndSemester();
    Student student = new Student();
    student.setId(studentId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(gradeRepository.findByStudent_IdAndExam_IdOrderByEnteredAtDesc(studentId, examId))
        .thenReturn(List.of());
    when(currentUserService.requireUser()).thenReturn(new User());
    when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> inv.getArgument(0));

    Grade grade = gradeService.submitGrade(studentId, examId, new BigDecimal("15"), null);

    assertEquals(new BigDecimal("15"), grade.getValue());
  }

  @Test
  void submitGrade_requiresAReason_whenAGradeAlreadyExists() {
    Exam exam = examWithCourseAndSemester();
    Student student = new Student();
    student.setId(studentId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(gradeRepository.findByStudent_IdAndExam_IdOrderByEnteredAtDesc(studentId, examId))
        .thenReturn(List.of(new Grade()));
    when(currentUserService.requireUser()).thenReturn(new User());

    assertThrows(
        DomainException.class,
        () -> gradeService.submitGrade(studentId, examId, new BigDecimal("15"), null));
  }

  @Test
  void submitGrade_succeeds_whenReasonIsProvidedForAnExistingGrade() {
    Exam exam = examWithCourseAndSemester();
    Student student = new Student();
    student.setId(studentId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(gradeRepository.findByStudent_IdAndExam_IdOrderByEnteredAtDesc(studentId, examId))
        .thenReturn(List.of(new Grade()));
    when(currentUserService.requireUser()).thenReturn(new User());
    when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> inv.getArgument(0));

    Grade grade =
        gradeService.submitGrade(
            studentId, examId, new BigDecimal("18"), "Grade correction after appeal");

    assertEquals("Grade correction after appeal", grade.getReason());
  }

  @Test
  void submitGrade_rejectsValueAboveTwenty() {
    Exam exam = examWithCourseAndSemester();
    Student student = new Student();
    student.setId(studentId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    assertThrows(
        DomainException.class,
        () -> gradeService.submitGrade(studentId, examId, new BigDecimal("25"), null));
  }

  @Test
  void submitGrade_rejectsNegativeValue() {
    Exam exam = examWithCourseAndSemester();
    Student student = new Student();
    student.setId(studentId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    assertThrows(
        DomainException.class,
        () -> gradeService.submitGrade(studentId, examId, new BigDecimal("-1"), null));
  }

  @Test
  void submitGrade_delegatesOwnershipCheck_toAccessGuardWithCourseAndSemester() {
    Exam exam = examWithCourseAndSemester();
    Student student = new Student();
    student.setId(studentId);
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(gradeRepository.findByStudent_IdAndExam_IdOrderByEnteredAtDesc(studentId, examId))
        .thenReturn(List.of());
    when(currentUserService.requireUser()).thenReturn(new User());
    when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> inv.getArgument(0));

    gradeService.submitGrade(studentId, examId, new BigDecimal("10"), null);

    org.mockito.Mockito.verify(accessGuard)
        .ensureAdminOrTeacherOfCourse(exam.getCourse().getId(), exam.getSemester().getId());
  }

  @Test
  void getCurrentGradesForStudent_delegatesToAccessGuard() {
    gradeService.getCurrentGradesForStudent(studentId);

    org.mockito.Mockito.verify(accessGuard).ensureSelfOrAdmin(studentId);
  }
}
