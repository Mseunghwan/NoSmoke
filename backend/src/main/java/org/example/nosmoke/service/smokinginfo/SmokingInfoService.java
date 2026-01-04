package org.example.nosmoke.service.smokinginfo;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.smokinginfo.SmokingInfoRequestDto;
import org.example.nosmoke.dto.smokinginfo.SmokingInfoUpdateRequestDto;
import org.example.nosmoke.entity.SmokingInfo;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.SmokingInfoRepository;
import org.example.nosmoke.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SmokingInfoService {

    private final SmokingInfoRepository smokingInfoRepository;
    private final UserRepository userRepository;

    // 흡연 정보 저장
    @Transactional
    public SmokingInfo smokingInfoSave(Long userId, SmokingInfoRequestDto requestDto) {
        // User가 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id = " + userId));

        SmokingInfo smokingInfo = new SmokingInfo(
                userId,
                requestDto.getCigaretteType(),
                requestDto.getDailyConsumption(),
                requestDto.getQuitStartTime(),
                requestDto.getTargetDate(),
                requestDto.getQuitGoal()
        );

        return smokingInfoRepository.save(smokingInfo);
    }

    // 흡연 정보 업데이트
    @Transactional
    public void smokingInfoUpdate(Long userId, SmokingInfoUpdateRequestDto requestDto) {
        SmokingInfo smokingInfo = smokingInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 흡연 정보가 존재하지 않습니다. id = " + userId));

        smokingInfo.updateInfo(
                requestDto.getCigaretteType(),
                requestDto.getDailyConsumption(),
                requestDto.getTargetDate(),
                requestDto.getQuitGoal()
        );
    }

}