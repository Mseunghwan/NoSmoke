package org.example.nosmoke.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 스레드 풀 사이즈 설정(5개) --> 스케쥴러 스레드
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("taskScheduler-task");
        scheduler.initialize();
        return scheduler;

    }

}
