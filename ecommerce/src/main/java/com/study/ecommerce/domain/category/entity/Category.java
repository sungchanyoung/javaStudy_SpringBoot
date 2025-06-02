package com.study.ecommerce.domain.category.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // 카테고리의 깊이
    private Integer depth;

    // 부모 카테고리 ID
    @Column(name = "parent_id")
    private Long parentId;

    @Builder
    //ID는 자동 생성 되기때문에 생성자로 받지 않는다.
    public Category(String name, Integer depth, Long parentId) {
        this.name = name;
        this.depth = depth;
        this.parentId = parentId;
    }

}
