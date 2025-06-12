package com.study.ecommerce.domain.cart.service;

import com.study.ecommerce.domain.cart.dto.CartItemRequest;
import com.study.ecommerce.domain.cart.dto.CartItemResponse;
import com.study.ecommerce.domain.cart.dto.CartResponse;
import com.study.ecommerce.domain.cart.entity.Cart;
import com.study.ecommerce.domain.cart.entity.CartItem;
import com.study.ecommerce.domain.cart.repository.CartItemRepository;
import com.study.ecommerce.domain.cart.repository.CartRepository;
import com.study.ecommerce.domain.member.entity.Member;
import com.study.ecommerce.domain.member.repository.MemberRepository;
import com.study.ecommerce.domain.product.entity.Product;
import com.study.ecommerce.domain.product.repository.ProductRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceCustom implements CartService{

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;


    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("장바구니를 찾을 수 없습니다."));

        // 장바구니의 아이템을 별도로 조회
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> items = cartItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

                    return new CartItemResponse(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            item.getQuantity(),
                            product.getPrice() * item.getQuantity()
                    );
                })
                .toList();

        long totalPrice = items.stream()
                .mapToLong(CartItemResponse::totalPrice)
                .sum();

        return new CartResponse(cart.getId(), totalPrice, items);
    }

    @Override
    @Transactional
    public CartItemResponse addCartItem(CartItemRequest request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart(member.getId());
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        //이미 장바구니에 있는 상품인지 ?
        CartItem cartItem = null;
        if(cartItemRepository.existsByCartIdAndProductId(cart.getId(), product.getId())){
           cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                   .orElseThrow(() -> new EntityNotFoundException("장바구니에 상품을 찾을수 없습니다"));

           cartItem.updateQuantity(cartItem.getQuantity() + request.quantity());
        }else{
           CartItem.builder()
                   .cartId(cart.getId())
                   .productId(product.getId())
                   .quantity(request.quantity())
                   .build();
        }

        cartItemRepository.save(cartItem);

        return new CartItemResponse(
                cart.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                product.getPrice() * cartItem.getQuantity()

        );

    }

    @Override
    public CartItemResponse updateCartItem(Long cartItemId, CartItemRequest request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 항목을 찾을 수 없습니다."));

        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("장바구니를 찾을 수 없습니다."));

        if (!cartItem.getCartId().equals(cart.getId())) {
            throw new IllegalArgumentException("사용자의 장바구니 항목이 아닙니다.");
        }

        cartItem.updateQuantity(request.quantity());
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을수 없습니다"));

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                product.getPrice() * cartItem.getQuantity()
        );

    }

    @Override
    public void removeCartItem(Long cartItemId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("장바구니를 찾을 수 없습니다."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 상품을 찾을 수 없습니다."));

        if (!cartItem.getCartId().equals(cart.getId())) {
            throw new IllegalArgumentException("장바구니 상품 삭제 권한이 없습니다.");
        }

        cartItemRepository.delete(cartItem);
    }
}
