package org.example.nosmoke.service.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.token.TokenDto;
import org.example.nosmoke.dto.user.*;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.SmokingInfoRepository;
import org.example.nosmoke.repository.UserRepository;
import org.example.nosmoke.util.JwtTokenProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SmokingInfoRepository smokingInfoRepository;

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
        TokenDto tokenDto = jwtTokenProvider.createTokenDto(user.getId().toString(), user.getEmail());

        // 4. Redis에 Refresh Token 저장
        long refreshTokenValidTime = jwtTokenProvider.getRefreshTokenValidTime();

        redisTemplate.opsForValue()
                .set("RT:" + user.getEmail(), tokenDto.getRefreshToken(), refreshTokenValidTime, TimeUnit.MILLISECONDS);

        // 유저 흡연 정보 등록되어 있는지 확인
        boolean hasSmokingInfo = smokingInfoRepository.findByUserId(user.getId()).isPresent();

        // 4. ResponseDto 생성
        return new UserLoginResponseDto(
                tokenDto.getAccessToken(),
                tokenDto.getRefreshToken(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPoint(),
                hasSmokingInfo
        );

    }

    // 토큰 재발급
    @Transactional
    public TokenDto reissue(String accessToekn, String refreshToken){

        // Refresh Token 검증
        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 만료되었거나 유효하지 않습니다.");
        }

        // Access Token에서 userID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToekn);

        // Redis에서 User Email을 기반으로 저장된 Refresh Token 값을 가져옴
        String userPk = jwtTokenProvider.getUserPk(accessToekn);
        User user = userRepository.getByIdOrThrow(Long.parseLong(userPk));

        String redisRefreshToken = (String) redisTemplate.opsForValue().get("RT:" + user.getEmail());

        // Redis의 Refresh Token과 클라이언트가 보낸 토큰 일치 여부 확인
        if(!refreshToken.equals(redisRefreshToken)) {
            throw new IllegalArgumentException("토큰 정보가 일치하지 않습니다"); // 탈취 가능성?
        }

        // 새로운 토큰 생성 및 Redis 업데이트
        TokenDto tokenDto = jwtTokenProvider.createTokenDto(userPk, user.getEmail());

        long refreshToeknExpiration = jwtTokenProvider.getRefreshTokenValidTime();
        redisTemplate.opsForValue()
                .set("RT:" + user.getEmail(), tokenDto.getRefreshToken(), refreshToeknExpiration);

        return tokenDto;
    }

    // 로그아웃
    @Transactional
    public void logout(String accessToken) {
        // Access Token 유효성 검증
        if(!jwtTokenProvider.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // Access Token에서 User 정보 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        String userPk = jwtTokenProvider.getUserPk(accessToken);
        User user = userRepository.getByIdOrThrow(Long.parseLong(userPk));

        // Redis 에서 Refresh Token 삭제
        if(redisTemplate.opsForValue().get("RT:" + user.getEmail()) != null) {
            redisTemplate.delete("RT:" + user.getEmail());
        }

        // Access Token 블랙리스트 등록(남은 유효기간 만큼)
        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisTemplate.opsForValue()
                .set(accessToken, "logout");
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
    public UserUpdateResponseDto updateNameProfile(Long userId, UserNameUpdateRequestDto requestDto) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 입력받은 이메일 중복 검증(다른 사용자가 사용하는 이메일인지 확인)
        if(!user.getName().equals(requestDto.getName()) &&
        userRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("이미 사용중인 사용자명입니다.");
        }


        // 3. 사용자 정보 수정(실제로는 User 엔티티에 Update 메서드 추가 필요(현재는 새로운 User 객체 생성(나중에 개선해야함!!)
        user.updateName(requestDto.getName());

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

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequestDto requestDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 현재 비밀번호 확인(DB에 저장된 암호화 된 비밀번호 < - > 입력받은 비밀번호)
        if(!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 3. 새 비밀번호와 확인 비밀번호가 같은지 검증
        if(!requestDto.getNewPassword().equals(requestDto.getNewPasswordCheck())){
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 4. 현재 비밀번호와 새 비밀번호가 같은지 확인
        if(!passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())){
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
        }

        // 5. 비밀번호 암호화 및 수정
        String encodedPassword = passwordEncoder.encode(requestDto.getNewPassword());
        user.updatePassword(encodedPassword);

        // @Transactional이 있으니 userRepository.save(user)는 필요없다구~
    }
}
