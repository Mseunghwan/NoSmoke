package org.example.nosmoke.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@Entity

@Table(name="smoking_info")
public class SmokingInfo extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name="user_id", nullable = false)
    public Long userId;

    @Column(name="cigarette_type", length=100)
    public String cigaretteType;

    @Column(name="daily_consumption")
    public int dailyConsumption;

    @Column(name="quit_start_date")
    public LocalDate quitStartDate;
    @Column(name="target_date")
    public LocalDate targetDate;
    @Column(name="quitGoal")
    public String quitGoal;

    public SmokingInfo(Long userId, String cigaretteType, Integer dailyConsumption, LocalDate quitStartDate, LocalDate targetDate, String quitGoal) {
        this.userId = userId;
        this.cigaretteType = cigaretteType;
        this.dailyConsumption = dailyConsumption;
        this.quitStartDate = quitStartDate;
        this.targetDate = targetDate;
        this.quitGoal = quitGoal;
    }

    // Setter 굳이 안쓰는 이유? 무분별한 수정 허용을 피하고 비즈니스 로직 부재로 인해
    public void updateInfo(String cigaretteType, int dailyConsumption, LocalDate targetDate, String quitGoal) {
        this.cigaretteType = cigaretteType;
        this.dailyConsumption = dailyConsumption;
        this.targetDate = targetDate;
        this.quitGoal = quitGoal;
    }
}
