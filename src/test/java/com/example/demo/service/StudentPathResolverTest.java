package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.model.CourseOffering;
import com.example.demo.model.GroupEnrollment;
import com.example.demo.model.SchoolYear;
import com.example.demo.model.Semester;
import com.example.demo.model.StudentGroup;
import com.example.demo.model.Track;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.GroupEnrollmentRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentPathResolverTest {

  @Mock private GroupEnrollmentRepository groupEnrollmentRepository;
  @Mock private CourseOfferingRepository courseOfferingRepository;

  @InjectMocks private StudentPathResolver resolver;

  private final UUID studentId = UUID.randomUUID();

  private GroupEnrollment enrollment(StudentGroup group, Instant start, Instant end) {
    GroupEnrollment e = new GroupEnrollment();
    e.setGroup(group);
    e.setStartDate(start);
    e.setEndDate(end);
    return e;
  }

  private StudentGroup groupWithTrack(Track track) {
    StudentGroup g = new StudentGroup();
    g.setId(UUID.randomUUID());
    g.setTrack(track);
    return g;
  }

  @Test
  void resolveGroupAt_returnsGroup_whenPointInTimeIsWithinAClosedInterval() {
    StudentGroup groupA = groupWithTrack(Track.EL);
    Instant start = Instant.parse("2023-09-01T00:00:00Z");
    Instant end = Instant.parse("2024-01-01T00:00:00Z");
    when(groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment(groupA, start, end)));

    Optional<StudentGroup> result =
        resolver.resolveGroupAt(studentId, start.plus(10, ChronoUnit.DAYS));

    assertTrue(result.isPresent());
    assertEquals(groupA.getId(), result.get().getId());
  }

  @Test
  void resolveGroupAt_returnsEmpty_whenPointInTimeIsBeforeEnrollmentStarted() {
    StudentGroup groupA = groupWithTrack(Track.EL);
    Instant start = Instant.parse("2023-09-01T00:00:00Z");
    when(groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment(groupA, start, null)));

    Optional<StudentGroup> result =
        resolver.resolveGroupAt(studentId, start.minus(1, ChronoUnit.DAYS));

    assertFalse(result.isPresent());
  }

  @Test
  void resolveGroupAt_treatsNullEndDateAsStillActive() {
    StudentGroup groupA = groupWithTrack(Track.TN);
    Instant start = Instant.parse("2023-09-01T00:00:00Z");
    when(groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment(groupA, start, null)));

    Optional<StudentGroup> result =
        resolver.resolveGroupAt(studentId, start.plus(1000, ChronoUnit.DAYS));

    assertTrue(result.isPresent());
  }

  @Test
  void
      resolveGroupAt_picksTheEnrollmentActiveAtThatPreciseInstant_whenStudentChangedGroupMidYear() {
    StudentGroup groupA = groupWithTrack(Track.EL);
    StudentGroup groupB = groupWithTrack(Track.TN);
    Instant switchDate = Instant.parse("2023-12-01T00:00:00Z");
    when(groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(
            List.of(
                enrollment(groupA, Instant.parse("2023-09-01T00:00:00Z"), switchDate),
                enrollment(groupB, switchDate, null)));

    Optional<StudentGroup> beforeSwitch =
        resolver.resolveGroupAt(studentId, switchDate.minus(1, ChronoUnit.DAYS));
    Optional<StudentGroup> afterSwitch =
        resolver.resolveGroupAt(studentId, switchDate.plus(1, ChronoUnit.DAYS));

    assertEquals(groupA.getId(), beforeSwitch.orElseThrow().getId());
    assertEquals(groupB.getId(), afterSwitch.orElseThrow().getId());
  }

  @Test
  void resolveTrackAt_returnsTheGroupsTrack() {
    StudentGroup group = groupWithTrack(Track.EL);
    Instant start = Instant.parse("2023-09-01T00:00:00Z");
    when(groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment(group, start, null)));

    Optional<Track> track = resolver.resolveTrackAt(studentId, start.plus(1, ChronoUnit.DAYS));

    assertTrue(track.isPresent());
    assertEquals(Track.EL, track.get());
  }

  @Test
  void resolveTrackAt_returnsEmpty_whenStudentHasNoEnrollmentAtThatTime() {
    when(groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(List.of());

    Optional<Track> track = resolver.resolveTrackAt(studentId, Instant.now());

    assertFalse(track.isPresent());
  }

  @Test
  void
      resolveApplicableCourses_anchorsOnSemesterStartDate_andDelegatesToRepositoryWithResolvedTrack() {
    StudentGroup group = groupWithTrack(Track.TN);
    LocalDate semesterStart = LocalDate.of(2023, 9, 1);
    when(groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(
            List.of(
                enrollment(group, semesterStart.atStartOfDay(ZoneOffset.UTC).toInstant(), null)));

    Semester semester = new Semester();
    semester.setId(UUID.randomUUID());
    semester.setStartDate(semesterStart);
    semester.setEndDate(LocalDate.of(2024, 1, 31));
    SchoolYear year = new SchoolYear();
    semester.setSchoolYear(year);

    CourseOffering offering = new CourseOffering();
    when(courseOfferingRepository.findApplicableForTrack(semester.getId(), Track.TN))
        .thenReturn(List.of(offering));

    List<CourseOffering> result = resolver.resolveApplicableCourses(studentId, semester);

    assertEquals(1, result.size());
    verify(courseOfferingRepository).findApplicableForTrack(semester.getId(), Track.TN);
  }

  @Test
  void resolveApplicableCourses_passesNullTrack_whenStudentHasNoGroupAtSemesterStart() {
    when(groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(List.of());

    Semester semester = new Semester();
    semester.setId(UUID.randomUUID());
    semester.setStartDate(LocalDate.of(2021, 9, 1));
    semester.setEndDate(LocalDate.of(2022, 1, 31));

    resolver.resolveApplicableCourses(studentId, semester);

    verify(courseOfferingRepository).findApplicableForTrack(semester.getId(), null);
  }
}
