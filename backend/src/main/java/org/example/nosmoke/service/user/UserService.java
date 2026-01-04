package org.example.nosmoke.service.user;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.user.*;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.UserRepository;
import org.example.nosmoke.util.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto requestDto) {

        // 1. 중복 이메일 검증
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일 입니다.");
            // IllegalArgumentException은 적합하지 않은 인자를 메소드에 넘겨주었을때 발생시키는 예외
        }

        // 2. Dto를 Entity로 변환
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        User user = new User(
                requestDto.getName(),
                requestDto.getEmail(),
                encodedPassword, // 암호화된 비밀번호를 저장
                0 // 초기 포인트
        );

        // 3. DB에 저장
        User savedUser = userRepository.save(user);

        // 4. Entity를 ResponseDto로 변환
        return new UserSignupResponseDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPoint(),
                savedUser.getCreatedAt()
        );
    }

    // 로그인
    public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 비밀번호 검증 - 암호화된 비밀번호를 비교합니다
        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        // 3. JWT 토큰 생성(아직 JWT 구현 안했으므로 temp_token으로 임시처리)
        String token = jwtTokenProvider.createToken(user.getId().toString(), user.getEmail());

        // 4. ResponseDto 생성
        return new UserLoginResponseDto(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPoint()
        );

    }

    // 사용자 프로필 조회
    public UserProfileResponseDto getProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return new UserProfileResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPoint(),
                user.getCreatedAt(),
                user.getModifiedAt()
        );

    }

    // 사용자 정보 수정
    public UserUpdateResponseDto updateProfile(Long userId, UserUpdateRequestDto requestDto) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 입력받은 이메일 중복 검증(다른 사용자가 사용하는 이메일인지 확인)
        if(!user.getEmail().equals(requestDto.getEmail()) &&
        userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }


        // 3. 사용자 정보 수정(실제로는 User 엔티티에 Update 메서드 추가 필요(현재는 새로운 User 객체 생성(나중에 개선해야함!!)

        // 4. 수정된 정보 저장 및 리턴
        User updatedUser = userRepository.save(user);

        return new UserUpdateResponseDto(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getPoint(),
                updatedUser.getModifiedAt()
        );

    }

    // 포인트 누적
    @Transactional
    public void addPoints(Long userId, int points){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        int newPoints = user.getPoint() + points;
        user.updatePoint(newPoints);
    }

    // 포인트 수정
    @Transactional
    public void updatePoints(Long userId, int points){
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 유저의 포인트 수정
        user.updatePoint(points);
        // 저장
        userRepository.save(user);
    }

    // 사용자 존재 여부 확인
    public boolean existsById(Long userId){
        return userRepository.existsById(userId);
    }
    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

}
