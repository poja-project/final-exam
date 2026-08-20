package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.SchoolYear;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repository.CohortRepository;
import com.example.demo.repository.SchoolYearRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.GradeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

  private final CohortRepository cohortRepository;
  private final CurrentUserService currentUserService;
  private final StudentRepository studentRepository;
  private final TeacherRepository teacherRepository;
  private final TeacherAssignmentRepository teacherAssignmentRepository;
  private final SchoolYearRepository schoolYearRepository;
  private final GradeService gradeService;

  @GetMapping("/web/login")
  public String loginPage() {
    return "login";
  }

  @GetMapping("/web/cohorts")
  public String cohortsPage(Model model) {
    model.addAttribute("cohorts", cohortRepository.findAll());
    return "cohorts";
  }

  @GetMapping("/web/student/grades")
  public String studentGrades(Model model) {
    User user = currentUserService.requireUser();
    Student student =
        studentRepository
            .findByUser_Id(user.getId())
            .orElseThrow(() -> new IllegalStateException("No student profile"));
    model.addAttribute("student", student);
    model.addAttribute("grades", gradeService.getCurrentGradesForStudent(student.getId()));

    List<SchoolYear> years = schoolYearRepository.findByCohort_Id(student.getCohort().getId());
    model.addAttribute("schoolYears", years);

    return "student";
  }

  @GetMapping("/web/teacher/courses")
  public String teacherCourses(Model model) {
    User user = currentUserService.requireUser();
    Teacher teacher =
        teacherRepository
            .findByUser_Id(user.getId())
            .orElseThrow(() -> new IllegalStateException("No teacher profile"));
    model.addAttribute("teacher", teacher);
    model.addAttribute(
        "assignments", teacherAssignmentRepository.findByTeacher_Id(teacher.getId()));
    return "teacher";
  }
}
