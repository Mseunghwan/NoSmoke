package org.example.nosmoke.repository;

import org.example.nosmoke.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Service에서 매번 예외처리 하지 않도록 만든 편의 메서드
    default User getByIdOrThrow(Long userId){
        return findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다. ID : " + userId));
    }

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByName(String name);
    boolean existsByName(String name);
}

