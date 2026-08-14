package com.example.demo.service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GraduateResult(
    int rank,
    UUID studentId,
    String studentNumber,
    String lastName,
    String firstName,
    BigDecimal overallAverage) {}
