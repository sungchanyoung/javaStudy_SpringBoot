package com.study.ecommerce.domain.product.repository;

import com.study.ecommerce.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryRepository{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long productId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithOptimisticLock(@Param("id") Long productId);

}
