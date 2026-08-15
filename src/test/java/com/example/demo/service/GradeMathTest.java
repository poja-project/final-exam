package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class GradeMathTest {

  @Test
  void emptyListReturnsZero() {
    assertEquals(BigDecimal.ZERO, GradeMath.arithmeticMean(List.of()));
    assertEquals(BigDecimal.ZERO, GradeMath.arithmeticMean(null));
  }

  @Test
  void arithmeticMeanIsUnweighted() {
    assertEquals(
        new BigDecimal("13.00"),
        GradeMath.arithmeticMean(
            List.of(new BigDecimal("10"), new BigDecimal("12"), new BigDecimal("17"))));
  }

  @Test
  void arithmeticMeanIgnoresCredits() {
    assertEquals(
        new BigDecimal("13.00"),
        GradeMath.arithmeticMean(List.of(new BigDecimal("8"), new BigDecimal("18"))));
  }

  @Test
  void meanRoundedHalfUp() {
    assertEquals(
        new BigDecimal("10.33"),
        GradeMath.arithmeticMean(
            List.of(new BigDecimal("10"), new BigDecimal("10.5"), new BigDecimal("10.5"))));
  }
}
