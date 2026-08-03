package com.example.shoestore.config;

import com.example.shoestore.security.CustomUserDetailsService;
import com.example.shoestore.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                // Tắt CSRF vì dùng JWT stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình phân quyền
                .authorizeHttpRequests(auth -> auth
                        // Swagger / OpenAPI docs — public để test API
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Public endpoints: đăng ký, đăng nhập, static files
                        .requestMatchers("/api/auth/**").permitAll()

                        // [FIXED] Trước đây mọi endpoint chỉ yêu cầu "đã đăng nhập" (anyRequest().authenticated()),
                        // nghĩa là 1 tài khoản USER/CUSTOMER thường cũng gọi thẳng được các API quản trị
                        // (xem toàn bộ đơn hàng, danh sách khách hàng, báo cáo doanh thu, tạo/sửa/xoá sản phẩm & voucher).
                        // Giờ các endpoint quản trị chỉ dành cho ROLE_ADMIN.

                        // Khách xem sản phẩm (đọc) — vẫn cần đăng nhập theo thiết kế hiện tại của trang shop
                        .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                        // Quản lý sản phẩm (tạo/sửa/xoá) — chỉ ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Khách hàng xem đơn của chính mình / đặt hàng — mọi user đã đăng nhập
                        .requestMatchers(HttpMethod.GET, "/api/orders/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                        // Danh sách TẤT CẢ đơn hàng — chỉ ADMIN. (Xem/huỷ 1 đơn theo id được kiểm tra
                        // quyền sở hữu ở tầng service vì cần biết đơn đó của ai.)
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")

                        // Quản lý khách hàng & báo cáo doanh thu — chỉ ADMIN
                        .requestMatchers("/api/customers/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

                        // Voucher: khách được xem danh sách + tra cứu mã khi checkout; tạo/sửa/xoá chỉ ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/vouchers/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/vouchers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vouchers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vouchers/**").hasRole("ADMIN")

                        // Tất cả API còn lại yêu cầu xác thực JWT
                        .anyRequest().authenticated()
                )

                // Stateless session (JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // AuthProvider
                .authenticationProvider(authenticationProvider())

                // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}