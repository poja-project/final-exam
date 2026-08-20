package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.demo.model.Course;
import com.example.demo.model.CourseOffering;
import com.example.demo.model.Semester;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.service.dto.CourseAverageResult;
import com.example.demo.service.dto.ReportCardResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportCardServiceTest {

  @Mock private SemesterRepository semesterRepository;
  @Mock private StudentPathResolver studentPathResolver;
  @Mock private CourseAverageCalculator courseAverageCalculator;

  @InjectMocks private ReportCardService reportCardService;

  private final UUID studentId = UUID.randomUUID();
  private final UUID schoolYearId = UUID.randomUUID();

  private Semester semester(int number) {
    Semester s = new Semester();
    s.setId(UUID.randomUUID());
    s.setNumber(number);
    s.setStartDate(LocalDate.of(2023, 9, 1));
    s.setEndDate(LocalDate.of(2024, 1, 31));
    return s;
  }

  private CourseOffering offeringFor(Course course) {
    CourseOffering o = new CourseOffering();
    o.setCourse(course);
    return o;
  }

  private Course course(int credit) {
    Course c = new Course();
    c.setId(UUID.randomUUID());
    c.setTitle("Course");
    c.setCredit(credit);
    return c;
  }

  @Test
  void buildReportCard_isComplete_whenAllCoursesAcrossBothSemestersAreComplete() {
    Semester s1 = semester(1);
    Semester s2 = semester(2);
    when(semesterRepository.findBySchoolYear_Id(schoolYearId)).thenReturn(List.of(s1, s2));

    Course courseA = course(6);
    Course courseB = course(4);
    when(studentPathResolver.resolveApplicableCourses(studentId, s1))
        .thenReturn(List.of(offeringFor(courseA)));
    when(studentPathResolver.resolveApplicableCourses(studentId, s2))
        .thenReturn(List.of(offeringFor(courseB)));

    when(courseAverageCalculator.computeAverage(studentId, courseA, s1.getId()))
        .thenReturn(
            new CourseAverageResult(courseA.getId(), "A", 6, new BigDecimal("12.00"), true));
    when(courseAverageCalculator.computeAverage(studentId, courseB, s2.getId()))
        .thenReturn(new CourseAverageResult(courseB.getId(), "B", 4, new BigDecimal("8.00"), true));

    ReportCardResult result = reportCardService.buildReportCard(studentId, schoolYearId);

    assertTrue(result.complete());
    assertEquals(6, result.creditsEarned());
    assertEquals(0, new BigDecimal("10.00").compareTo(result.overallAverage()));
  }

  @Test
  void buildReportCard_isIncomplete_whenAtLeastOneCourseIsIncomplete() {
    Semester s1 = semester(1);
    when(semesterRepository.findBySchoolYear_Id(schoolYearId)).thenReturn(List.of(s1));

    Course courseA = course(6);
    when(studentPathResolver.resolveApplicableCourses(studentId, s1))
        .thenReturn(List.of(offeringFor(courseA)));
    when(courseAverageCalculator.computeAverage(studentId, courseA, s1.getId()))
        .thenReturn(new CourseAverageResult(courseA.getId(), "A", 6, null, false));

    ReportCardResult result = reportCardService.buildReportCard(studentId, schoolYearId);

    assertFalse(result.complete());
    assertNull(result.overallAverage());
    assertEquals(0, result.creditsEarned());
  }

  @Test
  void
      buildReportCard_stillCountsCreditsEarnedForPassingCourses_evenWhenOtherCoursesAreIncomplete() {
    Semester s1 = semester(1);
    when(semesterRepository.findBySchoolYear_Id(schoolYearId)).thenReturn(List.of(s1));

    Course passed = course(6);
    Course pending = course(3);
    when(studentPathResolver.resolveApplicableCourses(studentId, s1))
        .thenReturn(List.of(offeringFor(passed), offeringFor(pending)));

    when(courseAverageCalculator.computeAverage(studentId, passed, s1.getId()))
        .thenReturn(
            new CourseAverageResult(passed.getId(), "Passed", 6, new BigDecimal("14.00"), true));
    when(courseAverageCalculator.computeAverage(studentId, pending, s1.getId()))
        .thenReturn(new CourseAverageResult(pending.getId(), "Pending", 3, null, false));

    ReportCardResult result = reportCardService.buildReportCard(studentId, schoolYearId);

    assertFalse(result.complete());
    assertEquals(6, result.creditsEarned());
  }
}
