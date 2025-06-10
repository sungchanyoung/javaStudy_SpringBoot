package com.study.ecommerce.domain.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.ecommerce.domain.category.entity.QCategory;
import com.study.ecommerce.domain.member.entity.QMember;
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

import java.math.BigDecimal;
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
        QMember member = QMember.member;

//       동적 쿼리 생성하기 위한 조건 = where절 을 만드는 도구이다.
//       유연성,재사용성,가독성
//        BooleanBuilder builder = new BooleanBuilder();
//
//        if (StringUtils.hasText(condition.keyword())){
//            builder.and(product.name.containsIgnoreCase(condition.keyword()))
//                    .or(product.description.containsIgnoreCase(condition.keyword()));
//        }
//
//        if (condition.categoryId() != null){
//            builder.and(product.categoryId.eq(condition.categoryId()));
//        }
//
//        if (condition.minPrice() != null){
//            builder.and(product.price.goe(condition.minPrice()));
//        }
//
//        if (condition.maxPrice() != null){
//            builder.and(product.price.loe(condition.maxPrice()));
//        }
//
//        if (condition.sellerId() != null){
//            builder.and(product.sellerId.eq(condition.sellerId()));
//        }
//
//        builder.and(product.status.eq(Product.ProductStatus.ACTIVE));
//
//        //전체 카운트 쿼리
//        JPAQuery<Long> countQuery = queryFactory
//                .select(product.count())
//                .from(product)
//                .where(builder);
//
//        //조인 없이 카테고리 이름 가져오기, 서브 퀄리 방식
//        List<ProductSummaryDto> summaryDtos = queryFactory
//                .select(Projections.constructor(ProductSummaryDto.class,
//                        product.id,
//                        product.name,
//                        product.price,
//                        product.stockQuantity,
//                        //카테고림 이름 대신 상수 값을 반환
//                        Expressions.asString("Category").as("categoryName"),
//                        product.status))
//                .from(product)
//                .where(builder, category.id.eq(product.categoryId))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .orderBy(getOrderSpecifier(pageable, product))
//                .distinct()
//                .fetch(); //단일 값은 fetone, fetch()LIst형태로 가져옴
//
//        //실제 조회 쿼리 -sql를 메소드로 구성하는게 다임
//        return PageableExecutionUtils.getPage(summaryDtos, pageable,countQuery::fetchOne);
    List<ProductSummaryDto> content =  queryFactory
            .select(Projections.constructor(ProductSummaryDto.class,
                    product.id,
                    product.name,
                    product.price,
                    product.stockQuantity,
                    // coalesce :null일 경우에  뒷 값을 사용해줘 |양방향 관계를 최대한 지향한다
                    category.name.coalesce("분류 없음").as("categoryName"),
                    product.status

            ))
            .from(product)
            .leftJoin(category).on(product.categoryId.eq(category.id))
            .where(
                    keywordContains(condition.keyword()),
                    categoryIdEq(condition.categoryId()),
                    priceGoe(BigDecimal.valueOf(condition.minPrice())),
                    priceLoe(BigDecimal.valueOf(condition.maxPrice())),
                    sellerIdEq(condition.sellerId()),
                    statusActive()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(getOrderSpecifier(pageable,product))
            .fetch();

       JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        keywordContains(condition.keyword()),
                        categoryIdEq(condition.categoryId()),
                        priceGoe(BigDecimal.valueOf(condition.minPrice())),
                        priceLoe(BigDecimal.valueOf(condition.maxPrice())),
                        sellerIdEq(condition.sellerId()),
                        statusActive()
                );

        //PageableExecutionUtils.getPage -> 페이징 처리를 최적화 해주는 유틸리티 불필요한 제거 성능 향상 해준다.
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    /**
     * 키워드 조건 검색
     * @param keyword,
     * @return BooleanEXpression
     * */
    private BooleanExpression keywordContains(String keyword){
        if(!StringUtils.hasText(keyword)){
            return null;
        }

        QProduct product = QProduct.product;
        return product.name.containsIgnoreCase(keyword)
                .or(product.description.containsIgnoreCase(keyword));
    }

    private BooleanExpression priceGoe(BigDecimal minPrice){
        return minPrice !=  null ? QProduct.product.price.goe(minPrice) : null;
    }

    private BooleanExpression priceLoe(BigDecimal masPrice){
        return masPrice !=  null ? QProduct.product.price.loe(masPrice) : null;
    }

    private BooleanExpression sellerIdEq(Long sellerId){
        return sellerId !=  null ? QProduct.product.sellerId.eq(sellerId) : null;
    }
    /**
     * 카테고리 ID 조건
     * @param categoryId
     * @return  BooleanExpression
     * */

    private BooleanExpression categoryIdEq(Long categoryId){
        return categoryId !=  null ? QProduct.product.categoryId.eq(categoryId) : null;
    }

    private BooleanExpression statusActive(){
        return QProduct.product.status.eq(Product.ProductStatus.ACTIVE);
    }

    //BigDecimal은 Java 언어에서 숫자를 정밀하게 저장하고 표현할 수 있는 유일한 방법이다.
    private BooleanExpression priceBetween(BigDecimal minPrice, BigDecimal maxPrice){
        return priceGoe(minPrice).and(priceLoe(maxPrice));
    }

    private BooleanExpression stockAvailable(){
        return QProduct.product.stockQuantity.gt(0);
    }


    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable, QProduct product) {
        if (!pageable.getSort().isEmpty()) {
            for (Sort.Order order : pageable.getSort()) {
                return switch (order.getProperty()) {
                    case "price" -> order.isAscending() ? product.price.asc() : product.price.desc();
                    case "createdAt" -> order.isAscending() ? product.createdAt.asc() : product.createdAt.desc();
                    default -> product.id.desc();
                };
            }
        }

        return product.id.desc();
    }
}
