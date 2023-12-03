package com.server.coupon.domain.coupon.service;

import com.server.coupon.domain.coupon.entity.Coupon;
import com.server.coupon.domain.coupon.repository.CouponRepository;
import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.event.repository.EventRepository;
import com.server.coupon.domain.event.service.EventService;
import com.server.coupon.domain.user.entity.User;
import com.server.coupon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CouponRepository couponRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 쿠폰 발급 (10개가 쌓이면 DB에 저장)
    @Transactional
    public void publish(Event event){
        try {
            Set<Object> queue = redisTemplate.opsForZSet().range(event.getId().toString(), 0, 9);
            if (queue != null && !queue.isEmpty()) {
                for (Object o : queue) {
                    if (event.getLimit_capacity() > 0) {
                        final Coupon coupon = Coupon.builder()
                                .event(event)
                                .user(userRepository.findByEmail(o.toString()).orElse(null))
                                .build();
                        redisTemplate.opsForZSet().remove(event.getId().toString(), o);
                        log.info("{}에 {}가 쿠폰 발급", event.getName(), o);
                        event.decrease();
                        couponRepository.save(coupon);
                    } else {
                        break;
                    }
                }
                eventRepository.save(event);
            }
        } catch (DataAccessException e) {
            log.error("쿠폰 발급 중 에러 발생: {}", e.getMessage());
        }
    }

    // 쿠폰 대기열 보여주기
    public void getOrder(Event event){
        try {
            Set<Object> queue = redisTemplate.opsForZSet().range(event.getId().toString(), 0, -1);
            if (queue != null) {
                for (Object o : queue) {
                    Long rank = redisTemplate.opsForZSet().rank(event.getId().toString(), o);
                    log.info("{}에 {}가 {}번째 대기중", event.getName(), o, rank);
                }
            }
        } catch (DataAccessException e) {
            log.error("대기열 조회 중 에러 발생: {}", e.getMessage());
        }
    }

    @Transactional
    public String addQueue(Event event, User user){
        try {
            final long now = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(event.getId().toString(), user.getEmail(), now);
            log.info("{}에 {}가 {}에 추가", event.getName(), user.getEmail(), now);
            return String.format("%s에 %s가 %s에 추가", event.getName(), user.getEmail(), now);
        } catch (DataAccessException e) {
            log.error("대기열 추가 중 에러 발생: {}", e.getMessage());
            return "false";
        }
    }

    public List<Coupon> findByUser(User user){
        return couponRepository.findByParticipant(user);
    }
}

