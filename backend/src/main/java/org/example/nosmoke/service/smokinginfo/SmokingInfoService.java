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

    // 흡연 정보 조회
    public SmokingInfo getSmokingInfo(Long userId) {
        return smokingInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("흡연 정보를 찾을 수 없습니다."));
    }

    // 흡연 정보 저장
    @Transactional
    public SmokingInfo smokingInfoSave(Long userId, SmokingInfoRequestDto requestDto) {
        // User가 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id = " + userId));

        // 기존 정보가 있는지 확인하여 분기 처리
        return smokingInfoRepository.findByUserId(userId)
                .map(existingInfo -> {
                    // 이미 존재하면 -> 기존 객체 업데이트 (Dirty Checking)
                    existingInfo.updateInfo(
                            requestDto.getCigaretteType(),
                            requestDto.getDailyConsumption(),
                            requestDto.getTargetDate(),
                            requestDto.getQuitGoal()
                    );
                    return existingInfo;
                })
                .orElseGet(() -> {
                    // 없으면 -> 새로 생성하여 저장 (Insert)
                    SmokingInfo newInfo = new SmokingInfo(
                            userId,
                            requestDto.getCigaretteType(),
                            requestDto.getDailyConsumption(),
                            requestDto.getQuitStartTime(),
                            requestDto.getTargetDate(),
                            requestDto.getQuitGoal()
                    );
                    return smokingInfoRepository.save(newInfo);
                });
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