package com.example.demo.repository;

import com.example.demo.model.CourseOffering;
import com.example.demo.model.Track;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, UUID> {

  List<CourseOffering> findBySemester_Id(UUID semesterId);

  @Query(
      """
      SELECT co FROM CourseOffering co
      WHERE co.semester.id = :semesterId
        AND (co.track IS NULL OR co.track = :track)
      """)
  List<CourseOffering> findApplicableForTrack(
      @Param("semesterId") UUID semesterId, @Param("track") Track track);
}
