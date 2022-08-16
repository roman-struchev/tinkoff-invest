package com.struchev.invest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReportInstrumentByInstrumentRow {
    private String strategy;

    private String initFigiTitle;
    private String lastFigiTitle;

    private BigDecimal initAmount;
    private BigDecimal lastAmount;
    private Integer orders;
    private BigDecimal percent;
    private BigDecimal commission;

    private OffsetDateTime initDate;
    private Duration duration;
}
