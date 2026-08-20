package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.ReportCardPdfRequestedEvent;
import com.example.demo.endpoint.rest.dto.ReportCardDtos.ReportCardResponse;
import com.example.demo.exception.DomainException;
import com.example.demo.model.Student;
import com.example.demo.repository.StudentRepository;
import com.example.demo.security.AccessGuard;
import com.example.demo.service.ReportCardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReportCardController {

  private final ReportCardService reportCardService;
  private final AccessGuard accessGuard;
  private final EventProducer<ReportCardPdfRequestedEvent> eventProducer;
  private final StudentRepository studentRepository;

  @GetMapping("/students/{id}/report-card/{schoolYearId}")
  public ReportCardResponse getReportCard(@PathVariable UUID id, @PathVariable UUID schoolYearId) {
    accessGuard.ensureSelfOrAdmin(id);
    return ReportCardResponse.from(reportCardService.buildReportCard(id, schoolYearId));
  }

  @PostMapping("/students/{id}/report-card/{schoolYearId}/pdf")
  public ResponseEntity<Void> sendReportCardPdf(
      @PathVariable UUID id, @PathVariable UUID schoolYearId) {
    accessGuard.ensureSelfOrAdmin(id);

    Student student =
        studentRepository
            .findById(id)
            .orElseThrow(() -> DomainException.notFound("Student not found: " + id));

    ReportCardPdfRequestedEvent event =
        ReportCardPdfRequestedEvent.builder()
            .studentId(id.toString())
            .schoolYearId(schoolYearId.toString())
            .studentEmail(student.getUser().getEmail())
            .build();

    eventProducer.accept(List.of(event));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
