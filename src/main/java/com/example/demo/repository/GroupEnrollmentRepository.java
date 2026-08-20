package com.example.demo.repository;

import com.example.demo.model.GroupEnrollment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupEnrollmentRepository extends JpaRepository<GroupEnrollment, UUID> {

  Optional<GroupEnrollment> findByStudent_IdAndEndDateIsNull(UUID studentId);

  List<GroupEnrollment> findByStudent_IdOrderByStartDateAsc(UUID studentId);
}
