package com.study.ecommerce.domain.cart.repository;

import com.study.ecommerce.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long id);


    Optional<CartItem> findByCartIdAndProductId(Long id, Long id1);
}
