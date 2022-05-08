package com.struchev.invest.repository;

import com.struchev.invest.entity.CandleDomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface CandleRepository extends JpaRepository<CandleDomainEntity, Long> {
    List<CandleDomainEntity> findByFigiAndIntervalOrderByDateTime(String figi, String interval);

    CandleDomainEntity findByFigiAndIntervalAndDateTime(String figi, String interval, OffsetDateTime dateTime);

    @Query("select c from CandleDomainEntity c where c.figi = :figi AND c.interval = :interval " +
            "AND c.dateTime >= :startDateTime AND c.dateTime <= :endDateTime ORDER BY c.dateTime")
    List<CandleDomainEntity> findByFigiAndIntervalAndBetweenDateTimes(String figi, String interval, OffsetDateTime startDateTime, OffsetDateTime endDateTime);

    List<CandleDomainEntity> findByFigiAndIntervalAndDateTimeAfterOrderByDateTime(String figi, String interval, OffsetDateTime startDateTime);

}
