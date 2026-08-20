package com.example.demo.repository;

import com.example.demo.model.Student;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {
  Optional<Student> findByUser_Id(UUID userId);

  List<Student> findByCohort_Id(UUID cohortId);

  boolean existsByStudentNumber(String studentNumber);
}
