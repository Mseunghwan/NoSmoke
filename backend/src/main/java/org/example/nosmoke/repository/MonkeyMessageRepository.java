package org.example.nosmoke.repository;

import org.example.nosmoke.entity.MonkeyMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonkeyMessageRepository extends JpaRepository<MonkeyMessage, Long> {
    // monkey message가 가진 user 필드의 id를 조건으로 생성시간 기준 내림차순(최신순)으로 검색하겠다
    List<MonkeyMessage> findByUser_IdOrderByCreatedAtDesc(Long userId);

}
