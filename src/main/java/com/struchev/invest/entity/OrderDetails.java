package com.struchev.invest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetails {
    Map<String, BigDecimal> currentPrices;
    Map<String, BigDecimal> sellPrices;
}