package com.server.coupon.domain.event.entity;

import com.server.coupon.domain.coupon.entity.Coupon;
import com.server.coupon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private Long limit_capacity;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @ManyToOne ()// Event N : 1 User
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User owner;

    @OneToMany(mappedBy = "event") // Event 1 : N Coupon
    private List<Coupon> coupons  = new ArrayList<>();

    public void decrease() {
        this.limit_capacity--;
    }
}
