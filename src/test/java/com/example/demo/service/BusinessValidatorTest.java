package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.demo.exception.DomainException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class BusinessValidatorTest {

  @Test
  void semesterNumberMatchesItsYear() {
    assertDoesNotThrow(() -> BusinessValidator.validateSemesterNumberForYear(1, 1));
    assertDoesNotThrow(() -> BusinessValidator.validateSemesterNumberForYear(2, 1));
    assertDoesNotThrow(() -> BusinessValidator.validateSemesterNumberForYear(3, 2));
    assertDoesNotThrow(() -> BusinessValidator.validateSemesterNumberForYear(4, 2));
    assertDoesNotThrow(() -> BusinessValidator.validateSemesterNumberForYear(5, 3));
    assertDoesNotThrow(() -> BusinessValidator.validateSemesterNumberForYear(6, 3));
  }

  @Test
  void semesterNumberMismatchThrows() {
    assertThrows(
        DomainException.class, () -> BusinessValidator.validateSemesterNumberForYear(3, 1));
    assertThrows(
        DomainException.class, () -> BusinessValidator.validateSemesterNumberForYear(6, 2));
    assertThrows(
        DomainException.class, () -> BusinessValidator.validateSemesterNumberForYear(4, 0));
  }

  @Test
  void coefficientsSumInFractionsAccepted() {
    assertDoesNotThrow(
        () ->
            BusinessValidator.validateCoefficientsSum(
                List.of(new BigDecimal("0.5"), new BigDecimal("0.3"), new BigDecimal("0.2"))));
  }

  @Test
  void coefficientsSumInPercentAccepted() {
    assertDoesNotThrow(
        () ->
            BusinessValidator.validateCoefficientsSum(
                List.of(new BigDecimal("50"), new BigDecimal("30"), new BigDecimal("20"))));
  }

  @Test
  void coefficientsSumNotEqualOneOrHundredThrows() {
    assertThrows(
        DomainException.class,
        () ->
            BusinessValidator.validateCoefficientsSum(
                List.of(new BigDecimal("0.5"), new BigDecimal("0.2"))));
    assertThrows(
        DomainException.class,
        () ->
            BusinessValidator.validateCoefficientsSum(
                List.of(new BigDecimal("50"), new BigDecimal("20"))));
  }

  @Test
  void coefficientRoundingTolerated() {
    assertDoesNotThrow(
        () ->
            BusinessValidator.validateCoefficientsSum(
                List.of(
                    new BigDecimal("0.3333"), new BigDecimal("0.3333"), new BigDecimal("0.3333"))));
  }

  @Test
  void percentCoefficientsNormalizedToFractions() {
    assertEquals(
        List.of(new BigDecimal("0.5"), new BigDecimal("0.3"), new BigDecimal("0.2")),
        BusinessValidator.normalizeCoefficients(
            List.of(new BigDecimal("50"), new BigDecimal("30"), new BigDecimal("20"))));
    assertEquals(
        List.of(new BigDecimal("0.5"), new BigDecimal("0.3"), new BigDecimal("0.2")),
        BusinessValidator.normalizeCoefficients(
            List.of(new BigDecimal("0.5"), new BigDecimal("0.3"), new BigDecimal("0.2"))));
  }

  @Test
  void gradeValueMustBeBetweenZeroAndTwenty() {
    assertDoesNotThrow(() -> BusinessValidator.validateGradeValue(new BigDecimal("0")));
    assertDoesNotThrow(() -> BusinessValidator.validateGradeValue(new BigDecimal("20")));
    assertThrows(
        DomainException.class, () -> BusinessValidator.validateGradeValue(new BigDecimal("20.5")));
    assertThrows(
        DomainException.class, () -> BusinessValidator.validateGradeValue(new BigDecimal("-0.1")));
    assertThrows(DomainException.class, () -> BusinessValidator.validateGradeValue(null));
  }

  @Test
  void reasonMandatoryOnlyOnModification() {
    assertDoesNotThrow(() -> BusinessValidator.validateReasonRequired(false, null));
    assertDoesNotThrow(() -> BusinessValidator.validateReasonRequired(false, ""));
    assertDoesNotThrow(() -> BusinessValidator.validateReasonRequired(true, "erratum"));
    assertThrows(DomainException.class, () -> BusinessValidator.validateReasonRequired(true, null));
    assertThrows(DomainException.class, () -> BusinessValidator.validateReasonRequired(true, "  "));
  }

  @Test
  void creditsPerSemesterMustNotExceedThirty() {
    assertDoesNotThrow(() -> BusinessValidator.validateCreditsPerSemester(30));
    assertDoesNotThrow(() -> BusinessValidator.validateCreditsPerSemester(0));
    assertThrows(DomainException.class, () -> BusinessValidator.validateCreditsPerSemester(31));
  }
}
