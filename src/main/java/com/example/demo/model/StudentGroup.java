package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_group")
@Getter
@Setter
@NoArgsConstructor
public class StudentGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 50)
  private String reference;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private Track track;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cohort_id", nullable = false)
  private Cohort cohort;
}
