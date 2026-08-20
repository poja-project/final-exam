package com.example.demo.service;

import com.example.demo.exception.DomainException;
import com.example.demo.model.Exam;
import com.example.demo.model.Grade;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.security.AccessGuard;
import com.example.demo.security.CurrentUserService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final ExamRepository examRepository;
  private final StudentRepository studentRepository;
  private final AccessGuard accessGuard;
  private final CurrentUserService currentUserService;

  @Transactional
  public Grade submitGrade(UUID studentId, UUID examId, BigDecimal value, String reason) {
    Exam exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> DomainException.notFound("Exam not found: " + examId));
    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(() -> DomainException.notFound("Student not found: " + studentId));

    accessGuard.ensureAdminOrTeacherOfCourse(exam.getCourse().getId(), exam.getSemester().getId());
    User author = currentUserService.requireUser();

    BusinessValidator.validateGradeValue(value);

    boolean alreadyGraded =
        !gradeRepository
            .findByStudent_IdAndExam_IdOrderByEnteredAtDesc(studentId, examId)
            .isEmpty();
    BusinessValidator.validateReasonRequired(alreadyGraded, reason);

    Grade grade = new Grade();
    grade.setStudent(student);
    grade.setExam(exam);
    grade.setValue(value);
    grade.setReason(reason);
    grade.setAuthor(author);
    return gradeRepository.save(grade);
  }

  public List<Grade> getCurrentGradesForExam(UUID examId) {
    Exam exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> DomainException.notFound("Exam not found: " + examId));
    accessGuard.ensureAdminOrTeacherOfCourse(exam.getCourse().getId(), exam.getSemester().getId());
    return gradeRepository.findCurrentGradesForExam(examId);
  }

  public List<Grade> getCurrentGradesForStudent(UUID studentId) {
    accessGuard.ensureSelfOrAdmin(studentId);
    return gradeRepository.findCurrentGradesForStudent(studentId);
  }

  public Optional<Grade> getCurrentGrade(UUID studentId, UUID examId) {
    accessGuard.ensureSelfOrAdmin(studentId);
    return gradeRepository.findCurrent(studentId, examId);
  }

  public List<Grade> getGradeHistory(UUID studentId, UUID examId) {
    Exam exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> DomainException.notFound("Exam not found: " + examId));
    accessGuard.ensureAdminOrTeacherOfCourse(exam.getCourse().getId(), exam.getSemester().getId());
    return gradeRepository.findByStudent_IdAndExam_IdOrderByEnteredAtDesc(studentId, examId);
  }

  public List<Grade> getCurrentGradesForCurrentStudent() {
    Student self = accessGuard.requireStudentProfile();
    return gradeRepository.findCurrentGradesForStudent(self.getId());
  }
}
