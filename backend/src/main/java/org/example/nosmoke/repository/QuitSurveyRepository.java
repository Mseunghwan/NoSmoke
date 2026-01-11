package org.example.nosmoke.repository;

import org.example.nosmoke.dto.quitsurvey.QuitSurveyLightDto;
import org.example.nosmoke.entity.QuitSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
@Repository
public interface QuitSurveyRepository extends JpaRepository<QuitSurvey, Long> {
    List<QuitSurvey> findByUserId(Long userId);

    List<QuitSurvey> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    // pageable 추가 : 원하는 LIMIT 만큼 잘라보기
    @Query("SELECT new org.example.nosmoke.dto.quitsurvey.QuitSurveyLightDto(q.isSuccess, q.createdAt) " +
            "FROM QuitSurvey q " +
            "WHERE q.userId = :userId " +
            "ORDER BY q.createdAt DESC") // 최신 순으로, 금연 몇일 했는지
    List<QuitSurveyLightDto> findAllLightByUserId(@Param("userId") Long userId, Pageable pageable);
}
