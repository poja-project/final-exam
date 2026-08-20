package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.demo.model.Course;
import com.example.demo.model.Exam;
import com.example.demo.model.Grade;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.service.dto.CourseAverageResult;
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
class CourseAverageCalculatorTest {

  @Mock private ExamRepository examRepository;
  @Mock private GradeRepository gradeRepository;

  @InjectMocks private CourseAverageCalculator calculator;

  private final UUID studentId = UUID.randomUUID();
  private final UUID semesterId = UUID.randomUUID();

  private Course course(int credit) {
    Course c = new Course();
    c.setId(UUID.randomUUID());
    c.setTitle("Algorithms");
    c.setCredit(credit);
    return c;
  }

  private Exam exam(BigDecimal coeff) {
    Exam e = new Exam();
    e.setId(UUID.randomUUID());
    e.setCoeff(coeff);
    return e;
  }

  private Grade grade(BigDecimal value) {
    Grade g = new Grade();
    g.setValue(value);
    return g;
  }

  @Test
  void computesWeightedAverage_whenAllExamsAreGraded() {
    Course course = course(6);
    Exam midterm = exam(new BigDecimal("0.50"));
    Exam finalExam = exam(new BigDecimal("0.50"));

    when(examRepository.findByCourse_IdAndSemester_Id(course.getId(), semesterId))
        .thenReturn(List.of(midterm, finalExam));
    when(gradeRepository.findCurrent(studentId, midterm.getId()))
        .thenReturn(Optional.of(grade(new BigDecimal("12"))));
    when(gradeRepository.findCurrent(studentId, finalExam.getId()))
        .thenReturn(Optional.of(grade(new BigDecimal("8"))));

    CourseAverageResult result = calculator.computeAverage(studentId, course, semesterId);

    assertTrue(result.complete());
    assertEquals(0, new BigDecimal("10.00").compareTo(result.average()));
    assertEquals(6, result.credit());
  }

  @Test
  void flagsIncomplete_whenOneExamHasNoGradeYet() {
    Course course = course(6);
    Exam midterm = exam(new BigDecimal("0.50"));
    Exam finalExam = exam(new BigDecimal("0.50"));

    when(examRepository.findByCourse_IdAndSemester_Id(course.getId(), semesterId))
        .thenReturn(List.of(midterm, finalExam));
    when(gradeRepository.findCurrent(studentId, midterm.getId()))
        .thenReturn(Optional.of(grade(new BigDecimal("12"))));
    when(gradeRepository.findCurrent(studentId, finalExam.getId())).thenReturn(Optional.empty());

    CourseAverageResult result = calculator.computeAverage(studentId, course, semesterId);

    assertFalse(result.complete());
    assertNull(result.average());
  }

  @Test
  void isIncomplete_whenNoExamIsDefinedYet() {
    Course course = course(6);
    when(examRepository.findByCourse_IdAndSemester_Id(course.getId(), semesterId))
        .thenReturn(List.of());

    CourseAverageResult result = calculator.computeAverage(studentId, course, semesterId);

    assertFalse(result.complete());
    assertNull(result.average());
  }

  @Test
  void usesTheMostRecentGrade_whenAGradeWasCorrected() {
    Course course = course(4);
    Exam exam = exam(new BigDecimal("1.0"));
    when(examRepository.findByCourse_IdAndSemester_Id(course.getId(), semesterId))
        .thenReturn(List.of(exam));
    when(gradeRepository.findCurrent(studentId, exam.getId()))
        .thenReturn(Optional.of(grade(new BigDecimal("16"))));

    CourseAverageResult result = calculator.computeAverage(studentId, course, semesterId);

    assertEquals(0, new BigDecimal("16.00").compareTo(result.average()));
  }
}
