package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.GradeDtos.CreateGradeRequest;
import com.example.demo.endpoint.rest.dto.GradeDtos.GradeResponse;
import com.example.demo.model.Grade;
import com.example.demo.service.GradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @PostMapping("/grades")
  public ResponseEntity<GradeResponse> submitGrade(@Valid @RequestBody CreateGradeRequest req) {
    Grade grade =
        gradeService.submitGrade(req.studentId(), req.examId(), req.value(), req.reason());
    return ResponseEntity.status(HttpStatus.CREATED).body(GradeResponse.from(grade));
  }

  @GetMapping("/exams/{id}/grades")
  public List<GradeResponse> gradesForExam(@PathVariable UUID id) {
    return gradeService.getCurrentGradesForExam(id).stream().map(GradeResponse::from).toList();
  }

  @GetMapping("/students/{id}/grades")
  public List<GradeResponse> gradesForStudent(@PathVariable UUID id) {
    return gradeService.getCurrentGradesForStudent(id).stream().map(GradeResponse::from).toList();
  }

  @GetMapping("/exams/{id}/grades/{studentId}/history")
  public List<GradeResponse> gradeHistory(@PathVariable UUID id, @PathVariable UUID studentId) {
    return gradeService.getGradeHistory(studentId, id).stream().map(GradeResponse::from).toList();
  }

  @GetMapping("/students/me/grades")
  public List<GradeResponse> myGrades() {
    return gradeService.getCurrentGradesForCurrentStudent().stream()
        .map(GradeResponse::from)
        .toList();
  }
}
