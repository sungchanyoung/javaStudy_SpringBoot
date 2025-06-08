package com.study.ecommerce.domain.order.service;

import com.study.ecommerce.domain.cart.repository.CartItemRepository;
import com.study.ecommerce.domain.cart.repository.CartRepository;
import com.study.ecommerce.domain.member.repository.MemberRepository;
import com.study.ecommerce.domain.order.dto.OrderCreateRequest;
import com.study.ecommerce.domain.order.dto.OrderDetailResponse;
import com.study.ecommerce.domain.order.dto.OrderResponse;
import com.study.ecommerce.domain.order.repository.OrderItemRepository;
import com.study.ecommerce.domain.order.repository.OrderRepository;
import com.study.ecommerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;

@Service
@RequiredArgsConstructor
public class OrderServiceCustom implements OrderService{
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


    @Override
    public OrderResponse createOrder(OrderCreateRequest request, String email) {
        return null;
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, String email) {
        return null;
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderId, String email) {
        return null;
    }

    @Override
    public Page<OrderResponse> getOrders(String email, Pageable pageable) {
        return null;
    }
}
