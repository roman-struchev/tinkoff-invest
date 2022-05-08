package com.struchev.invest.repository;

import com.struchev.invest.entity.OrderDomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderDomainEntity, Long> {
}
