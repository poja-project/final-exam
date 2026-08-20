package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.Course;
import com.example.demo.model.Teacher;
import com.example.demo.repository.TeacherAssignmentRepository;
import com.example.demo.security.AccessGuard;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TeacherController {

  private final AccessGuard accessGuard;
  private final TeacherAssignmentRepository teacherAssignmentRepository;

  public record CourseSummary(UUID id, String reference, String title, Integer credit) {
    static CourseSummary from(Course c) {
      return new CourseSummary(c.getId(), c.getReference(), c.getTitle(), c.getCredit());
    }
  }

  @GetMapping("/teachers/me/courses")
  public List<CourseSummary> myCourses() {
    Teacher teacher = accessGuard.requireTeacherProfile();
    return teacherAssignmentRepository.findByTeacher_Id(teacher.getId()).stream()
        .map(a -> a.getCourse())
        .distinct()
        .map(CourseSummary::from)
        .toList();
  }
}
