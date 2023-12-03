package com.server.coupon.domain.coupon.repository;

import com.server.coupon.domain.coupon.entity.Coupon;
import com.server.coupon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByParticipant(User user);

}
