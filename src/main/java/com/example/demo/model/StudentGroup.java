package com.example.demo.model;

import jakarta.persistence.*;
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
  private Track track; // nullable before semester 5

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cohort_id", nullable = false)
  private Cohort cohort;
}
