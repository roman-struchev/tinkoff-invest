package com.struchev.invest.strategy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategySelector {
    @Getter
    private final List<AStrategy> allStrategies;
    private List<AStrategy> activeStrategies;

    public List<AStrategy> suitableByFigi(String figi, AStrategy.Type type) {
        return activeStrategies.stream()
                .filter(AStrategy::isEnabled)
                .filter(s -> type == null || s.getType() == type)
                .filter(s -> s.isSuitableByFigi(figi))
                .collect(Collectors.toList());
    }

    public List<AStrategy> getActiveStrategies() {
        return activeStrategies;
    }

    public Set<String> getFigiesForActiveStrategies() {
        return getActiveStrategies().stream().flatMap(s -> s.getFigies().keySet().stream()).collect(Collectors.toSet());
    }

    public AStrategy.Type getStrategyType(String name, AStrategy.Type defaultValue) {
        return allStrategies.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst().map(AStrategy::getType).orElse(defaultValue);
    }

    @PostConstruct
    private void init() {
        activeStrategies = allStrategies.stream()
                .filter(AStrategy::isEnabled)
                .peek(s -> {
                    if (s.getType() == AStrategy.Type.instrumentByInstrument && s.getFigies().size() < 2
                            || s.getType() == AStrategy.Type.instrumentByFiat && s.getFigies().size() < 1) {
                        throw new UnsupportedOperationException("Incorrect count of figies in " + s.getName());
                    }
                })
                .peek(s -> log.info("Enabled strategy: {}: {}", s.getName(), s.getFigies().keySet()))
                .collect(Collectors.toList());
    }
}
