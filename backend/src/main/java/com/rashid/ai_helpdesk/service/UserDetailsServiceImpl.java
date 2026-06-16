package com.rashid.ai_helpdesk.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rashid.ai_helpdesk.entity.UserEntity;
import com.rashid.ai_helpdesk.repository.UserRepository;
import com.rashid.ai_helpdesk.security.jwt.UserDetailsImpl;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + userEmail));

        return new UserDetailsImpl(user);
    }

    @Transactional
    public void createUser(String email, String username, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

    
    }

    public UserEntity findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<UserEntity> findAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new RuntimeException("No users found");
        }
        return users;
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }

    @Transactional
    public void updateEmail(String email, String newEmail) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email"));

        user.setEmail(newEmail);
    }

    @Transactional
    public void updateUsername(String email, String newUsername) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email"));

        user.setUsername(newUsername);
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email"));

        user.setPassword(passwordEncoder.encode(newPassword));
    }

    public boolean assureEmailExists(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        return userRepository.existsByEmail(email);
    }

    @Transactional
    public boolean deleteById(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);

            return true;
        }

    return false;
    }
}
