package com.server.coupon.domain.user.controller;

import com.server.coupon.domain.coupon.entity.Coupon;
import com.server.coupon.domain.event.entity.Event;
import com.server.coupon.domain.event.service.EventService;
import com.server.coupon.domain.user.entity.ROLE;
import com.server.coupon.domain.user.entity.User;
import com.server.coupon.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final EventService eventService;

    @GetMapping({"","/"})
    public String index(Model model){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.equals("anonymousUser")) {
            return "loginForm";
        }
        String Email = ((UserDetails)principal).getUsername();
        User user = userService.findByEmail(Email);
        model.addAttribute("user", Email);
        List<Event> events = eventService.getAll();
        model.addAttribute("events", events);
        model.addAttribute("isAdmin", user != null && user.getRole() == ROLE.ADMIN);
        return "index";
    }
    // 아이디 중복 체크
    @PostMapping("/idCheck")
    public ResponseEntity<Boolean> idCheck(@RequestParam String email) {
        boolean isAvailable = userService.checkEmail(email);
        return ResponseEntity.ok(!isAvailable);
    }

    // 회원가입
    @PostMapping("/join")
    public String join(User user, boolean isAdmin, RedirectAttributes redirectAttributes) {
        boolean process = userService.save(user, isAdmin);
        if (process) {
            redirectAttributes.addFlashAttribute("successMessage", "회원가입에 성공했습니다. 로그인하세요.");
            return "redirect:/loginForm";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입에 실패했습니다.");
            return "redirect:/joinForm"; // 또는 에러 메시지와 함께 회원가입 폼을 다시 표시
        }
    }

    @GetMapping("/loginForm")
    public  String loginForm(Model model, HttpServletRequest request){
        if(request.getParameter("error") != null)
            model.addAttribute("loginError", "아이디 또는 비밀번호가 틀렸습니다.");
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public  String joinForm() {
        return "joinForm";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String Email = ((UserDetails)principal).getUsername();
        User user = userService.findByEmail(Email);
        if(user.getRole() == ROLE.ADMIN){
            List<Event> events = eventService.getMyEvent(user);
            model.addAttribute("events", events);
            model.addAttribute("hasEvents", !events.isEmpty());
        }
        else{
            List<Coupon> coupons = userService.getValidCoupons(user);
            model.addAttribute("coupons", coupons);
            model.addAttribute("hasCoupons", !coupons.isEmpty());
        }
        model.addAttribute("user", user);
        return "mypage";
    }
}
