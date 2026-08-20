package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.exception.DomainException;
import com.example.demo.model.Course;
import com.example.demo.model.Exam;
import com.example.demo.model.Semester;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.security.AccessGuard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

  @Mock private ExamRepository examRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private SemesterRepository semesterRepository;
  @Mock private AccessGuard accessGuard;

  @InjectMocks private ExamService examService;

  private final UUID courseId = UUID.randomUUID();
  private final UUID semesterId = UUID.randomUUID();

  private void stubCourseAndSemesterFound() {
    Course course = new Course();
    course.setId(courseId);
    Semester semester = new Semester();
    semester.setId(semesterId);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
  }

  @Test
  void createExams_succeeds_whenCoefficientsSumToExactlyOne() {
    stubCourseAndSemesterFound();
    when(examRepository.findByCourse_IdAndSemester_Id(courseId, semesterId)).thenReturn(List.of());
    when(examRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    List<ExamService.ExamRequest> requests =
        List.of(
            new ExamService.ExamRequest(Instant.now(), new BigDecimal("0.5")),
            new ExamService.ExamRequest(Instant.now(), new BigDecimal("0.5")));

    List<Exam> result = examService.createExams(courseId, semesterId, requests);

    assertEquals(2, result.size());
  }

  @Test
  void createExams_rejectsPartialCoefficientSum() {
    stubCourseAndSemesterFound();
    when(examRepository.findByCourse_IdAndSemester_Id(courseId, semesterId)).thenReturn(List.of());

    List<ExamService.ExamRequest> requests =
        List.of(new ExamService.ExamRequest(Instant.now(), new BigDecimal("0.7")));

    assertThrows(
        DomainException.class, () -> examService.createExams(courseId, semesterId, requests));
  }

  @Test
  void createExams_normalizesPercentCoefficients() {
    stubCourseAndSemesterFound();
    when(examRepository.findByCourse_IdAndSemester_Id(courseId, semesterId)).thenReturn(List.of());
    when(examRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    List<ExamService.ExamRequest> requests =
        List.of(
            new ExamService.ExamRequest(Instant.now(), new BigDecimal("60")),
            new ExamService.ExamRequest(Instant.now(), new BigDecimal("40")));

    List<Exam> result = examService.createExams(courseId, semesterId, requests);

    BigDecimal totalCoeff =
        result.stream().map(Exam::getCoeff).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, BigDecimal.ONE.compareTo(totalCoeff));
  }

  @Test
  void createExams_rejectsWhenExamsAlreadyExistForThisCourseAndSemester() {
    stubCourseAndSemesterFound();
    when(examRepository.findByCourse_IdAndSemester_Id(courseId, semesterId))
        .thenReturn(List.of(new Exam()));

    List<ExamService.ExamRequest> requests =
        List.of(new ExamService.ExamRequest(Instant.now(), new BigDecimal("1.0")));

    DomainException ex =
        assertThrows(
            DomainException.class, () -> examService.createExams(courseId, semesterId, requests));
    assertEquals(org.springframework.http.HttpStatus.CONFLICT, ex.getStatus());
  }

  @Test
  void createExams_rejectsEmptyRequestList() {
    stubCourseAndSemesterFound();

    assertThrows(
        DomainException.class, () -> examService.createExams(courseId, semesterId, List.of()));
  }

  @Test
  void createExams_withUnknownCourse_throwsNotFound() {
    when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

    assertThrows(
        DomainException.class,
        () ->
            examService.createExams(
                courseId,
                semesterId,
                List.of(new ExamService.ExamRequest(Instant.now(), BigDecimal.ONE))));
  }
}
