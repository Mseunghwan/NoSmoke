package org.example.nosmoke;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;


// Scheduling 기능 활성화 어노테이션
@EnableScheduling
// JPA Auditing 기능 활성화, createdAt, modifiedAt의 자동입력 가능
@EnableJpaAuditing
@SpringBootApplication
public class NoSmokeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoSmokeApplication.class, args);
    }

}
