package com.example.demo.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;
  private final SemesterRepository semesterRepository;
  private final AccessGuard accessGuard;

  public record ExamRequest(Instant date, BigDecimal coeff) {}

  @Transactional
  public List<Exam> createExams(UUID courseId, UUID semesterId, List<ExamRequest> examRequests) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> DomainException.notFound("Course not found: " + courseId));
    Semester semester =
        semesterRepository
            .findById(semesterId)
            .orElseThrow(() -> DomainException.notFound("Semester not found: " + semesterId));

    accessGuard.ensureAdminOrTeacherOfCourse(courseId, semesterId);

    if (examRequests == null || examRequests.isEmpty()) {
      throw DomainException.badRequest("At least one exam is required");
    }

    boolean alreadyExists =
        !examRepository.findByCourse_IdAndSemester_Id(courseId, semesterId).isEmpty();
    if (alreadyExists) {
      throw DomainException.conflict(
          "Exams already exist for this course/semester. Delete them first to redefine the set.");
    }

    for (ExamRequest req : examRequests) {
      if (req.coeff().compareTo(BigDecimal.ZERO) <= 0) {
        throw DomainException.badRequest(
            "Each coeff must be strictly positive, got: " + req.coeff());
      }
    }

    List<BigDecimal> rawCoefficients = examRequests.stream().map(ExamRequest::coeff).toList();
    List<BigDecimal> normalized = BusinessValidator.normalizeCoefficients(rawCoefficients);
    BusinessValidator.validateCoefficientsSum(normalized);

    List<Exam> exams = new ArrayList<>();
    for (int i = 0; i < examRequests.size(); i++) {
      Exam exam = new Exam();
      exam.setCourse(course);
      exam.setSemester(semester);
      exam.setDate(examRequests.get(i).date());
      exam.setCoeff(normalized.get(i));
      exams.add(exam);
    }
    return examRepository.saveAll(exams);
  }
}
