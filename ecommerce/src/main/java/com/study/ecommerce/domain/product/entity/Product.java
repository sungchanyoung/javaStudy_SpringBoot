package com.study.ecommerce.domain.product.entity;

import com.study.ecommerce.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//최대한 연간 관계를 맺지 마라 -> 자유도 증가
public class Product  extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Builder
    public Product(String name, String description,
                   Long price, Integer stockQuantity,
                   ProductStatus status, Long sellerId,
                   Long categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
    }

    public enum ProductStatus{
       ACTIVE,SOLD_OUT, DELETED;
    }

    //비지니스 메소드
    //감소 메소드(int quantity)
    //에러 터트려야하는 부분 if -> error
    //상태변경 -> sold_out ->if

    //증가 메소드(int quantity)

    //update 메소드 -> 전체 설레 아이디는 바꿀수 없다 생각
    //delete - soft delete 상태를 변경
    public void decreaseStockQuantity(int quantity){
       int restStockQuantity = this.stockQuantity - quantity;
       if(restStockQuantity < 0){
           throw new IllegalArgumentException("재고가 부족합니다.");
       }

       this.stockQuantity = restStockQuantity;
       if(this.stockQuantity == 0){
           this.status = ProductStatus.SOLD_OUT;
       }
    }

    public void increaseStockQuantity(int quantity){
      if(quantity < 0){
          throw new IllegalArgumentException("재고가 부족합니다.");
      }
      this.stockQuantity += quantity;
      this.status = ProductStatus.ACTIVE;
    }

    public void update(String name, String description, Long price, Integer stockQuantity){
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void delete(){
        this.status = ProductStatus.DELETED;
    }


}
