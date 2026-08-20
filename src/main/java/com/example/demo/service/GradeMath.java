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

  public static BigDecimal weightedMean(List<BigDecimal> values, List<BigDecimal> weights) {
    if (values == null || weights == null || values.isEmpty() || values.size() != weights.size()) {
      return BigDecimal.ZERO;
    }
    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalWeight = BigDecimal.ZERO;
    for (int i = 0; i < values.size(); i++) {
      weightedSum = weightedSum.add(values.get(i).multiply(weights.get(i)));
      totalWeight = totalWeight.add(weights.get(i));
    }
    if (totalWeight.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return weightedSum.divide(totalWeight, SCALE, RoundingMode.HALF_UP);
  }
}
