package org.example.nosmoke.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
// MappedSuperclass = 이 클래스는 테이블로 만들지 않고, 상속받는 클래스들의 공동 필드로 쓰자라는 뜻
@EntityListeners(AuditingEntityListener.class)
// EntityListeners = 데이터 변경 시 자동으로 시간 기록해달라는 뜻 - createdAt, modifiedAt 위해서
@Getter
public abstract class BaseEntity {
    @CreatedDate
    @Column(name="created_at", updatable = false)
    // DB는 스네이크 케이스이므로, Column명 지정해주자
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name="modified_at")
    private LocalDateTime modifiedAt;

}
