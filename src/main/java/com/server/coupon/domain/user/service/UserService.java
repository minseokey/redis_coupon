package com.server.coupon.domain.user.service;

import com.server.coupon.domain.coupon.entity.Coupon;
import com.server.coupon.domain.coupon.repository.CouponRepository;
import com.server.coupon.domain.user.entity.ROLE;
import com.server.coupon.domain.user.entity.User;
import com.server.coupon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder encodePwd;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    public boolean checkEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean save(User user, boolean isAdmin){
        try {
            String raw = user.getPassword();
            String encode = encodePwd.encode(raw);
            user.setPassword(encode);
            if(isAdmin) user.setRole(ROLE.ADMIN);
            else user.setRole(ROLE.USER);
            userRepository.save(user);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public User findByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    public List<Coupon> getValidCoupons(User user) {
        return couponRepository.findByParticipant(user);
    }
}
