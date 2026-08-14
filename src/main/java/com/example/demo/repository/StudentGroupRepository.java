package com.example.demo.repository;

import com.example.demo.model.StudentGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, UUID> {
  List<StudentGroup> findByCohort_Id(UUID cohortId);
}
