package com.study.ecommerce.domain.payment.repository;

import com.study.ecommerce.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
