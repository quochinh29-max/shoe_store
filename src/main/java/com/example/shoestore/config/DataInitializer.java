package com.example.shoestore.config;

import com.example.shoestore.entity.User;
import com.example.shoestore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tự động BCrypt encode mật khẩu plaintext trong DB khi khởi động.
 * Chỉ encode những password chưa ở dạng BCrypt (không bắt đầu bằng "$2a$").
 * Chạy 1 lần duy nhất — lần sau sẽ tự skip vì password đã encoded.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        int updated = 0;

        for (User user : users) {
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                String rawPassword = user.getPassword(); // e.g. "admin123"
                String encoded = passwordEncoder.encode(rawPassword);
                user.setPassword(encoded);
                user.setPasswordHash(encoded);
                userRepository.save(user);
                updated++;
                log.info("Encoded password for user: {}", user.getUsername());
            }
        }

        if (updated > 0) {
            log.info("DataInitializer: Encoded passwords for {} user(s)", updated);
        }
    }
}
