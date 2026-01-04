package org.example.nosmoke.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="users")
@Getter
@NoArgsConstructor
// NoArgsConstructor = 기본 생성자를 자동으로 만들어 주는 어노테이션
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String name;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String password;

    @Column(nullable=false)
    private int point = 0;

    public User(String name, String email, String password, int point) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.point = point;
    }

    // 포인트 업데이트 메서드
    public void updatePoint(int point) {
        this.point = point;
    }
}
