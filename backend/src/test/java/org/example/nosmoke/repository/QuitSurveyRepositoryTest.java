package org.example.nosmoke.repository;

import org.example.nosmoke.dto.quitsurvey.QuitSurveyLightDto;
import org.example.nosmoke.entity.QuitSurvey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class QuitSurveyRepositoryTest {

    @Autowired
    private QuitSurveyRepository quitSurveyRepository;

    @Test
    @DisplayName("DTO 직접 조회 쿼리(findAllLightByUserId) 테스트")
    void 쿼리_조회_테스트(){
        // given
        Long userId1 = 1L;
        Long userId2 = 2L;

        // 내 설문 2개 저장
        QuitSurvey survey1_1 = new QuitSurvey(userId1, true, 5, "이유~~1", 3, "메모~~1");
        QuitSurvey survey1_2 = new QuitSurvey(userId1, false, 8, "이유~~2", 7, "메모~~2");

        // 남의 설문 2개 저장
        QuitSurvey survey3 = new QuitSurvey(userId2, true, 1, "이유~~3", 1, "메모~~3");

        quitSurveyRepository.saveAll(List.of(survey1_1, survey1_2, survey3));

        // when
        // 페이지 요청
        List<QuitSurveyLightDto> result = quitSurveyRepository.findAllLightByUserId(userId1, PageRequest.of(0, 10));


        // then
        assertThat(result).hasSize(2); // 내 것만 2개 나와야

        // 최신 순 정렬확인
        QuitSurveyLightDto firstDto = result.get(0);

        assertThat(firstDto.isSuccess()).isEqualTo(survey1_2.isSuccess());

    }

    @Test
    @DisplayName("최근 5개 조회 (findTop5ByUserIdOrderByCreatedAtDesc) 테스트")
    void 최근_5개_조회_테스트(){
        // given
        Long userId = 1L;
        for(int i = 0; i < 6; i++){
            quitSurveyRepository.save(new QuitSurvey(userId, true, i, "이유", i, "메모"));
        }

        // when
        List<QuitSurvey> result = quitSurveyRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);

        // then
        assertThat(result).hasSize(5);

    }

}
