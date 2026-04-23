package com.ecommerce.order.repository;

import com.ecommerce.order.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByOrderOrderId(Long orderId);
}
