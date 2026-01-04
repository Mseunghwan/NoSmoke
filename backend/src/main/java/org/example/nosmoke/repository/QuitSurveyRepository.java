package org.example.nosmoke.repository;

import org.example.nosmoke.entity.QuitSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuitSurveyRepository extends JpaRepository<QuitSurvey, Long> {
    List<QuitSurvey> findByUserId(Long userId);

    List<QuitSurvey> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
