package com.example.demo.repository;

import com.example.demo.model.Course;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {
  boolean existsByReference(String reference);
}
