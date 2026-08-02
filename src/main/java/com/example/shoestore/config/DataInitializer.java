package com.example.shoestore.config;

import com.example.shoestore.entity.Color;
import com.example.shoestore.entity.Product;
import com.example.shoestore.entity.ProductVariant;
import com.example.shoestore.entity.Size;
import com.example.shoestore.entity.User;
import com.example.shoestore.repository.ColorRepository;
import com.example.shoestore.repository.ProductRepository;
import com.example.shoestore.repository.ProductVariantRepository;
import com.example.shoestore.repository.SizeRepository;
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
    // [MỚI] Backfill biến thể (size/màu) còn thiếu cho sản phẩm đã tồn tại trong DB —
    // nếu không có biến thể, khách hàng không có gì để chọn ở trang mua sắm nên không
    // thêm được vào giỏ hàng.
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    @Override
    @Transactional
    public void run(String... args) {
        migrateUsers();
        backfillMissingProductVariants();
    }

    private void migrateUsers() {
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

    /**
     * [MỚI] Một số sản phẩm (tạo qua form Admin trước khi có bản vá, hoặc thiếu trong seed.sql)
     * chỉ có size/màu dạng chuỗi trên bảng products mà CHƯA có dòng tương ứng trong
     * product_variants -> trang mua sắm của khách không có gì để chọn nên không thêm được
     * vào giỏ hàng. Hàm này tự tạo 1 biến thể mặc định cho các sản phẩm như vậy.
     * Chạy lại mỗi lần khởi động nhưng tự skip các sản phẩm đã có biến thể.
     */
    private void backfillMissingProductVariants() {
        List<Product> products = productRepository.findAll();
        int created = 0;

        for (Product product : products) {
            boolean hasVariant = !productVariantRepository.findByProductIdWithRefs(product.getId()).isEmpty();
            if (hasVariant) continue;

            String sizeValue = product.getSize();
            String colorName = product.getColor();
            if (sizeValue == null || sizeValue.isBlank() || colorName == null || colorName.isBlank()) {
                // Không đủ dữ liệu size/màu để tự tạo biến thể — cần admin bổ sung thủ công
                continue;
            }

            Size size = sizeRepository.findBySizeValue(sizeValue.trim())
                    .orElseGet(() -> sizeRepository.save(Size.builder().sizeValue(sizeValue.trim()).build()));
            Color color = colorRepository.findByColorNameIgnoreCase(colorName.trim())
                    .orElseGet(() -> colorRepository.save(Color.builder().colorName(colorName.trim()).build()));

            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .size(size)
                    .color(color)
                    .sku(generateUniqueSku(product, size, color))
                    .price(product.getPrice())
                    .stockQuantity(product.getQuantity() != null ? product.getQuantity() : 0)
                    .build();
            productVariantRepository.save(variant);
            created++;
            log.info("Backfilled default variant for product: {}", product.getName());
        }

        if (created > 0) {
            log.info("DataInitializer: Created {} missing product variant(s)", created);
        }
    }

    private String generateUniqueSku(Product product, Size size, Color color) {
        String colorPrefix = color.getColorName().length() >= 3
                ? color.getColorName().substring(0, 3)
                : color.getColorName();
        String base = ("SP" + product.getId() + "-" + size.getSizeValue() + "-" + colorPrefix)
                .toUpperCase()
                .replaceAll("\\s+", "");

        String sku = base;
        int suffix = 1;
        while (productVariantRepository.existsBySku(sku)) {
            sku = base + "-" + (suffix++);
        }
        return sku;
    }
}
