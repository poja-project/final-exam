package com.example.demo.service;

import com.example.demo.model.CourseOffering;
import com.example.demo.model.GroupEnrollment;
import com.example.demo.model.Semester;
import com.example.demo.model.StudentGroup;
import com.example.demo.model.Track;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.GroupEnrollmentRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentPathResolver {

  private final GroupEnrollmentRepository groupEnrollmentRepository;
  private final CourseOfferingRepository courseOfferingRepository;

  public Optional<StudentGroup> resolveGroupAt(UUID studentId, Instant pointInTime) {
    return groupEnrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId).stream()
        .filter(e -> isActiveAt(e, pointInTime))
        .map(GroupEnrollment::getGroup)
        .findFirst();
  }

  public Optional<Track> resolveTrackAt(UUID studentId, Instant pointInTime) {
    return resolveGroupAt(studentId, pointInTime).map(StudentGroup::getTrack);
  }

  List<CourseOffering> resolveApplicableCourses(UUID studentId, Semester semester) {
    Instant anchor = semester.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    Track track = resolveTrackAt(studentId, anchor).orElse(null);
    return courseOfferingRepository.findApplicableForTrack(semester.getId(), track);
  }

  private boolean isActiveAt(GroupEnrollment enrollment, Instant pointInTime) {
    boolean startedByThen = !enrollment.getStartDate().isAfter(pointInTime);
    boolean notYetEnded =
        enrollment.getEndDate() == null || enrollment.getEndDate().isAfter(pointInTime);
    return startedByThen && notYetEnded;
  }
}
