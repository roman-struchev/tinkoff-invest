package com.struchev.invest.service.order;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.entity.OrderDetails;
import com.struchev.invest.entity.OrderDomainEntity;
import com.struchev.invest.repository.OrderRepository;
import com.struchev.invest.service.dictionary.InstrumentService;
import com.struchev.invest.service.tinkoff.ITinkoffOrderAPI;
import com.struchev.invest.strategy.AStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final InstrumentService instrumentService;
    private final ITinkoffOrderAPI tinkoffOrderAPI;

    private volatile List<OrderDomainEntity> orders;

    public OrderDomainEntity findActiveByFigiAndStrategy(String figi, AStrategy strategy) {
        return orders.stream()
                .filter(o -> figi == null || o.getFigi().equals(figi))
                .filter(o -> o.getSellDateTime() == null)
                .filter(o -> o.getStrategy().equals(strategy.getName()))
                .findFirst().orElse(null);
    }

    public List<OrderDomainEntity> findClosedByFigiAndStrategy(String figi, AStrategy strategy) {
        return orders.stream()
                .filter(o -> o.getFigi().equals(figi))
                .filter(o -> o.getSellDateTime() != null)
                .filter(o -> o.getStrategy().equals(strategy.getName()))
                .collect(Collectors.toList());
    }

    public OrderDomainEntity findLastByFigiAndStrategy(String figi, AStrategy strategy) {
        return orders.stream()
                .filter(o -> figi == null || o.getFigi().equals(figi))
                .filter(o -> o.getSellDateTime() != null)
                .filter(o -> o.getStrategy().equals(strategy.getName()))
                .reduce((first, second) -> second).orElse(null);
    }

    @Transactional
    public synchronized OrderDomainEntity openOrder(CandleDomainEntity candle, AStrategy strategy, Map<String, BigDecimal> currentPrices) {
        if (currentPrices != null) {
            currentPrices = currentPrices.entrySet().stream()
                    .filter(e -> strategy.getFigies().containsKey(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        var instrument = instrumentService.getInstrument(candle.getFigi());
        var order = OrderDomainEntity.builder()
                .currency(instrument.getCurrency())
                .figi(instrument.getFigi())
                .figiTitle(instrument.getName())
                .purchasePrice(candle.getClosingPrice())
                .strategy(strategy.getName())
                .purchaseDateTime(candle.getDateTime())
                .lots(strategy.getCount(candle.getFigi()))
                .purchaseCommission(BigDecimal.ZERO)
                .details(OrderDetails.builder().currentPrices(currentPrices).build())
                .build();

        var result = tinkoffOrderAPI.buy(instrument, candle.getClosingPrice(), order.getLots());
        order.setPurchaseCommission(result.getCommission());
        order.setPurchasePrice(result.getPrice());
        order = orderRepository.save(order);

        orders.add(order);
        return order;
    }

    @Transactional
    public synchronized OrderDomainEntity closeOrder(CandleDomainEntity candle, AStrategy strategy) {
        var instrument = instrumentService.getInstrument(candle.getFigi());
        var order = findActiveByFigiAndStrategy(candle.getFigi(), strategy);
        order.setSellPrice(candle.getClosingPrice());
        order.setSellProfit(candle.getClosingPrice().subtract(order.getPurchasePrice()));
        order.setSellDateTime(candle.getDateTime());

        var result = tinkoffOrderAPI.sell(instrument, candle.getClosingPrice(), order.getLots());
        order.setSellCommission(result.getCommission());
        order.setSellPrice(result.getPrice());
        order.setSellProfit(result.getPrice().subtract(order.getPurchasePrice()));
        order = orderRepository.save(order);

        var orderId = order.getId();
        var orderInList = orders.stream().filter(o -> o.getId().equals(orderId)).findFirst().orElseThrow();
        orders.remove(orderInList);
        orders.add(order);
        return order;
    }

    @PostConstruct
    public void loadOrdersFromDB() {
        orders = new CopyOnWriteArrayList();
        orders.addAll(orderRepository.findAll(Sort.by("id")));
    }
}
