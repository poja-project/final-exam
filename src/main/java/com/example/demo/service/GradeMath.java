package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class GradeMath {

  private static final int SCALE = 2;

  private GradeMath() {}

  public static BigDecimal arithmeticMean(List<BigDecimal> values) {
    if (values == null || values.isEmpty()) {
      return BigDecimal.ZERO;
    }
    BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.divide(BigDecimal.valueOf(values.size()), SCALE, RoundingMode.HALF_UP);
  }
}
