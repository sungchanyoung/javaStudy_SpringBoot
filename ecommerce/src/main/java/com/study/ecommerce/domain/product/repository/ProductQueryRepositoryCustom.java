package com.study.ecommerce.domain.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.ecommerce.domain.category.entity.QCategory;
import com.study.ecommerce.domain.product.dto.req.ProductSearchCondition;
import com.study.ecommerce.domain.product.dto.resp.ProductSummaryDto;
import com.study.ecommerce.domain.product.entity.Product;
import com.study.ecommerce.domain.product.entity.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class ProductQueryRepositoryCustom  implements  ProductQueryRepository{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSummaryDto> searchProducts(
            ProductSearchCondition condition, Pageable pageable
    ) {
        QProduct product = QProduct.product;
        QCategory category = QCategory.category;

        //동적 쿼리 생성하기 위한 조건
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(condition.keyword())){
            builder.and(product.name.containsIgnoreCase(condition.keyword()))
                    .or(product.description.containsIgnoreCase(condition.keyword()));
        }

        if (condition.categoryId() != null){
            builder.and(product.categoryId.eq(condition.categoryId()));
        }

        if (condition.minPrice() != null){
            builder.and(product.price.goe(condition.minPrice()));
        }

        if (condition.maxPrice() != null){
            builder.and(product.price.loe(condition.maxPrice()));
        }

        if (condition.sellerId() != null){
            builder.and(product.sellerId.eq(condition.sellerId()));
        }

        builder.and(product.status.eq(Product.ProductStatus.ACTIVE));

        //전체 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(builder);

        //조인 없이 카테고리 이름 가져오기, 서브 퀄리 방식
        List<ProductSummaryDto> summaryDtos = queryFactory
                .select(Projections.constructor(ProductSummaryDto.class,
                        product.id,
                        product.name,
                        product.price,
                        product.stockQuantity,
                        Expressions.asString("Category").as("categoryName"),
                        product.status))
                .from(product)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable, product))
                .fetch(); //단일 값은 fetone, fetch()LIst형태로 가져옴

        //실제 조회 쿼리
        return PageableExecutionUtils.getPage(summaryDtos, pageable,countQuery::fetchOne);

    }

    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable, QProduct product) {
        if (!pageable.getSort().isEmpty()) {
            for (Sort.Order order : pageable.getSort()) {
                switch (order.getProperty()) {
                    case "price" :
                        return order.isAscending() ? product.price.asc() : product.price.desc();

                    case "createdAt":
                        return order.isAscending() ? product.createdAt.asc() : product.createdAt.desc();

                    default:
                        return product.id.desc();
                }
            }
        }

        return product.id.desc();
    }
}
