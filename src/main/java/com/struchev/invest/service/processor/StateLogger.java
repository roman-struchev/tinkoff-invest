package com.struchev.invest.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class StateLogger {
    private final Map<String, OffsetDateTime> lastLogExecutions = new ConcurrentHashMap<>();

    private void logStateIfSellingStuck(String strategy, String figi, OffsetDateTime purchaseDate,
                                        BigDecimal purchasePrice, Map<String, Double> changePercents)
            throws JsonProcessingException {
        // No notification is needed if the purchase was made yesterday or earlier
        if (purchaseDate.isAfter(OffsetDateTime.now().minusDays(1))) {
            return;
        }

        // No notification is needed if notification was already sent for last hour
        var lastLogDate = lastLogExecutions.getOrDefault(strategy + figi, OffsetDateTime.MIN);
        if (lastLogDate.isAfter(OffsetDateTime.now().minusHours(1))) {
            return;
        }
        var changePercentsStr = new ObjectMapper().writeValueAsString(changePercents);
        log.info("Position selling is to long {}, {} (price {}, date {}). Changes by figies: {}",
                strategy, figi, purchasePrice, purchaseDate, changePercentsStr);

    }

    public void logStateIfSellingStuckNoThrow(String strategy, String figi, OffsetDateTime purchaseDate,
                                              BigDecimal purchasePrice, Map<String, Double> changePercents) {
        try {
            logStateIfSellingStuck(strategy, figi, purchaseDate, purchasePrice, changePercents);
        } catch (Exception e) {
            log.error("An error occurred while", e);
        }
    }
}
