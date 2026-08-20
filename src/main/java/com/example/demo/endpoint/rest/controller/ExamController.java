package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.ExamDtos.CreateExamsRequest;
import com.example.demo.endpoint.rest.dto.ExamDtos.ExamResponse;
import com.example.demo.model.Exam;
import com.example.demo.repository.ExamRepository;
import com.example.demo.service.ExamService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ExamController {

  private final ExamService examService;
  private final ExamRepository examRepository;

  @PostMapping("/exams")
  public ResponseEntity<List<ExamResponse>> createExams(
      @Valid @RequestBody CreateExamsRequest req) {
    List<ExamService.ExamRequest> examRequests =
        req.exams().stream()
            .map(item -> new ExamService.ExamRequest(item.date(), item.coeff()))
            .toList();
    List<Exam> exams = examService.createExams(req.courseId(), req.semesterId(), examRequests);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(exams.stream().map(ExamResponse::from).toList());
  }

  @GetMapping("/courses/{id}/exams")
  public List<ExamResponse> listExams(@PathVariable UUID id, @RequestParam UUID semesterId) {
    return examRepository.findByCourse_IdAndSemester_Id(id, semesterId).stream()
        .map(ExamResponse::from)
        .toList();
  }
}
