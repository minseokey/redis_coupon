package com.server.coupon.domain.event.repository;

import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOwner(User owner);
}
