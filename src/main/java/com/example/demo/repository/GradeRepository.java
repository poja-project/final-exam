package com.example.demo.repository;

import com.example.demo.model.Grade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

  List<Grade> findByStudent_IdAndExam_IdOrderByEnteredAtDesc(UUID studentId, UUID examId);

  default Optional<Grade> findCurrent(UUID studentId, UUID examId) {
    List<Grade> history = findByStudent_IdAndExam_IdOrderByEnteredAtDesc(studentId, examId);
    return history.isEmpty() ? Optional.empty() : Optional.of(history.get(0));
  }

  @org.springframework.data.jpa.repository.Query(
      """
      SELECT g FROM Grade g
      WHERE g.exam.id = :examId
        AND g.enteredAt = (
            SELECT MAX(g2.enteredAt) FROM Grade g2
            WHERE g2.exam.id = g.exam.id AND g2.student.id = g.student.id
        )
      """)
  List<Grade> findCurrentGradesForExam(
      @org.springframework.data.repository.query.Param("examId") UUID examId);

  @org.springframework.data.jpa.repository.Query(
      """
      SELECT g FROM Grade g
      WHERE g.student.id = :studentId
        AND g.enteredAt = (
            SELECT MAX(g2.enteredAt) FROM Grade g2
            WHERE g2.exam.id = g.exam.id AND g2.student.id = g.student.id
        )
      """)
  List<Grade> findCurrentGradesForStudent(
      @org.springframework.data.repository.query.Param("studentId") UUID studentId);
}
