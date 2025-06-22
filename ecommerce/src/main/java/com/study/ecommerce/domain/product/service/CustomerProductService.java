package com.study.ecommerce.domain.product.service;

import com.study.ecommerce.domain.category.entity.Category;
import com.study.ecommerce.domain.category.repository.CategoryRepository;
import com.study.ecommerce.domain.payment.entity.Payment;
import com.study.ecommerce.domain.product.dto.resp.ProductResponse;
import com.study.ecommerce.domain.product.entity.Product;
import com.study.ecommerce.domain.product.entity.Product.ProductStatus.*;
import com.study.ecommerce.domain.product.repository.ProductRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.study.ecommerce.domain.product.entity.Product.ProductStatus.ACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 카테고리별 판매중인 상품을 전체 조회
     * List<ProductResponse> getActiveProductByCategory
     * param Long categoryId
     */

    public List<ProductResponse> getActiveProductByCategory(Long categoryId){
        //카테고리의 존재 유무
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하는 카테고리 없음"));

        //해당 카테고리의 판매중인 상품 찾기
        List<Product>products = productRepository.findByCategoryIdAndStatus(categoryId, ACTIVE);
        return products.stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStockQuantity(),
                        product.getStatus(),
                        category.getName()
                )).toList();
    }

    /**
     * 페이지 모든 판매중인 상품 조회  -> pageable
     * Page<ProductResponse> getAllActiveProducts
     */
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable){
        //페이지에  판맨중인 물건 찾기
        Page<Product> products = productRepository.findByStatus(ACTIVE, pageable);

        //물건이 비워 있으면 카테고리는 분류 없음 나머지는 물건의 기본 정보 가져오기
        if (products.isEmpty()){ //product를 empty를 하면
            return products.map(product -> new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getStatus(),
                    "분류 없음"
            ));
        }

        //카테고리 정보 조회 효율적으로 조회를 하기 위해서 -맵 생성
        List<Long> categoryIds = products.getContent().stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long,String> categoryMap = new HashMap<>();
        if (!categoryMap.isEmpty()){
            List<Category>categories = categoryRepository.findAllById(categoryIds);
            categories.forEach(category ->
                    categoryMap.put(category.getId(), category.getName()));
        }

        Page<ProductResponse> result = products.map(product ->{
            String categoryName = "분류 없음";
            if(product.getCategoryId() != null){
                categoryName = categoryMap.getOrDefault(product.getCategoryId(), "분류 없음");
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
        });
        return result;
    }

    /**
     * 판매중인 상품 상세조회  -> id, ProductResponse 반환
     */
    public ProductResponse getActiveProduct(Long id){
        //판매중인 물건 찾기
        Product product = productRepository.findByIdAndStatus(id, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("판매중인 상품을 찾을 수 없습니다."));

        //상품의 카테고리 찾기
        String categoryName = "분류 없음";
        if (product.getCategoryId() != null) {
            Category category = categoryRepository.findById(product.getCategoryId())
                    .orElse(null);

            if (category != null) {
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

    /**
     * 상품명으로 판매중인 상품을 검색
     * Page<ProductResponse> param Pageable String keyword
     */
    public Page<ProductResponse> getActiveProduct(Pageable pageable, String keyword){

        //키워드 글자에 대한 조건 검사
        if (keyword == null || keyword.isBlank() || keyword.trim().length() < 2) {
            throw new IllegalArgumentException("검색어는 두 글자 이상 입력해주세요.");
        }

        // 판매중이며 상품명에 키워드가 포함된 상품 검색
        Page<Product> products = productRepository.findByStatusAndNameLikeIgnoreCase(
                ACTIVE, keyword.trim(), pageable
        );

        //물건이 비워 있으면 카테고리는 분류 없음 나머지는 물건의 기본 정보 가져오기
        if (products.isEmpty()){
            return products.map(product -> new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getStatus(),
                    "분류 없음"
            ));
        }

        return products.map(product ->
                new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStockQuantity(),
                        product.getStatus(),
                        product.getCategoryId() != null ? product.getCategoryId().toString() : "분류 없음"
                ));
    }

    /**
     * 가격 범위로 판매중인 상품 검색
     * Long minPrice, Long maxPrice
     */
    public List<ProductResponse> getPriceBetweenProduct(Long minPrice, Long maxPrice){
        //가격에대한 조건 범위 설정
        if (minPrice < 0 || maxPrice > 1000000) {
            throw new IllegalArgumentException("가격을 다시 조정해주새요 ");
        }

        //가격 범위 에 맞는 물건 찾기
        List<Product> product = productRepository.findPriceBetweenProduct(minPrice, maxPrice);

        if (product.isEmpty()) {
            throw new EntityNotFoundException("해당 가격 범위에 맞는 상품이 없습니다.");
        }

        return product.stream()
                .map(products -> new ProductResponse(
                        products.getId(),
                        products.getName(),
                        products.getDescription(),
                        products.getPrice(),
                        products.getStockQuantity(),
                        products.getStatus(),
                        products.getCategoryId() != null ? products.getCategoryId().toString() : "분류 없음"
                        ))
                .collect(Collectors.toList());
    }

    /**
     *  카테고리 내에서 상품명으로 검색
     *  categoryId, keyword
     */
    public ProductResponse getCategoryProduct(Long categoryId, String keyword){
        //카테고리 전제 유무
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->  new EntityNotFoundException("존재 하지 않은 카테고리 입니다"));

        //키워드 글자에 대한 조건 검사 + 추가로 정규식을 입력
        if (keyword == null || keyword.isBlank() ){
            throw  new EntityNotFoundException("존재하지는 상품이 없습니다" +keyword);
        }
        if ( keyword.trim().length() < 2) {
            throw new IllegalArgumentException("검색어는 두 글자 이상 입력해주세요.");
        }

        Product product = productRepository
                .findByCategoryAndNameContainingAndStatus(categoryId, keyword.trim(), ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("해당 조건에 맞는 상품이 없습니다."));

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

    /**
     *  extract method
     *  Product Page를 ProductResponse의 Page 변환하는 공통 메소드 구현
     *  Page<Product> -> Page<ProductResponse>
     */


}
