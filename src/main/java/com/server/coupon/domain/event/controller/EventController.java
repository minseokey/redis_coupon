package com.server.coupon.domain.event.controller;

import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.event.service.EventService;
import com.server.coupon.domain.user.entity.ROLE;
import com.server.coupon.domain.user.entity.User;
import com.server.coupon.domain.user.repository.UserRepository;
import com.server.coupon.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final UserService userService;


    @PostMapping("/event")
    @Secured("ROLE_ADMIN")
    public String save(Event event, RedirectAttributes redirectAttributes) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByEmail(((UserDetails)principal).getUsername());
        String ret = eventService.save(event, user);
        if (ret.equals("success")){
            return "redirect:/";
        }
        else {
            redirectAttributes.addFlashAttribute("errorCreate", ret);
            return "redirect:/event";
        }
    }
    @PostMapping("/eventdelete")
    @Secured("ROLE_ADMIN")
    public String delete(Long event_id, RedirectAttributes redirectAttributes) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByEmail(((UserDetails)principal).getUsername());
        String ret = eventService.delete(event_id, user);
        if (ret.equals("success")) {
            return "redirect:/";
        }
        else {
            redirectAttributes.addFlashAttribute("errorDelete", ret);
            return "redirect:/event";
        }
    }
    @GetMapping("/event")
    @Secured("ROLE_ADMIN")
    public String getEvent() {
        return "eventC&D";
    }
}
