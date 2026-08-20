package com.example.demo.repository;

import com.example.demo.model.Teacher;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
  Optional<Teacher> findByUser_Id(UUID userId);
}
