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
 * Tự động thực hiện data migration khi khởi động:
 * 1. BCrypt encode mật khẩu plaintext trong DB.
 * 2. [FIXED] Migrate role CUSTOMER → USER (CUSTOMER là legacy, không còn dùng).
 * Chạy lại mỗi lần khởi động nhưng sẽ tự skip nếu dữ liệu đã đúng.
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
        int passwordUpdated = 0;
        int roleUpdated = 0;

        for (User user : users) {
            boolean changed = false;

            // [FIXED] Dùng startsWith("$2") thay vì "$2a$" — bao phủ cả BCrypt v2a, v2b, v2y
            if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
                String encoded = passwordEncoder.encode(user.getPassword());
                user.setPassword(encoded);
                // [FIXED] Đã xoá user.setPasswordHash(encoded) — trường passwordHash không còn tồn tại trong entity
                passwordUpdated++;
                changed = true;
                log.info("Encoded password for user: {}", user.getUsername());
            }

            // [FIXED] Migrate role CUSTOMER → USER (DB cũ có thể chứa role CUSTOMER)
            if (user.getRole() == User.Role.CUSTOMER) {
                user.setRole(User.Role.USER);
                roleUpdated++;
                changed = true;
                log.info("Migrated role CUSTOMER → USER for user: {}", user.getUsername());
            }

            if (changed) {
                userRepository.save(user);
            }
        }

        if (passwordUpdated > 0) {
            log.info("DataInitializer: Encoded passwords for {} user(s)", passwordUpdated);
        }
        if (roleUpdated > 0) {
            log.info("DataInitializer: Migrated {} user(s) from CUSTOMER to USER role", roleUpdated);
        }
    }
}
