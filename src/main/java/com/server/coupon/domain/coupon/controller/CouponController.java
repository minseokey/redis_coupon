package com.server.coupon.domain.coupon.controller;

// 여기서 레디스와 관련된 로직들이 진행.

import com.server.coupon.domain.coupon.service.CouponService;
import com.server.coupon.domain.coupon.service.EventScheduler;
import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.event.service.EventService;
import com.server.coupon.domain.user.entity.ROLE;
import com.server.coupon.domain.user.entity.User;
import com.server.coupon.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class CouponController {
    private final CouponService couponService;
    private final UserService userService;
    private final EventService eventService;
    private final EventScheduler scheduler;
    // 쿠폰 발급
    @PostMapping("/eventApply")
    public String publish(Long event_id, RedirectAttributes redirectAttributes) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByEmail(((UserDetails) principal).getUsername());
        Event event = eventService.getEvent(event_id);
        if (user.getRole() == ROLE.ADMIN){
            redirectAttributes.addFlashAttribute("message", "관리자는 쿠폰을 발급할 수 없습니다.");
        }
        else {
            if (scheduler.isEventInValidPeriod(event)) {
                String temp = couponService.addQueue(event, user);
                if (temp.equals("fail")) {
                    redirectAttributes.addFlashAttribute("message", "쿠폰 발급에 실패했습닌다.");
                } else {
                    redirectAttributes.addFlashAttribute("message", temp);
                }
            } else {
                redirectAttributes.addFlashAttribute("message", "이벤트시간에 맞춰서 오세요~.");
            }
        }
        return "redirect:/";
    }

}
