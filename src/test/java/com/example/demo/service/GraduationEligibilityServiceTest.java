package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.demo.model.SchoolYear;
import com.example.demo.model.Semester;
import com.example.demo.model.Student;
import com.example.demo.model.Track;
import com.example.demo.repository.SchoolYearRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.dto.CourseAverageResult;
import com.example.demo.service.dto.GraduateResult;
import com.example.demo.service.dto.ReportCardResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduationEligibilityServiceTest {

  @Mock private StudentRepository studentRepository;
  @Mock private SchoolYearRepository schoolYearRepository;
  @Mock private SemesterRepository semesterRepository;
  @Mock private StudentPathResolver studentPathResolver;
  @Mock private ReportCardService reportCardService;

  @InjectMocks private GraduationEligibilityService graduationEligibilityService;

  private final UUID cohortId = UUID.randomUUID();

  private Student student(String number, String last, String first) {
    Student s = new Student();
    s.setId(UUID.randomUUID());
    s.setStudentNumber(number);
    s.setLastName(last);
    s.setFirstName(first);
    return s;
  }

  private SchoolYear yearWithLastSemesterStart(LocalDate startDate) {
    SchoolYear year = new SchoolYear();
    year.setId(UUID.randomUUID());
    Semester last = new Semester();
    last.setNumber(6);
    last.setStartDate(startDate);
    when(semesterRepository.findBySchoolYear_Id(year.getId())).thenReturn(List.of(last));
    return year;
  }

  private CourseAverageResult passingCourse(int credit, String average) {
    return new CourseAverageResult(
        UUID.randomUUID(), "Course", credit, new BigDecimal(average), true);
  }

  @Test
  void eligibleStudent_appearsInResults_rankedByOverallAverage() {
    Student topStudent = student("STD-1", "Top", "Student");
    Student secondStudent = student("STD-2", "Second", "Student");
    when(studentRepository.findByCohort_Id(cohortId))
        .thenReturn(List.of(topStudent, secondStudent));

    LocalDate lastSemesterStart = LocalDate.of(2026, 1, 1);
    SchoolYear year3 = yearWithLastSemesterStart(lastSemesterStart);
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 3))
        .thenReturn(Optional.of(year3));
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 1))
        .thenReturn(Optional.empty());
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 2))
        .thenReturn(Optional.empty());

    Instant anchor = lastSemesterStart.atStartOfDay(ZoneOffset.UTC).toInstant();
    when(studentPathResolver.resolveTrackAt(topStudent.getId(), anchor))
        .thenReturn(Optional.of(Track.EL));
    when(studentPathResolver.resolveTrackAt(secondStudent.getId(), anchor))
        .thenReturn(Optional.of(Track.EL));

    when(reportCardService.buildReportCard(topStudent.getId(), year3.getId()))
        .thenReturn(
            new ReportCardResult(
                topStudent.getId(),
                year3.getId(),
                List.of(passingCourse(6, "18.00")),
                new BigDecimal("18.00"),
                6,
                true));
    when(reportCardService.buildReportCard(secondStudent.getId(), year3.getId()))
        .thenReturn(
            new ReportCardResult(
                secondStudent.getId(),
                year3.getId(),
                List.of(passingCourse(6, "11.00")),
                new BigDecimal("11.00"),
                6,
                true));

    List<GraduateResult> graduates =
        graduationEligibilityService.computeGraduates(cohortId, Track.EL);

    assertEquals(2, graduates.size());
    assertEquals(1, graduates.get(0).rank());
    assertEquals("STD-1", graduates.get(0).studentNumber());
    assertEquals(2, graduates.get(1).rank());
    assertEquals("STD-2", graduates.get(1).studentNumber());
  }

  @Test
  void studentWithACourseBelowTen_isExcludedFromGraduates() {
    Student failingStudent = student("STD-FAIL", "Fail", "Student");
    when(studentRepository.findByCohort_Id(cohortId)).thenReturn(List.of(failingStudent));

    LocalDate lastSemesterStart = LocalDate.of(2026, 1, 1);
    SchoolYear year3 = yearWithLastSemesterStart(lastSemesterStart);
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 3))
        .thenReturn(Optional.of(year3));
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 1))
        .thenReturn(Optional.empty());
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 2))
        .thenReturn(Optional.empty());

    Instant anchor = lastSemesterStart.atStartOfDay(ZoneOffset.UTC).toInstant();
    when(studentPathResolver.resolveTrackAt(failingStudent.getId(), anchor))
        .thenReturn(Optional.of(Track.TN));

    when(reportCardService.buildReportCard(failingStudent.getId(), year3.getId()))
        .thenReturn(
            new ReportCardResult(
                failingStudent.getId(),
                year3.getId(),
                List.of(
                    new CourseAverageResult(
                        UUID.randomUUID(), "Failed", 6, new BigDecimal("8.00"), true)),
                new BigDecimal("8.00"),
                0,
                true));

    List<GraduateResult> graduates =
        graduationEligibilityService.computeGraduates(cohortId, Track.TN);

    assertTrue(graduates.isEmpty());
  }

  @Test
  void studentWithIncompleteCourse_isExcludedFromGraduates() {
    Student incompleteStudent = student("STD-INC", "Incomplete", "Student");
    when(studentRepository.findByCohort_Id(cohortId)).thenReturn(List.of(incompleteStudent));

    LocalDate lastSemesterStart = LocalDate.of(2026, 1, 1);
    SchoolYear year3 = yearWithLastSemesterStart(lastSemesterStart);
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 3))
        .thenReturn(Optional.of(year3));
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 1))
        .thenReturn(Optional.empty());
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 2))
        .thenReturn(Optional.empty());

    Instant anchor = lastSemesterStart.atStartOfDay(ZoneOffset.UTC).toInstant();
    when(studentPathResolver.resolveTrackAt(incompleteStudent.getId(), anchor))
        .thenReturn(Optional.of(Track.EL));

    when(reportCardService.buildReportCard(incompleteStudent.getId(), year3.getId()))
        .thenReturn(
            new ReportCardResult(
                incompleteStudent.getId(),
                year3.getId(),
                List.of(
                    new CourseAverageResult(UUID.randomUUID(), "Missing grade", 6, null, false)),
                null,
                0,
                false));

    List<GraduateResult> graduates =
        graduationEligibilityService.computeGraduates(cohortId, Track.EL);

    assertTrue(graduates.isEmpty());
  }

  @Test
  void studentInTheOtherTrack_isExcludedFromResults() {
    Student wrongTrackStudent = student("STD-WT", "Wrong", "Track");
    when(studentRepository.findByCohort_Id(cohortId)).thenReturn(List.of(wrongTrackStudent));

    LocalDate lastSemesterStart = LocalDate.of(2026, 1, 1);
    SchoolYear year3 = yearWithLastSemesterStart(lastSemesterStart);
    when(schoolYearRepository.findByCohort_IdAndYearNumber(cohortId, 3))
        .thenReturn(Optional.of(year3));

    Instant anchor = lastSemesterStart.atStartOfDay(ZoneOffset.UTC).toInstant();
    when(studentPathResolver.resolveTrackAt(wrongTrackStudent.getId(), anchor))
        .thenReturn(Optional.of(Track.TN));

    List<GraduateResult> graduates =
        graduationEligibilityService.computeGraduates(cohortId, Track.EL);

    assertTrue(graduates.isEmpty());
  }
}
