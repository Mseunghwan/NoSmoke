package org.example.nosmoke.repository;

import org.example.nosmoke.entity.SmokingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmokingInfoRepository extends JpaRepository<SmokingInfo, Long> {
    Optional<SmokingInfo> findByUserId(Long userId);

    // 정보가 없으면 null을 반환하는 편의 메서드
    default SmokingInfo getByUserIdOrNull(Long userId) {
        return findByUserId(userId).orElse(null);
    }

}
