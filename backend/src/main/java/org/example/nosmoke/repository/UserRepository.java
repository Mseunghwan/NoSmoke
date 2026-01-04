package org.example.nosmoke.repository;

import org.example.nosmoke.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
// interface로 JPARepository를 상속받는 이유?
    // 상속받으면 SpringDataJpa가 자동으로 구현체를 만들어 줌,
    // 원래는 save, findById 뭐 이런거 만들어야 하는데 그냥 써도 된다는 거

// <User, Long> 타입의 이유
    // 첫 번째 User : 어떤 Entity를 다룰 지,
    // 두 번째 Long : Primary Key의 타입
    // 그래서 userRepository.findById(1L) 을 쓰거나
    // userRepository.save(new User(...)) 를 쓸 수 있다

// Optional? 값이 있을 수도 있고, 없을 수도 있다를 표현하는 클래스

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

