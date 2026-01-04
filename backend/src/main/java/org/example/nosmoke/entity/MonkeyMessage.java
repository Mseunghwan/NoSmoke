package org.example.nosmoke.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonkeyMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Builder
    public MonkeyMessage(User user, String content, MessageType messageType) {
        this.user = user;
        this.content = content;
        this.messageType = messageType;
    }

    public enum MessageType {
        REACTIVE, // 사용자의 행동(설문 제출)에 대한 반응 - 반응형
        PROACTIVE // AI 펫이 먼저 말을 거는 경우 - 능동형
    }

}
