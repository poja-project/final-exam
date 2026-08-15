package com.example.demo.service;

import com.example.demo.model.CourseOffering;
import com.example.demo.model.Semester;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.service.dto.CourseAverageResult;
import com.example.demo.service.dto.ReportCardResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportCardService {

  private static final BigDecimal PASSING_GRADE = BigDecimal.TEN;

  private final SemesterRepository semesterRepository;
  private final StudentPathResolver studentPathResolver;
  private final CourseAverageCalculator courseAverageCalculator;

  public ReportCardResult buildReportCard(UUID studentId, UUID schoolYearId) {
    List<Semester> semesters = semesterRepository.findBySchoolYear_Id(schoolYearId);

    List<CourseAverageResult> allCourseResults = new ArrayList<>();
    for (Semester semester : semesters) {
      List<CourseOffering> applicable =
          studentPathResolver.resolveApplicableCourses(studentId, semester);
      for (CourseOffering offering : applicable) {
        allCourseResults.add(
            courseAverageCalculator.computeAverage(
                studentId, offering.getCourse(), semester.getId()));
      }
    }

    boolean allComplete = allCourseResults.stream().allMatch(CourseAverageResult::complete);

    int creditsEarned =
        allCourseResults.stream()
            .filter(c -> c.average() != null && c.average().compareTo(PASSING_GRADE) >= 0)
            .mapToInt(CourseAverageResult::credit)
            .sum();

    BigDecimal overallAverage =
        allComplete
            ? GradeMath.arithmeticMean(
                allCourseResults.stream().map(CourseAverageResult::average).toList())
            : null;

    return new ReportCardResult(
        studentId, schoolYearId, allCourseResults, overallAverage, creditsEarned, allComplete);
  }
}
