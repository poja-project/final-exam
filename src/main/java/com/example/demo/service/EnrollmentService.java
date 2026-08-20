package com.example.demo.service;

import com.example.demo.exception.DomainException;
import com.example.demo.model.GroupEnrollment;
import com.example.demo.model.Student;
import com.example.demo.model.StudentGroup;
import com.example.demo.repository.GroupEnrollmentRepository;
import com.example.demo.repository.StudentGroupRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.security.AccessGuard;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

  private final GroupEnrollmentRepository groupEnrollmentRepository;
  private final StudentRepository studentRepository;
  private final StudentGroupRepository studentGroupRepository;
  private final AccessGuard accessGuard;

  @Transactional
  public GroupEnrollment enroll(UUID studentId, UUID groupId, Instant startDate) {
    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(() -> DomainException.notFound("Student not found: " + studentId));
    StudentGroup group =
        studentGroupRepository
            .findById(groupId)
            .orElseThrow(() -> DomainException.notFound("Group not found: " + groupId));

    Optional<GroupEnrollment> currentActive =
        groupEnrollmentRepository.findByStudent_IdAndEndDateIsNull(studentId);
    currentActive.ifPresent(
        previous -> {
          previous.setEndDate(startDate);
          groupEnrollmentRepository.save(previous);
        });

    GroupEnrollment enrollment = new GroupEnrollment();
    enrollment.setStudent(student);
    enrollment.setGroup(group);
    enrollment.setStartDate(startDate);
    return groupEnrollmentRepository.save(enrollment);
  }

  public List<GroupEnrollment> getHistory(UUID studentId) {
    return groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId);
  }

  public List<GroupEnrollment> getHistoryForRequester(UUID studentId) {
    accessGuard.ensureSelfOrAdmin(studentId);
    return getHistory(studentId);
  }
}
