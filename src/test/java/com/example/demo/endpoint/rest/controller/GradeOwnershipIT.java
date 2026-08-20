package com.example.demo.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class GradeOwnershipIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private TeacherRepository teacherRepository;
  @Autowired private StudentRepository studentRepository;
  @Autowired private CohortRepository cohortRepository;
  @Autowired private SchoolYearRepository schoolYearRepository;
  @Autowired private SemesterRepository semesterRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private StudentGroupRepository studentGroupRepository;
  @Autowired private TeacherAssignmentRepository teacherAssignmentRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void teacherAssignedOnlyForSemester1_cannotGradeSameCourseInSemester2() {
    Cohort cohort = cohortRepository.save(cohortWith("Cohort IT-1"));
    SchoolYear year = schoolYearRepository.save(yearFor(cohort));
    Semester semester1 = semesterRepository.save(semesterFor(year, 1));
    Semester semester2 = semesterRepository.save(semesterFor(year, 2));
    Course course = courseRepository.save(courseWith("ALG-IT-1"));
    StudentGroup group = studentGroupRepository.save(groupFor(cohort));

    User teacherUser = userRepository.save(userWith("teacher-it1@prog4.local", Role.TEACHER));
    Teacher teacher = teacherRepository.save(teacherFor(teacherUser));

    teacherAssignmentRepository.save(assignmentFor(teacher, course, group, semester1));

    Exam examInSemester2 = examRepository.save(examFor(course, semester2, new BigDecimal("1.0")));

    var credentials = Map.of("email", "teacher-it1@prog4.local", "password", "Demo1234!");
    var loginResponse = restTemplate.postForEntity("/auth/login", credentials, Void.class);
    String cookie = loginResponse.getHeaders().getFirst("Set-Cookie");

    var headers = new org.springframework.http.HttpHeaders();
    headers.add("Cookie", cookie);
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

    User studentUser = userRepository.save(userWith("student-it1@prog4.local", Role.STUDENT));
    Student student = studentRepository.save(studentFor(studentUser, cohort, "STD-IT-1"));

    var body =
        Map.of(
            "studentId", student.getId().toString(),
            "examId", examInSemester2.getId().toString(),
            "value", 15);
    var request = new org.springframework.http.HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.postForEntity("/grades", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void studentCannotViewAnotherStudentsGrades() {
    Cohort cohort = cohortRepository.save(cohortWith("Cohort IT-2"));
    User studentUserA = userRepository.save(userWith("student-a-own@prog4.local", Role.STUDENT));
    Student studentA = studentRepository.save(studentFor(studentUserA, cohort, "STD-OWN-A"));
    User studentUserB = userRepository.save(userWith("student-b-own@prog4.local", Role.STUDENT));
    studentRepository.save(studentFor(studentUserB, cohort, "STD-OWN-B"));

    var credentials = Map.of("email", "student-b-own@prog4.local", "password", "Demo1234!");
    var loginResponse = restTemplate.postForEntity("/auth/login", credentials, Void.class);
    String cookie = loginResponse.getHeaders().getFirst("Set-Cookie");

    var headers = new org.springframework.http.HttpHeaders();
    headers.add("Cookie", cookie);
    var request = new org.springframework.http.HttpEntity<>(headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/students/" + studentA.getId() + "/grades",
            org.springframework.http.HttpMethod.GET,
            request,
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private Cohort cohortWith(String label) {
    Cohort c = new Cohort();
    c.setLabel(label);
    c.setEntryYear(2023);
    return c;
  }

  private SchoolYear yearFor(Cohort cohort) {
    SchoolYear y = new SchoolYear();
    y.setLabel("2023-2024");
    y.setYearNumber(1);
    y.setCohort(cohort);
    return y;
  }

  private Semester semesterFor(SchoolYear year, int number) {
    Semester s = new Semester();
    s.setNumber(number);
    s.setSchoolYear(year);
    s.setStartDate(LocalDate.of(2023, 9, 1));
    s.setEndDate(LocalDate.of(2024, 1, 31));
    return s;
  }

  private Course courseWith(String reference) {
    Course c = new Course();
    c.setReference(reference);
    c.setTitle("Algorithms IT");
    c.setCredit(6);
    return c;
  }

  private StudentGroup groupFor(Cohort cohort) {
    StudentGroup g = new StudentGroup();
    g.setReference("G-IT-1");
    g.setCohort(cohort);
    return g;
  }

  private User userWith(String email, Role role) {
    User u = new User();
    u.setEmail(email);
    u.setPassword(passwordEncoder.encode("Demo1234!"));
    u.setRole(role);
    return u;
  }

  private Teacher teacherFor(User user) {
    Teacher t = new Teacher();
    t.setLastName("Test");
    t.setFirstName("Teacher");
    t.setUser(user);
    return t;
  }

  private Student studentFor(User user, Cohort cohort, String studentNumber) {
    Student s = new Student();
    s.setLastName("Test");
    s.setFirstName("Student");
    s.setUser(user);
    s.setCohort(cohort);
    s.setStudentNumber(studentNumber);
    return s;
  }

  private TeacherAssignment assignmentFor(
      Teacher teacher, Course course, StudentGroup group, Semester semester) {
    TeacherAssignment a = new TeacherAssignment();
    a.setTeacher(teacher);
    a.setCourse(course);
    a.setGroup(group);
    a.setSemester(semester);
    return a;
  }

  private Exam examFor(Course course, Semester semester, BigDecimal coeff) {
    Exam e = new Exam();
    e.setCourse(course);
    e.setSemester(semester);
    e.setDate(Instant.now());
    e.setCoeff(coeff);
    return e;
  }
}
