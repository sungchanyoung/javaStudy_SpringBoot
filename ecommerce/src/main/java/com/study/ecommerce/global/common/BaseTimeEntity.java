package com.study.ecommerce.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
//공통 필드를 추상 클래스에 정의 하고 이를 실제 엔티티들이 상속할 수 있도록 해준 것
//직접 테이블러 매핑되지 않고 작식 엔티티의 테이블레 해당 필드가 포함
@MappedSuperclass

//EntityListeners 엔티티의 생성/수정 이벤트를 감지하여 특정 로직을 자동으로 수행하게 도와줌
@EntityListeners(AuditingEntityListener.class)
//BaseEntity는 모임 entity에 기본이 된다 한꺼번에 패키지화 해서 사용한다
public abstract class BaseTimeEntity {
    //각각 보일러 플레이트
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; //created_at 컬럼명으로 설정 해야한다

    @LastModifiedDate //마지막으로 업데이트 된 시간을 가져온다
    private LocalDateTime updatedAt; //updated_at 컬럼

}
