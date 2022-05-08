package com.struchev.invest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReportInstrumentByFiatRow {
    private String figiTitle;
    private String strategy;
    private BigDecimal firstPrice;
    private BigDecimal lastPrice;
    private BigDecimal profitByRobot;
    private BigDecimal profitByInvest;
    private Integer orders;
}
