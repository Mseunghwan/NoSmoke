package org.example.nosmoke.repository;

import org.example.nosmoke.entity.SmokingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmokingInfoRepository extends JpaRepository<SmokingInfo, Long> {
    Optional<SmokingInfo> findByUserId(Long userId);

    default SmokingInfo getByUserIdOrThrow(Long userId){
        return findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("금연 정보를 찾을 수 없습니다. 설정에서 정보를 입력하세요"));
    }

}
