package com.study.ecommerce.domain.product.service;

import com.study.ecommerce.domain.category.entity.Category;
import com.study.ecommerce.domain.category.repository.CategoryRepository;
import com.study.ecommerce.domain.member.entity.Member;
import com.study.ecommerce.domain.member.repository.MemberRepository;
import com.study.ecommerce.domain.product.dto.req.ProductCreateRequest;
import com.study.ecommerce.domain.product.dto.req.ProductSearchCondition;
import com.study.ecommerce.domain.product.dto.req.ProductUpdateRequest;
import com.study.ecommerce.domain.product.dto.resp.ProductResponse;
import com.study.ecommerce.domain.product.dto.resp.ProductSummaryDto;
import com.study.ecommerce.domain.product.entity.Product;
import com.study.ecommerce.domain.product.repository.ProductRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(ProductSearchCondition condition, Pageable pageable){
       Page<ProductSummaryDto> productSummaryDtos = productRepository.searchProducts(condition, pageable);

            return productSummaryDtos.map(dto ->new ProductResponse(
                    dto.id(),
                    dto.name(),
                    null,
                    dto.price(),
                    dto.stockQuantity(),
                    dto.status(),
                    dto.categoryName()
            ));
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, String email){
        Member seller = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을수 없습니다"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없습니다"));

        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .status(Product.ProductStatus.ACTIVE)
                .sellerId(seller.getId())
                .categoryId(category.getId())
                .build();

        productRepository.save(product);

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                category.getName()
        );
    }

    @Transactional
    public void deleteProductById(Long id, String email){
        Product product  = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을수 없습니다"));

        Member seller = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을수 없습니다"));

        if (!seller.getEmail().equals(email)){
            throw new IllegalArgumentException("판매자가 아닙니다.");
        }
        product.delete();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을수 없습니다"));

        Category category = null;
        String categoryName = "분류없음";

        if(product.getCategoryId() != null){
            category = categoryRepository.findById(product.getCategoryId())
                    .orElse(null);

            if (category != null){
                categoryName = category.getName();
            }

        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                categoryName
        );
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request, String email){
        //물건 찾고
        //현재 사용자가 판매자 인지 확인
        //카테고리 찾고
        //물건 업데이트 -> jpa더티 체킹, 더티 캐싱
        //그리고 변환
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을수 없습니다"));

        Member seller = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을수 없습니다"));

        if (!seller.getEmail().equals(email)){
            throw new IllegalArgumentException("판매자가 아닙니다.");
        }

        Category category = categoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없습니다"));

        product.update(request.name(), request.description(),
                request.price(), request.stockQuantity());

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                category.getName()
        );
    }

}
