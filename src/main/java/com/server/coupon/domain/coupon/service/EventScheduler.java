package com.server.coupon.domain.coupon.service;

import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.event.repository.EventRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final CouponService couponService;
    private final EventRepository eventRepository;
    private final TaskScheduler customtaskScheduler;
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    // ScheduledExecutorService 초기화
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void initialize() {
        // 모든 이벤트를 주기적으로 확인하여 스케줄링
        scheduledExecutorService.scheduleAtFixedRate(this::checkAndScheduleEvents, 0, 10, TimeUnit.SECONDS);
    }

    // 모든 이벤트를 확인하고 유효한 시간대의 이벤트를 스케줄링하는 메서드
    private void checkAndScheduleEvents() {
        List<Event> events = eventRepository.findAll();
        for (Event event : events) {
            if (isEventInValidPeriod(event) && !scheduledTasks.containsKey(event.getId())) {
                scheduleEvent(event);
                scheduledTasks.remove(event.getId()); // 스케줄링 완료
            }
        }
    }
    // 이벤트가 유효한 시간대에 있는지 확인
    public boolean isEventInValidPeriod(Event event) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(event.getStartDate()) && now.isBefore(event.getEndDate());
    }

    // 이벤트 스케줄링
    private void scheduleEvent(Event event) {
        Runnable task = () -> {
            if (isEventInValidPeriod(event) && event.getLimit_capacity() > 0) {
                couponService.publish(event); // 쿠폰 발행
                couponService.getOrder(event); // 순서 보여주기
            } else {
                cancelScheduledEvent(event.getId()); // 스케줄링 취소
            }
        };

        ScheduledFuture<?> future = customtaskScheduler.scheduleAtFixedRate(
                task,
                Instant.now(), // 시작 시간을 현재 시간으로 설정
                Duration.ofMillis(1000) // 1초 간격
        );
        scheduledTasks.put(event.getId(), future);
    }

    // 스케줄링 취소
    private void cancelScheduledEvent(Long eventId) {
        ScheduledFuture<?> future = scheduledTasks.get(eventId);
        if (future != null) {
            future.cancel(true);
            scheduledTasks.remove(eventId);
        }
    }

    // 이벤트 추가 및 스케줄링
    public void addAndScheduleEvent(Event event) {
        eventRepository.save(event);
        if (isEventInValidPeriod(event)) {
            scheduleEvent(event);
        }
    }

    // 이벤트 삭제 및 스케줄링 취소
    public void deleteAndCancelEvent(Event event) {
        cancelScheduledEvent(event.getId());
        eventRepository.delete(event);
    }
}
