package com.example.demo.service;

import com.example.demo.model.Course;
import com.example.demo.model.Exam;
import com.example.demo.model.Grade;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.service.dto.CourseAverageResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseAverageCalculator {

  private static final int SCALE = 2;

  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;

  public CourseAverageResult computeAverage(UUID studentId, Course course, UUID semesterId) {
    List<Exam> exams = examRepository.findByCourse_IdAndSemester_Id(course.getId(), semesterId);

    if (exams.isEmpty()) {
      return new CourseAverageResult(
          course.getId(), course.getTitle(), course.getCredit(), null, false);
    }

    BigDecimal weightedSum = BigDecimal.ZERO;
    for (Exam exam : exams) {
      Optional<Grade> currentGrade = gradeRepository.findCurrent(studentId, exam.getId());
      if (currentGrade.isEmpty()) {
        return new CourseAverageResult(
            course.getId(), course.getTitle(), course.getCredit(), null, false);
      }
      weightedSum = weightedSum.add(currentGrade.get().getValue().multiply(exam.getCoeff()));
    }

    BigDecimal average = weightedSum.setScale(SCALE, RoundingMode.HALF_UP);
    return new CourseAverageResult(
        course.getId(), course.getTitle(), course.getCredit(), average, true);
  }
}
