package com.example.demo.repository;

import com.example.demo.model.TeacherAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, UUID> {

  List<TeacherAssignment> findByTeacher_Id(UUID teacherId);

  List<TeacherAssignment> findBySemester_Id(UUID semesterId);

  boolean existsByTeacher_IdAndCourse_Id(UUID teacherId, UUID courseId);
}
