package org.example.nosmoke.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name="quit_survey")
public class QuitSurvey extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="is_success")
    private boolean isSuccess;

    @Column(name="stress_level")
    private Integer stressLevel;

    @Column(name="stress_cause")
    private String stressCause;

    @Column(name="craving_level")
    private Integer cravingLevel;

    @Column(name="additional_notes", columnDefinition = "TEXT")
    // columnDefinition 안하면 기본동작으로 DB는 VARCHAR(255)로 짧은 텍스트 설정,
    // Text로 설정해주면 더 긴 텍스트 가능
    private String additionalNotes;

    public QuitSurvey(Long userId, boolean isSuccess, Integer stressLevel, String stressCause, Integer cravingLevel, String additionalNotes) {
        this.userId = userId;
        this.isSuccess = isSuccess;
        this.stressLevel = stressLevel;
        this.stressCause = stressCause;
        this.cravingLevel = cravingLevel;
        this.additionalNotes = additionalNotes;
    }
}
