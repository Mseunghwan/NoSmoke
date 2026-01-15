package org.example.nosmoke.service.smokinginfo;

import org.example.nosmoke.dto.smokinginfo.SmokingInfoRequestDto;
import org.example.nosmoke.dto.smokinginfo.SmokingInfoUpdateRequestDto;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.SmokingInfoRepository;
import org.example.nosmoke.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SmokingInfoServiceTest {

    @InjectMocks
    SmokingInfoService smokingInfoService;

    @Mock
    private SmokingInfoRepository smokingInfoRepository;

    @Mock
    private UserRepository userRepository;


    // 1. 흡연정보 저장

    @Test
    @DisplayName("흡연 정보 저장 성공")
    void 흡연정보_저장_성공() {
        // given
        Long userId = 1L;
        SmokingInfoRequestDto requestDto = new SmokingInfoRequestDto(
                "말보로", 10, LocalDate.now(), LocalDate.now().plusDays(100), "건강을 위해"
        );

        // 유저가 존재한다고 가정
        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));

        // 저장될 때 입력받은 객체 그대로 반환한다고 가정 (ID는 임의로 100L)
        given(smokingInfoRepository.save(any(SmokingInfo.class))).willAnswer(invocation -> {
            SmokingInfo info = (SmokingInfo) invocation.getArgument(0);
            // 테스트 검증을 위해 ID가 부여된 것처럼 처리 (Reflection 없이 필드값 검증만으로 충분하면 생략 가능)
            return info;
        });

        // when
        SmokingInfo result = smokingInfoService.smokingInfoSave(userId, requestDto);

        // then
        assertThat(result.getCigaretteType()).isEqualTo("말보로");
        assertThat(result.getDailyConsumption()).isEqualTo(10);
        assertThat(result.getQuitGoal()).isEqualTo("건강을 위해");

        // 실제로 Repository의 save가 호출되었는지 확인
        verify(smokingInfoRepository).save(any(SmokingInfo.class));
    }

    @Test
    @DisplayName("흡연 정보 저장 실패 - 유저 없음")
    void 흡연정보_저장_실패_유저없음() {
        // given
        Long userId = 999L;
        SmokingInfoRequestDto requestDto = new SmokingInfoRequestDto(
                "말보로", 10, LocalDate.now(), LocalDate.now().plusDays(100), "목표"
        );

        // 유저를 찾지 못함 (Empty)
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> smokingInfoService.smokingInfoSave(userId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 사용자를 찾을 수 없습니다");
    }

    // 2. 흡연 정보 조회

    @Test
    @DisplayName("흡연 정보 조회 성공")
    void 흡연정보_조회_성공() {
        // given
        Long userId = 1L;
        SmokingInfo info = new SmokingInfo(userId, "에쎄", 5, LocalDate.now(), LocalDate.now().plusDays(50), "가족");

        given(smokingInfoRepository.findByUserId(userId)).willReturn(Optional.of(info));

        // when
        SmokingInfo result = smokingInfoService.getSmokingInfo(userId);

        // then
        assertThat(result.getCigaretteType()).isEqualTo("에쎄");
        assertThat(result.getDailyConsumption()).isEqualTo(5);
    }

    @Test
    @DisplayName("흡연 정보 조회 실패 - 정보 없음")
    void 흡연정보_조회_실패_데이터없음() {
        // given
        Long userId = 1L;
        given(smokingInfoRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> smokingInfoService.getSmokingInfo(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("흡연 정보를 찾을 수 없습니다.");
    }

    // 3. 흡연 정보 수정

    @Test
    @DisplayName("흡연 정보 수정 성공")
    void 흡연정보_수정_성공() {
        // given
        Long userId = 1L;
        // 기존 정보
        SmokingInfo existingInfo = new SmokingInfo(userId, "기존담배", 20, LocalDate.now(), LocalDate.now().plusDays(10), "기존목표");

        // 수정 요청 데이터 (담배 종류 변경, 개수 줄임)
        SmokingInfoUpdateRequestDto updateDto = new SmokingInfoUpdateRequestDto(
                "전자담배", 5, LocalDate.now().plusDays(200), "새로운목표"
        );

        given(smokingInfoRepository.findByUserId(userId)).willReturn(Optional.of(existingInfo));

        // when
        smokingInfoService.smokingInfoUpdate(userId, updateDto);

        // then (기존 객체의 내용이 바뀌었는지 확인 - Dirty Checking)
        assertThat(existingInfo.getCigaretteType()).isEqualTo("전자담배");
        assertThat(existingInfo.getDailyConsumption()).isEqualTo(5);
        assertThat(existingInfo.getQuitGoal()).isEqualTo("새로운목표");
    }

    @Test
    @DisplayName("흡연 정보 수정 실패 - 정보 없음")
    void 흡연정보_수정_실패_데이터없음() {
        // given
        Long userId = 1L;
        SmokingInfoUpdateRequestDto updateDto = new SmokingInfoUpdateRequestDto(
                "전자담배", 5, LocalDate.now().plusDays(200), "새로운목표"
        );

        given(smokingInfoRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> smokingInfoService.smokingInfoUpdate(userId, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("흡연 정보가 존재하지 않습니다");
    }
}