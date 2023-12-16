package com.server.coupon;

import com.server.coupon.domain.coupon.service.CouponService;
import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.event.repository.EventRepository;
import com.server.coupon.domain.event.service.EventService;
import com.server.coupon.domain.user.entity.User;
import com.server.coupon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class CouponTest {

    @Autowired
    private CouponService couponService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("싱글쓰레드 테스트")
    void 쿠폰_발급_싱글쓰레드_테스트() throws InterruptedException {

        // 8명의 사용자 생성, 하나의 이벤트에 일제히 참여를 12번씩 -> 총 96번
        // 하지만 쿠폰제한은 80개만 해서 80개만 발행, 또한 각각이 넣는 순서에따라 큐가 진행이 되어야한다. -> 큐 사이즈는 10

        // 1. 이벤트 생성
        // 1-1. Admin 계정생성.
        User admin = User.builder()
                .email("testAdmin")
                .password("testAdmin")
                .name("testAdmin")
                .build();
        userRepository.save(admin);

        // 1-2. Event 생성.
        Event event = Event.builder()
                .name("이벤트")
                .limit_capacity(50L)
                .description("이벤트")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMinutes(2))
                .owner(admin)
                .build();
        eventRepository.save(event);

        // 2. 이벤트에 참여하는 사용자 생성
        // 2-1. User 계정생성.
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            User user = User.builder()
                    .email("test" + i)
                    .password("test" + i)
                    .name("test" + i)
                    .build();
            users.add(user);
            userRepository.save(user);
        }

        // 3. 이벤트에 참여하는 사용자가 쿠폰을 발행하는 순서를 보여준다.
        for (User user : users) {
            couponService.addQueue(event, user);
        }

        // 4. 큐에 넣고 나면 일정시간 대기해주어야한다.
        Thread.sleep(20000);

        // 5. 쿠폰 발급 여부 확인 -> 순서대로라면 50번째 사용자까지 쿠폰을 발급받아야함.
        int count = 0;
        for (User user : users) {
            if (!couponService.findByUser(user).isEmpty()) count++;
            else break;
        }
        assertEquals(50, count);
    }

    @Test
    @DisplayName("멀티쓰레드 테스트")
    void 쿠폰_발급_멀티쓰레드_테스트() throws InterruptedException {
        // 기본 로직은 위와 동일 하지만 80개의 쓰레드의 각각의 인원이 동시에 이벤트참여
        // 1. 이벤트 생성
        // 1-1. Admin 계정생성.
        User admin = User.builder()
                .email("testAdmin")
                .password("testAdmin")
                .name("testAdmin")
                .build();
        userRepository.save(admin);

        // 1-2. Event 생성.
        Event event = Event.builder()
                .name("이벤트")
                .limit_capacity(50L)
                .description("이벤트")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMinutes(2))
                .owner(admin)
                .build();
        eventRepository.save(event);

        // 2. 이벤트에 참여하는 사용자 생성
        // 2-1. User 계정생성.
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            User user = User.builder()
                    .email("test" + i)
                    .password("test" + i)
                    .name("test" + i)
                    .build();
            users.add(user);
            userRepository.save(user);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(80);
        CountDownLatch latch = new CountDownLatch(80);
        for (int i = 0; i < 80; i++) {
            int finalI = i;
            executorService.execute(() -> {
                User user = users.get(finalI);
                couponService.addQueue(event, user);
                latch.countDown();
            });
        }
        Thread.sleep(20000);
        latch.await();
        executorService.shutdown();

        // 4. 쿠폰 발급 여부 확인 -> 50명이 쿠폰을 발급받아야함.
        int count = 0;
        for (User user : users) {
            if (!couponService.findByUser(user).isEmpty())
                count++;
        }
        assertEquals(50, count);
    }
}
