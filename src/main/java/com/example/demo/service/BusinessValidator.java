package com.example.demo.service;

import com.example.demo.exception.DomainException;
import java.math.BigDecimal;
import java.util.List;

public final class BusinessValidator {

  private static final BigDecimal COEFF_TOLERANCE = new BigDecimal("0.0001");
  private static final BigDecimal ONE = BigDecimal.ONE;
  private static final BigDecimal HUNDRED = new BigDecimal("100");
  private static final BigDecimal MAX_GRADE = new BigDecimal("20");
  private static final int MAX_CREDITS_PER_SEMESTER = 30;

  private BusinessValidator() {}

  public static void validateSemesterNumberForYear(int semesterNumber, int yearNumber) {
    if (yearNumber < 1 || yearNumber > 3) {
      throw DomainException.badRequest("Year number must be between 1 and 3, got: " + yearNumber);
    }
    int first = (yearNumber - 1) * 2 + 1;
    int second = first + 1;
    if (semesterNumber != first && semesterNumber != second) {
      throw DomainException.badRequest(
          "Semester "
              + semesterNumber
              + " does not belong to year "
              + yearNumber
              + " (expected "
              + first
              + " or "
              + second
              + ")");
    }
  }

  public static void validateCoefficientsSum(List<BigDecimal> coefficients) {
    BigDecimal sum = coefficients.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    if (isCloseTo(sum, ONE) || isCloseTo(sum, HUNDRED)) {
      return;
    }
    throw DomainException.unprocessable(
        "The sum of the coefficients must be 1 (fraction) or 100 (percent), got: " + sum);
  }

  public static List<BigDecimal> normalizeCoefficients(List<BigDecimal> coefficients) {
    BigDecimal sum = coefficients.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    if (isCloseTo(sum, HUNDRED)) {
      return coefficients.stream().map(c -> c.divide(HUNDRED)).toList();
    }
    return coefficients;
  }

  public static void validateGradeValue(BigDecimal value) {
    if (value == null || value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(MAX_GRADE) > 0) {
      throw DomainException.badRequest("Grade value must be between 0 and 20, got: " + value);
    }
  }

  public static void validateReasonRequired(boolean gradeAlreadyExists, String reason) {
    if (gradeAlreadyExists && (reason == null || reason.isBlank())) {
      throw DomainException.badRequest("A reason is mandatory when modifying an existing grade");
    }
  }

  public static void validateCreditsPerSemester(int totalCredits) {
    if (totalCredits > MAX_CREDITS_PER_SEMESTER) {
      throw DomainException.badRequest(
          "The total credits of a semester cannot exceed "
              + MAX_CREDITS_PER_SEMESTER
              + ", got: "
              + totalCredits);
    }
  }

  private static boolean isCloseTo(BigDecimal value, BigDecimal target) {
    return value.subtract(target).abs().compareTo(COEFF_TOLERANCE) <= 0;
  }
}
