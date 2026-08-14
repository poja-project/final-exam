package com.example.demo.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cohort")
@Getter
@Setter
@NoArgsConstructor
public class Cohort {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String label;

  @Column(name = "entry_year", nullable = false)
  private Integer entryYear;
}
