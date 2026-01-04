package org.example.nosmoke.repository;

import org.example.nosmoke.entity.SmokingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmokingInfoRepository extends JpaRepository<SmokingInfo, Long> {
    Optional<SmokingInfo> findByUserId(Long userId);
}
