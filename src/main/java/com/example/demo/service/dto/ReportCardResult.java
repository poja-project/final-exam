package com.example.demo.service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReportCardResult(
    UUID studentId,
    UUID schoolYearId,
    List<CourseAverageResult> courses,
    BigDecimal overallAverage,
    int creditsEarned,
    boolean complete) {}
