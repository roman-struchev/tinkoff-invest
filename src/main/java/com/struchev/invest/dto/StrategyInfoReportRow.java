package com.struchev.invest.dto;

import com.struchev.invest.strategy.instrument_by_fiat.AInstrumentByFiatStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyInfoReportRow {
    private boolean isEnabled;
    private String name;
    private Map<String, Integer> figies;
    private String type;
    private AInstrumentByFiatStrategy.BuyCriteria buyCriteria;
    private AInstrumentByFiatStrategy.SellCriteria sellCriteria;
    private Duration history;
    private Float dropPercent;
}
