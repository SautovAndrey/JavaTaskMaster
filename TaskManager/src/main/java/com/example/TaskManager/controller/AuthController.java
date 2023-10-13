package com.example.TaskManager.controller;

import com.example.TaskManager.dao.RoleRepository;
import com.example.TaskManager.dao.UserRepository;
import com.example.TaskManager.domain.Role;
import com.example.TaskManager.domain.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@Valid User user, Model model) {
        User userFromDb = userRepository.findByUsername(user.getUsername());

        if (userFromDb != null) {
            logger.warn("User with username {} already exists", user.getUsername());
            model.addAttribute("usernameError", "User with this username already exists");
            return "registration";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole != null) {
            user.getRoles().add(userRole);
        } else {
            logger.error("Role USER not found");
            model.addAttribute("roleError", "Internal error, please try again later");
            return "registration";
        }

        User savedUser = userRepository.save(user);

        if (savedUser != null) {
            logger.info("User {} successfully saved", savedUser.getUsername());
            return "redirect:/login";
        } else {
            logger.error("User {} could not be saved", user.getUsername());
            model.addAttribute("saveError", "Internal error, please try again later");
            return "registration";
        }
    }
}

