package com.example.demo.repository;

import com.example.demo.model.SchoolYear;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolYearRepository extends JpaRepository<SchoolYear, UUID> {
  List<SchoolYear> findByCohort_Id(UUID cohortId);

  Optional<SchoolYear> findByCohort_IdAndYearNumber(UUID cohortId, Integer yearNumber);
}
