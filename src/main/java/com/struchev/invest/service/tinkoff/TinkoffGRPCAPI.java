package com.struchev.invest.service.tinkoff;

import com.struchev.invest.service.candle.ConvertorUtils;
import com.struchev.invest.service.dictionary.InstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "tinkoff.emulator", havingValue = "false")
@RequiredArgsConstructor
public class TinkoffGRPCAPI extends ATinkoffAPI {

    public OrderResult buy(InstrumentService.Instrument instrument, BigDecimal price, Integer count) {
        long quantity = count / instrument.getLot();
        var quotation = Quotation.newBuilder()
                .setUnits(price.longValue())
                .setNano(price.remainder(BigDecimal.ONE).movePointRight(9).intValue())
                .build();
        var uuid = UUID.randomUUID().toString();
        log.info("Send postOrderSync with: figi {}, quantity {}, quotation {}, direction {}, acc {}, type {}, id {}",
                instrument.getFigi(), quantity, quotation, OrderDirection.ORDER_DIRECTION_BUY, getAccountId(), OrderType.ORDER_TYPE_MARKET, uuid);

        if (getIsSandboxMode()) {
            var result = getApi().getSandboxService().postOrderSync(instrument.getFigi(), quantity, quotation,
                    OrderDirection.ORDER_DIRECTION_BUY, getAccountId(), OrderType.ORDER_TYPE_MARKET, uuid);
            return OrderResult.builder()
                    .commission(ConvertorUtils.toBigDecimal(result.getInitialCommission(), 8))
                    .price(getOrderPrice(result.getInitialOrderPricePt(), result.getExecutedOrderPrice(), price, count))
                    .build();
        } else {
            var result = getApi().getOrdersService().postOrderSync(instrument.getFigi(), quantity, quotation,
                    OrderDirection.ORDER_DIRECTION_BUY, getAccountId(), OrderType.ORDER_TYPE_MARKET, uuid);
            return OrderResult.builder()
                    .commission(ConvertorUtils.toBigDecimal(result.getInitialCommission(), 8))
                    .price(getOrderPrice(result.getInitialOrderPricePt(), result.getExecutedOrderPrice(), price, count))
                    .build();
        }
    }

    public OrderResult sell(InstrumentService.Instrument instrument, BigDecimal price, Integer count) {
        long quantity = count / instrument.getLot();
        var quotation = Quotation.newBuilder()
                .setUnits(price.longValue())
                .setUnits(price.remainder(BigDecimal.ONE).movePointRight(9).intValue())
                .build();
        var uuid = UUID.randomUUID().toString();
        log.info("Send postOrderSync with: figi {}, quantity {}, quotation {}, direction {}, acc {}, type {}, id {}",
                instrument.getFigi(), quantity, quotation, OrderDirection.ORDER_DIRECTION_SELL, getAccountId(), OrderType.ORDER_TYPE_MARKET, uuid);

        if (getIsSandboxMode()) {
            var result = getApi().getSandboxService().postOrderSync(instrument.getFigi(), quantity, quotation,
                    OrderDirection.ORDER_DIRECTION_SELL, getAccountId(), OrderType.ORDER_TYPE_MARKET, uuid);
            return OrderResult.builder()
                    .commission(ConvertorUtils.toBigDecimal(result.getInitialCommission(), 8))
                    .price(getOrderPrice(result.getInitialOrderPricePt(), result.getExecutedOrderPrice(), price, count))
                    .build();
        } else {
            var result = getApi().getOrdersService().postOrderSync(instrument.getFigi(), quantity, quotation,
                    OrderDirection.ORDER_DIRECTION_SELL, getAccountId(), OrderType.ORDER_TYPE_MARKET, uuid);
            return OrderResult.builder()
                    .commission(ConvertorUtils.toBigDecimal(result.getInitialCommission(), 8))
                    .price(getOrderPrice(result.getInitialOrderPricePt(), result.getExecutedOrderPrice(), price, count))
                    .build();
        }
    }

    /**
     * Цена за один инструмент по результату заявки
     * Фьючерсы торгуются в пунктах, свечи и стратегии оперируют ими же, поэтому для них берем стоимость
     * заявки в пунктах (initial_order_price_pt) и делим на количество инструментов
     * Для остальных инструментов это поле не заполняется, берем среднюю цену исполнения (executed_order_price),
     * а если и ее нет (песочница отдает 0) - цену, по которой отправляли заявку
     */
    private BigDecimal getOrderPrice(Quotation initialOrderPricePt, MoneyValue executedOrderPrice,
                                     BigDecimal price, Integer count) {
        var orderPricePt = ConvertorUtils.toBigDecimal(initialOrderPricePt, 4);
        if (orderPricePt.compareTo(BigDecimal.ZERO) != 0) {
            return orderPricePt.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_EVEN);
        }
        return ConvertorUtils.toBigDecimal(executedOrderPrice, 4, price);
    }
}
