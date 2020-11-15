package application.controllers;

import application.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
@RequestMapping("/")
public class Index {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String addUser(@ModelAttribute User user, RedirectAttributes redirAttrs) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            redirAttrs.addFlashAttribute("error", "This username already taken");
            return "redirect:/register";
        }

        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            redirAttrs.addFlashAttribute("error", "Something went wrong with DB :(");
            return "redirect:/register";
        }

        redirAttrs.addFlashAttribute("success", "Registration success! Please login.");
        return "redirect:/login";
    }

}
