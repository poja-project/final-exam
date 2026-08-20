package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.rest.dto.AccountDtos.CreateStudentRequest;
import com.example.demo.endpoint.rest.dto.AccountDtos.CreateTeacherRequest;
import com.example.demo.endpoint.rest.dto.AccountDtos.StudentResponse;
import com.example.demo.endpoint.rest.dto.AccountDtos.TeacherResponse;
import com.example.demo.endpoint.rest.dto.AccountDtos.UserResponse;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AccountService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;
  private final UserRepository userRepository;

  @PostMapping("/admin/users/teachers")
  public ResponseEntity<TeacherResponse> createTeacher(
      @Valid @RequestBody CreateTeacherRequest req) {
    Teacher teacher =
        accountService.createTeacher(req.lastName(), req.firstName(), req.email(), req.password());
    return ResponseEntity.status(HttpStatus.CREATED).body(TeacherResponse.from(teacher));
  }

  @PostMapping("/admin/users/students")
  public ResponseEntity<StudentResponse> createStudent(
      @Valid @RequestBody CreateStudentRequest req) {
    Student student =
        accountService.createStudent(
            req.lastName(), req.firstName(), req.email(), req.password(), req.cohortId());
    return ResponseEntity.status(HttpStatus.CREATED).body(StudentResponse.from(student));
  }

  @GetMapping("/admin/users")
  public List<UserResponse> listUsers(@RequestParam(required = false) Role role) {
    List<User> users =
        role == null
            ? userRepository.findAll()
            : userRepository.findAll().stream().filter(u -> u.getRole() == role).toList();
    return users.stream().map(UserResponse::from).toList();
  }
}
