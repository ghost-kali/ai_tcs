package com.ecommerce.order.repository;

import com.ecommerce.order.model.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Page<CustomerOrder> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
}
