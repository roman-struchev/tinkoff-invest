package com.struchev.invest.entity;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "offer",
        indexes = {
                @Index(columnList = "figi"),
                @Index(columnList = "strategy"),
                @Index(columnList = "currency")
        })
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class)
public class OrderDomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "figi", nullable = false)
    private String figi;

    @Column(name = "figi_title", nullable = true)
    private String figiTitle;

    @Column(name = "purchase_price", nullable = false, scale = 8, precision = 19)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_commission", nullable = false, scale = 8, precision = 19)
    private BigDecimal purchaseCommission;

    @Column(name = "purchase_date_time", nullable = false)
    private OffsetDateTime purchaseDateTime;

    @Column(name = "sell_price", nullable = true, scale = 8, precision = 19)
    private BigDecimal sellPrice;

    @Column(name = "sell_date_time", nullable = true)
    private OffsetDateTime sellDateTime;

    @Column(name = "sell_profit", nullable = true, scale = 8, precision = 19)
    private BigDecimal sellProfit;

    @Column(name = "sell_commission", nullable = true, scale = 8, precision = 19)
    private BigDecimal sellCommission;

    @Column(name = "strategy", nullable = false)
    private String strategy;

    @Column(name = "currency", nullable = false)
    private String currency;

    private Integer lots;

    @Type(type = "jsonb")
    @Column(name = "details", columnDefinition = "jsonb")
    private OrderDetails details;

    @Version
    private Integer version;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}
