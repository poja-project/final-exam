package com.example.demo.service;

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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduationEligibilityService {

  private static final BigDecimal PASSING_GRADE = BigDecimal.TEN;

  private final StudentRepository studentRepository;
  private final SchoolYearRepository schoolYearRepository;
  private final SemesterRepository semesterRepository;
  private final StudentPathResolver studentPathResolver;
  private final ReportCardService reportCardService;

  public List<GraduateResult> computeGraduates(UUID cohortId, Track track) {
    List<Student> students = studentRepository.findByCohort_Id(cohortId);

    List<GraduateResult> ranked = new ArrayList<>();
    for (Student student : students) {
      if (!endsInTrack(student.getId(), cohortId, track)) {
        continue;
      }

      List<CourseAverageResult> threeYearCourses =
          collectAllCourseResults(student.getId(), cohortId);

      boolean allComplete = threeYearCourses.stream().allMatch(CourseAverageResult::complete);
      boolean allPassing =
          threeYearCourses.stream()
              .allMatch(c -> c.average() != null && c.average().compareTo(PASSING_GRADE) >= 0);

      if (!allComplete || !allPassing || threeYearCourses.isEmpty()) {
        continue;
      }

      BigDecimal overallAverage = creditWeightedAverage(threeYearCourses);
      ranked.add(
          new GraduateResult(
              0,
              student.getId(),
              student.getStudentNumber(),
              student.getLastName(),
              student.getFirstName(),
              overallAverage));
    }

    ranked.sort(Comparator.comparing(GraduateResult::overallAverage).reversed());

    List<GraduateResult> result = new ArrayList<>();
    int rank = 1;
    for (GraduateResult g : ranked) {
      result.add(
          new GraduateResult(
              rank++,
              g.studentId(),
              g.studentNumber(),
              g.lastName(),
              g.firstName(),
              g.overallAverage()));
    }
    return result;
  }

  private boolean endsInTrack(UUID studentId, UUID cohortId, Track track) {
    Optional<Semester> lastSemester =
        schoolYearRepository
            .findByCohort_IdAndYearNumber(cohortId, 3)
            .flatMap(
                year3 ->
                    semesterRepository.findBySchoolYear_Id(year3.getId()).stream()
                        .max(Comparator.comparing(Semester::getNumber)));

    if (lastSemester.isEmpty()) {
      return false;
    }
    Instant anchor = lastSemester.get().getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    return studentPathResolver.resolveTrackAt(studentId, anchor).map(t -> t == track).orElse(false);
  }

  private List<CourseAverageResult> collectAllCourseResults(UUID studentId, UUID cohortId) {
    List<CourseAverageResult> all = new ArrayList<>();
    for (int yearNumber = 1; yearNumber <= 3; yearNumber++) {
      schoolYearRepository
          .findByCohort_IdAndYearNumber(cohortId, yearNumber)
          .ifPresent(
              year -> {
                ReportCardResult reportCard =
                    reportCardService.buildReportCard(studentId, year.getId());
                all.addAll(reportCard.courses());
              });
    }
    return all;
  }

  private BigDecimal creditWeightedAverage(List<CourseAverageResult> courses) {
    BigDecimal totalCredits =
        courses.stream()
            .map(c -> BigDecimal.valueOf(c.credit()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal weightedSum =
        courses.stream()
            .map(c -> c.average().multiply(BigDecimal.valueOf(c.credit())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return weightedSum.divide(totalCredits, 2, RoundingMode.HALF_UP);
  }
}
