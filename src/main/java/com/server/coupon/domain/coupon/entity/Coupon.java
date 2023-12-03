package com.server.coupon.domain.coupon.entity;

import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Coupon N : 1 Event
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Event event;

    @ManyToOne // Coupon N : 1 User
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User participant;

    @Builder
    public Coupon(Event event, User user) {
        this.event = event;
        this.participant = user;
    }
}
