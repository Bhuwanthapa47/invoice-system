package com.invoice_system.service;

import com.invoice_system.dto.UserDTO;
import com.invoice_system.model.User;
import com.invoice_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(UserDTO dto) {
        System.out.println(">> Registering user: " + dto.getUsername() + " | Role: " + dto.getRole());

        if (userRepository.existsByUsername(dto.getUsername())) {
            System.out.println(">> Username already exists!");
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole().equalsIgnoreCase("ADMIN") ? "ROLE_ADMIN" : "ROLE_USER");

// 🆕 New profile fields
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setCreatedAt(LocalDate.now());

        userRepository.save(user);
        System.out.println(">> User saved: " + user.getUsername());

    }


}
