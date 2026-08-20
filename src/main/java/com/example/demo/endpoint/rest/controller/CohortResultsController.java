package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.CohortResultsDtos.StudentResults;
import com.example.demo.endpoint.rest.dto.CohortResultsDtos.YearResult;
import com.example.demo.model.Student;
import com.example.demo.repository.SchoolYearRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.ReportCardService;
import com.example.demo.service.dto.ReportCardResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CohortResultsController {

  private final StudentRepository studentRepository;
  private final SchoolYearRepository schoolYearRepository;
  private final ReportCardService reportCardService;

  @GetMapping("/admin/cohorts/{id}/results")
  public List<StudentResults> getCohortResults(@PathVariable UUID id) {
    List<Student> students = studentRepository.findByCohort_Id(id);

    List<StudentResults> results = new ArrayList<>();
    for (Student student : students) {
      List<YearResult> years = new ArrayList<>();
      for (int yearNumber = 1; yearNumber <= 3; yearNumber++) {
        final int currentYearNumber = yearNumber;
        schoolYearRepository
            .findByCohort_IdAndYearNumber(id, currentYearNumber)
            .ifPresent(
                schoolYear -> {
                  ReportCardResult reportCard =
                      reportCardService.buildReportCard(student.getId(), schoolYear.getId());
                  years.add(YearResult.from(currentYearNumber, reportCard));
                });
      }
      results.add(
          new StudentResults(
              student.getId(),
              student.getStudentNumber(),
              student.getLastName(),
              student.getFirstName(),
              years));
    }
    return results;
  }
}
