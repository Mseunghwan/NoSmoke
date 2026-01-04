package org.example.nosmoke.service.user;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.entity.User;
import org.example.nosmoke.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CutsomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        // userId로 조회
        try {
            long userId = Long.parseLong(username);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("찾을 수 없는 UserId 입니다 ID : " + userId));

            return createUserDetails(user);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("유효하지 않은 사용자 ID 형식입니다. " + username + " : " + e.getMessage());
        }
    }

    // DB에 User 값 존재한다면 UserDetails 객체로 만들어 리턴
    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getId())) // 이메일 대신 회원 ID를 UserID로 --> 이메일 사용하면 input string 에러 발생
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // 역할 설정 부분, 우선 비워 둠
                .build();
    }
}
