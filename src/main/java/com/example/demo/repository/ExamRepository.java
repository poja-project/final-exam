package com.example.demo.repository;

import com.example.demo.model.Exam;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

  List<Exam> findByCourse_IdAndSemester_Id(UUID courseId, UUID semesterId);

  default BigDecimal sumCoeffForCourseAndSemester(UUID courseId, UUID semesterId) {
    return findByCourse_IdAndSemester_Id(courseId, semesterId).stream()
        .map(Exam::getCoeff)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
