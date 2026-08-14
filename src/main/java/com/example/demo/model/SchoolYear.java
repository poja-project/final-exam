package com.example.demo.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "school_year")
@Getter
@Setter
@NoArgsConstructor
public class SchoolYear {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 20)
  private String label;

  @Column(name = "year_number", nullable = false)
  private Integer yearNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cohort_id", nullable = false)
  private Cohort cohort;
}
