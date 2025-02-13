package com.example.music.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/user")
@Controller
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup() {
        return "/user/signup";
    }
    @PostMapping("/signup")
    public String signup(@ModelAttribute SiteUser siteUser, Model model) {
        if(userService.isUsernameTaken(siteUser.getUsername())) {
            model.addAttribute("error", "이미 존재하는 아이디입니다.");
            return "/user/signup";
        }
        if(siteUser.getUsername().toLowerCase().contains("admin")) {
            siteUser.setRole(Role.ADMIN);
        }else {
            siteUser.setRole(Role.USER);
        }
        userService.createSiteUser(siteUser);
        return "redirect:/user/signup_success";
    }
    @GetMapping("/signup_success")
    public String signupSuccess() {
        return "user/signup_success";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        SiteUser user = userService.validateUser(username, password);
        if(user != null) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/user/login_success";
        }else {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "/user/login";
        }
    }
    @GetMapping("/login_success")
    public String loginSuccess(Model model ,HttpSession session) {
        SiteUser loggedInUser = (SiteUser) session.getAttribute("loggedInUser");
        model.addAttribute("loggedInUser",loggedInUser);
        return "user/login_success";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
