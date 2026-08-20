package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.CohortResponse;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.CourseOfferingResponse;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.CourseResponse;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.CreateCohortRequest;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.CreateCourseOfferingRequest;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.CreateCourseRequest;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.CreateSchoolYearRequest;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.CreateSemesterRequest;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.SchoolYearResponse;
import com.example.demo.endpoint.rest.dto.AcademicReferenceDtos.SemesterResponse;
import com.example.demo.exception.DomainException;
import com.example.demo.model.Cohort;
import com.example.demo.model.Course;
import com.example.demo.model.CourseOffering;
import com.example.demo.model.SchoolYear;
import com.example.demo.model.Semester;
import com.example.demo.repository.CohortRepository;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.SchoolYearRepository;
import com.example.demo.repository.SemesterRepository;
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
public class AcademicReferenceController {
  private final CohortRepository cohortRepository;
  private final SchoolYearRepository schoolYearRepository;
  private final SemesterRepository semesterRepository;
  private final CourseRepository courseRepository;
  private final CourseOfferingRepository courseOfferingRepository;

  @PostMapping("/admin/cohorts")
  public ResponseEntity<CohortResponse> createCohort(@Valid @RequestBody CreateCohortRequest req) {
    Cohort cohort = new Cohort();
    cohort.setLabel(req.label());
    cohort.setEntryYear(req.entryYear());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(CohortResponse.from(cohortRepository.save(cohort)));
  }

  @GetMapping("/cohorts")
  public List<CohortResponse> listCohorts() {
    return cohortRepository.findAll().stream().map(CohortResponse::from).toList();
  }

  @PostMapping("/admin/cohorts/{id}/school-years")
  public ResponseEntity<SchoolYearResponse> createSchoolYear(
      @PathVariable UUID id, @Valid @RequestBody CreateSchoolYearRequest req) {
    Cohort cohort =
        cohortRepository
            .findById(id)
            .orElseThrow(() -> DomainException.notFound("Cohort not found: " + id));
    SchoolYear year = new SchoolYear();
    year.setLabel(req.label());
    year.setYearNumber(req.yearNumber());
    year.setCohort(cohort);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SchoolYearResponse.from(schoolYearRepository.save(year)));
  }

  @PostMapping("/admin/school-years/{id}/semesters")
  public ResponseEntity<SemesterResponse> createSemester(
      @PathVariable UUID id, @Valid @RequestBody CreateSemesterRequest req) {
    SchoolYear year =
        schoolYearRepository
            .findById(id)
            .orElseThrow(() -> DomainException.notFound("School year not found: " + id));
    Semester semester = new Semester();
    semester.setNumber(req.number());
    semester.setSchoolYear(year);
    semester.setStartDate(req.startDate());
    semester.setEndDate(req.endDate());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SemesterResponse.from(semesterRepository.save(semester)));
  }

  @PostMapping("/admin/courses")
  public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest req) {
    Course course = new Course();
    course.setReference(req.reference());
    course.setTitle(req.title());
    course.setCredit(req.credit());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(CourseResponse.from(courseRepository.save(course)));
  }

  @PostMapping("/admin/semesters/{id}/courses")
  public ResponseEntity<CourseOfferingResponse> addCourseToSemester(
      @PathVariable UUID id, @Valid @RequestBody CreateCourseOfferingRequest req) {
    Semester semester =
        semesterRepository
            .findById(id)
            .orElseThrow(() -> DomainException.notFound("Semester not found: " + id));
    Course course =
        courseRepository
            .findById(req.courseId())
            .orElseThrow(() -> DomainException.notFound("Course not found: " + req.courseId()));
    CourseOffering offering = new CourseOffering();
    offering.setCourse(course);
    offering.setSemester(semester);
    offering.setTrack(req.track());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(CourseOfferingResponse.from(courseOfferingRepository.save(offering)));
  }
}
