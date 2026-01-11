package org.example.nosmoke.repository;

import org.example.nosmoke.entity.MonkeyMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MonkeyMessageRepository extends JpaRepository<MonkeyMessage, Long> {
    // Slice로 반환
    // 쿼리 실행 시 limit + 1개를 가져와서 다음 페이지 존재 여부만 체크한다(Count 쿼리가 안나간다)
    Slice<MonkeyMessage> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable) ;

}
