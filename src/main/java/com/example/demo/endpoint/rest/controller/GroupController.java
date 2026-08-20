package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.GroupDtos.AssignmentResponse;
import com.example.demo.endpoint.rest.dto.GroupDtos.CreateAssignmentRequest;
import com.example.demo.endpoint.rest.dto.GroupDtos.CreateEnrollmentRequest;
import com.example.demo.endpoint.rest.dto.GroupDtos.CreateGroupRequest;
import com.example.demo.endpoint.rest.dto.GroupDtos.GroupEnrollmentResponse;
import com.example.demo.endpoint.rest.dto.GroupDtos.GroupResponse;
import com.example.demo.exception.DomainException;
import com.example.demo.model.Cohort;
import com.example.demo.model.Course;
import com.example.demo.model.GroupEnrollment;
import com.example.demo.model.StudentGroup;
import com.example.demo.model.Teacher;
import com.example.demo.model.TeacherAssignment;
import com.example.demo.repository.CohortRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.StudentGroupRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.service.EnrollmentService;
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
public class GroupController {

  private final StudentGroupRepository studentGroupRepository;
  private final CohortRepository cohortRepository;
  private final EnrollmentService enrollmentService;
  private final TeacherAssignmentRepository teacherAssignmentRepository;
  private final TeacherRepository teacherRepository;
  private final CourseRepository courseRepository;
  private final SemesterRepository semesterRepository;

  @PostMapping("/admin/groups")
  public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest req) {
    Cohort cohort =
        cohortRepository
            .findById(req.cohortId())
            .orElseThrow(() -> DomainException.notFound("Cohort not found: " + req.cohortId()));
    StudentGroup group = new StudentGroup();
    group.setReference(req.reference());
    group.setTrack(req.track());
    group.setCohort(cohort);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(GroupResponse.from(studentGroupRepository.save(group)));
  }

  @PostMapping("/admin/students/{id}/enrollments")
  public ResponseEntity<GroupEnrollmentResponse> enroll(
      @PathVariable UUID id, @Valid @RequestBody CreateEnrollmentRequest req) {
    GroupEnrollment enrollment = enrollmentService.enroll(id, req.groupId(), req.startDate());
    return ResponseEntity.status(HttpStatus.CREATED).body(GroupEnrollmentResponse.from(enrollment));
  }

  @GetMapping("/students/{id}/enrollments")
  public List<GroupEnrollmentResponse> history(@PathVariable UUID id) {
    return enrollmentService.getHistoryForRequester(id).stream()
        .map(GroupEnrollmentResponse::from)
        .toList();
  }

  @PostMapping("/admin/assignments")
  public ResponseEntity<AssignmentResponse> createAssignment(
      @Valid @RequestBody CreateAssignmentRequest req) {
    Teacher teacher =
        teacherRepository
            .findById(req.teacherId())
            .orElseThrow(() -> DomainException.notFound("Teacher not found: " + req.teacherId()));
    Course course =
        courseRepository
            .findById(req.courseId())
            .orElseThrow(() -> DomainException.notFound("Course not found: " + req.courseId()));
    StudentGroup group =
        studentGroupRepository
            .findById(req.groupId())
            .orElseThrow(() -> DomainException.notFound("Group not found: " + req.groupId()));
    var semester =
        semesterRepository
            .findById(req.semesterId())
            .orElseThrow(() -> DomainException.notFound("Semester not found: " + req.semesterId()));

    TeacherAssignment assignment = new TeacherAssignment();
    assignment.setTeacher(teacher);
    assignment.setCourse(course);
    assignment.setGroup(group);
    assignment.setSemester(semester);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(AssignmentResponse.from(teacherAssignmentRepository.save(assignment)));
  }

  @GetMapping("/admin/assignments")
  public List<AssignmentResponse> listAssignments(
      @RequestParam(required = false) UUID semesterId,
      @RequestParam(required = false) UUID teacherId) {
    List<TeacherAssignment> assignments;
    if (teacherId != null) {
      assignments = teacherAssignmentRepository.findByTeacher_Id(teacherId);
    } else if (semesterId != null) {
      assignments = teacherAssignmentRepository.findBySemester_Id(semesterId);
    } else {
      assignments = teacherAssignmentRepository.findAll();
    }
    return assignments.stream().map(AssignmentResponse::from).toList();
  }
}
