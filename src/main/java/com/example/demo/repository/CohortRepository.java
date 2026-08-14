package com.example.demo.repository;

import com.example.demo.model.Cohort;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CohortRepository extends JpaRepository<Cohort, UUID> {}
