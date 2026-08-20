package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.exception.DomainException;
import com.example.demo.model.GroupEnrollment;
import com.example.demo.model.Student;
import com.example.demo.model.StudentGroup;
import com.example.demo.repository.GroupEnrollmentRepository;
import com.example.demo.repository.StudentGroupRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.security.AccessGuard;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

  @Mock private GroupEnrollmentRepository groupEnrollmentRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private StudentGroupRepository studentGroupRepository;
  @Mock private AccessGuard accessGuard;

  @InjectMocks private EnrollmentService enrollmentService;

  private final UUID studentId = UUID.randomUUID();
  private final UUID groupId = UUID.randomUUID();

  @Test
  void enroll_closesThePreviousActiveEnrollment_beforeCreatingTheNewOne() {
    Student student = new Student();
    student.setId(studentId);
    StudentGroup group = new StudentGroup();
    group.setId(groupId);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

    GroupEnrollment previous = new GroupEnrollment();
    previous.setStartDate(Instant.parse("2023-09-01T00:00:00Z"));
    previous.setEndDate(null);
    when(groupEnrollmentRepository.findByStudent_IdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(previous));
    when(groupEnrollmentRepository.save(any(GroupEnrollment.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    Instant newStart = Instant.parse("2024-01-15T00:00:00Z");
    enrollmentService.enroll(studentId, groupId, newStart);

    assertEquals(newStart, previous.getEndDate());
    verify(groupEnrollmentRepository, times(2)).save(any(GroupEnrollment.class));
  }

  @Test
  void enroll_doesNotTouchAnyPreviousEnrollment_whenStudentHasNoneYet() {
    Student student = new Student();
    student.setId(studentId);
    StudentGroup group = new StudentGroup();
    group.setId(groupId);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(groupEnrollmentRepository.findByStudent_IdAndEndDateIsNull(studentId))
        .thenReturn(Optional.empty());
    when(groupEnrollmentRepository.save(any(GroupEnrollment.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    enrollmentService.enroll(studentId, groupId, Instant.now());

    verify(groupEnrollmentRepository, times(1)).save(any(GroupEnrollment.class));
  }

  @Test
  void enroll_withUnknownStudent_throwsNotFound() {
    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(
        DomainException.class, () -> enrollmentService.enroll(studentId, groupId, Instant.now()));
    verify(groupEnrollmentRepository, never()).save(any());
  }

  @Test
  void getHistoryForRequester_delegatesOwnershipCheckToAccessGuard() {
    enrollmentService.getHistoryForRequester(studentId);

    verify(accessGuard).ensureSelfOrAdmin(studentId);
  }
}
