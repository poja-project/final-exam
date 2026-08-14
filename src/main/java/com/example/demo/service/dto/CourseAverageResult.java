package com.example.demo.service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseAverageResult(
    UUID courseId, String courseTitle, Integer credit, BigDecimal average, boolean complete) {}
