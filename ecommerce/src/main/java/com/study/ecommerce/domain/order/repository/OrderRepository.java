package com.study.ecommerce.domain.order.repository;


import com.study.ecommerce.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("")
    Page<Order> findByMemberId(@Param("id") Long id, Pageable pageable);
}
