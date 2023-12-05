package com.server.coupon.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {
    private final RedisProperty redisProperty;
    @Bean
    // 레디스 연결을 위한 팩토리 생성, 레디스 서버 정보를 넘겨줌
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisProperty.getHost(), redisProperty.getPort());
    }
    @Bean
    // 레디스에 연결된 템플릿 생성
    // 이건 보면 string타입 키로 object타입 value를 저장하고 조회하는데 사용됨.
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
    @Bean(name = "customtaskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // 적절한 스레드 풀 크기 설정
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }
}
