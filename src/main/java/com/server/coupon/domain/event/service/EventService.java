package com.server.coupon.domain.event.service;

import com.server.coupon.domain.coupon.service.EventScheduler;
import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.event.repository.EventRepository;
import com.server.coupon.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final EventScheduler eventScheduler;

    public String save(Event event, User user) {
        if (event.getStartDate().isBefore(LocalDateTime.now())){
            return "이벤트 시작 날짜가 현재보다 이전입니다.";
        }
        else if (event.getEndDate().isBefore(LocalDateTime.now())) {
            return "이벤트 종료 날짜가 현재보다 이전입니다.";
        }
        else if (event.getStartDate().isAfter(event.getEndDate())) {
            return "이벤트 시작 날짜가 종료 날짜보다 이후입니다.";
        }
        else if (event.getLimit_capacity() < 0) {
            return "이벤트 참여 제한 인원이 0보다 작습니다.";
        }
        else if (event.getLimit_capacity() > 1000) {
            return "이벤트 참여 제한 인원이 1000보다 큽니다.";
        }
        else {
            try {
                event.setOwner(user);
                eventScheduler.addAndScheduleEvent(event);
                return "success";
            } catch (Exception e) {
                return "이벤트 생성에 실패하였습니다.";
            }
        }
    }

    public String delete(Long event_id, User user) {
        Event event = eventRepository.findById(event_id).orElse(null);
        if (event == null) {
            return "존재하지 않는 이벤트입니다.";

        }
        else if (!event.getOwner().equals(user)) {
            return "삭제 권한이 없습니다.";
        }
        else {
            try {
                eventScheduler.deleteAndCancelEvent(event);
                return "success";
            } catch (Exception e) {
                return "이벤트 삭제에 실패하였습니다.";
            }
        }
    }
    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    public List<Event> getMyEvent(User user) {
        return eventRepository.findByOwner(user);
    }

    public Event getEvent(Long event_id) {
        return eventRepository.findById(event_id).orElse(null);
    }
}
