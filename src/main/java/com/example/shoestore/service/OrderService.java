package com.example.shoestore.service;

import com.example.shoestore.dto.CreateOrderRequest;
import com.example.shoestore.dto.OrderDTO;
import com.example.shoestore.dto.OrderItemDTO;
import com.example.shoestore.dto.OrderItemRequest;
import com.example.shoestore.entity.Order;
import com.example.shoestore.entity.OrderDetail;
import com.example.shoestore.entity.ProductVariant;
import com.example.shoestore.entity.User;
import com.example.shoestore.entity.Voucher;
import com.example.shoestore.exception.ResourceNotFoundException;
import com.example.shoestore.repository.OrderRepository;
import com.example.shoestore.repository.ProductVariantRepository;
import com.example.shoestore.repository.UserRepository;
import com.example.shoestore.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    // [MỚI] cần để tra người dùng hiện tại, biến thể sản phẩm, voucher khi checkout
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VoucherRepository voucherRepository;

    /**
     * Danh sách tất cả đơn hàng (không kèm chi tiết sản phẩm, để tải nhanh cho bảng danh sách).
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllWithUser()
                .stream()
                .map(order -> toDTO(order, false))
                .collect(Collectors.toList());
    }

    /**
     * Chi tiết 1 đơn hàng, kèm danh sách sản phẩm trong đơn.
     * [FIXED] Trước đây không kiểm tra chủ sở hữu -> bất kỳ user nào cũng xem được đơn của người khác
     * chỉ bằng cách đổi id trên URL (IDOR). Giờ: ADMIN xem được mọi đơn, USER chỉ xem được đơn của mình.
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Integer id, Authentication authentication) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        assertCanAccessOrder(order, authentication);
        return toDTO(order, true);
    }

    /**
     * [MỚI] Lịch sử đơn hàng của người dùng đang đăng nhập (dùng cho trang "Đơn hàng của tôi").
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getMyOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(order -> toDTO(order, false))
                .collect(Collectors.toList());
    }

    /**
     * [MỚI] Khách hàng đặt hàng (checkout).
     * - Kiểm tra & trừ tồn kho theo từng biến thể (size/màu).
     * - Áp dụng voucher (nếu có) và tính giảm giá.
     * - Tạo Order + OrderDetail.
     */
    @Transactional
    public OrderDTO createOrder(String username, CreateOrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();

        for (OrderItemRequest item : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", item.getVariantId()));

            int currentStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
            if (currentStock < item.getQuantity()) {
                String productName = variant.getProduct() != null ? variant.getProduct().getName() : "sản phẩm";
                throw new IllegalArgumentException(
                        "\"" + productName + "\" chỉ còn " + currentStock + " sản phẩm trong kho");
            }

            BigDecimal unitPrice = variant.getPrice() != null
                    ? variant.getPrice()
                    : (variant.getProduct() != null ? variant.getProduct().getPrice() : BigDecimal.ZERO);

            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

            // Trừ kho
            variant.setStockQuantity(currentStock - item.getQuantity());
            productVariantRepository.save(variant);

            details.add(OrderDetail.builder()
                    .variant(variant)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .build());
        }

        // Áp dụng voucher (nếu có)
        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            voucher = voucherRepository.findByCodeIgnoreCase(request.getVoucherCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Mã voucher không tồn tại"));

            LocalDateTime now = LocalDateTime.now();
            if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
                throw new IllegalArgumentException("Voucher chưa đến thời gian sử dụng");
            }
            if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
                throw new IllegalArgumentException("Voucher đã hết hạn");
            }
            if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null
                    && voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new IllegalArgumentException("Voucher đã hết lượt sử dụng");
            }
            if (voucher.getMinOrderValue() != null
                    && totalAmount.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new IllegalArgumentException(
                        "Đơn hàng chưa đạt giá trị tối thiểu để dùng voucher này");
            }

            if ("PERCENT".equals(voucher.getDiscountType())) {
                discountAmount = totalAmount
                        .multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                discountAmount = voucher.getDiscountValue();
            }

            if (voucher.getMaxDiscount() != null
                    && discountAmount.compareTo(voucher.getMaxDiscount()) > 0) {
                discountAmount = voucher.getMaxDiscount();
            }
            if (discountAmount.compareTo(totalAmount) > 0) {
                discountAmount = totalAmount;
            }

            voucher.setUsedCount((voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) + 1);
            voucherRepository.save(voucher);
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        Order order = Order.builder()
                .user(user)
                .voucher(voucher)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .orderStatus("PENDING")
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .shippingAddress(request.getShippingAddress())
                .note(request.getNote())
                .orderDetails(details)
                .build();

        for (OrderDetail d : details) {
            d.setOrder(order);
        }

        Order saved = orderRepository.save(order);
        return toDTO(saved, true);
    }

    /**
     * Cập nhật trạng thái đơn hàng (PENDING → CONFIRMED → SHIPPING → COMPLETED, hoặc CANCELLED).
     * [FIXED] Trước đây không kiểm tra ai đang gọi -> bất kỳ user nào cũng đổi được trạng thái
     * (kể cả huỷ/hoàn thành) của đơn hàng bất kỳ, không chỉ đơn của mình (IDOR).
     * Giờ: ADMIN được đổi trạng thái mọi đơn; USER thường chỉ được HUỶ đơn CỦA CHÍNH MÌNH khi đơn còn PENDING.
     */
    @Transactional
    public OrderDTO updateOrderStatus(Integer id, String newStatus, Authentication authentication) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (isAdmin(authentication)) {
            if ("CANCELLED".equals(order.getOrderStatus()) || "COMPLETED".equals(order.getOrderStatus())) {
                throw new IllegalArgumentException(
                        "Không thể đổi trạng thái của đơn hàng đã " +
                                ("CANCELLED".equals(order.getOrderStatus()) ? "hủy" : "hoàn thành"));
            }
        } else {
            assertCanAccessOrder(order, authentication);
            if (!"CANCELLED".equals(newStatus)) {
                throw new AccessDeniedException("Bạn chỉ có thể hủy đơn hàng của mình");
            }
            if (!"PENDING".equals(order.getOrderStatus())) {
                throw new IllegalArgumentException(
                        "Chỉ có thể hủy đơn hàng đang ở trạng thái chờ xử lý");
            }
        }

        order.setOrderStatus(newStatus);
        if ("COMPLETED".equals(newStatus)) {
            order.setPaymentStatus("PAID");
        } else if ("CANCELLED".equals(newStatus)) {
            order.setPaymentStatus("FAILED");
        }

        Order saved = orderRepository.save(order);
        return toDTO(saved, false);
    }

    // ─── Authorization helpers ───

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void assertCanAccessOrder(Order order, Authentication authentication) {
        if (isAdmin(authentication)) return;
        String username = authentication.getName();
        if (order.getUser() == null || !order.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập đơn hàng này");
        }
    }

    // ─── Mapping helpers ───

    private OrderDTO toDTO(Order order, boolean withItems) {
        OrderDTO.OrderDTOBuilder builder = OrderDTO.builder()
                .id(order.getId())
                .customerId(order.getUser() != null ? order.getUser().getId() : null)
                .customerName(order.getUser() != null ? order.getUser().getFullName() : "N/A")
                .customerEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .voucherCode(order.getVoucher() != null ? order.getVoucher().getCode() : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt());

        if (withItems && order.getOrderDetails() != null) {
            List<OrderItemDTO> items = order.getOrderDetails()
                    .stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList());
            builder.items(items);
        }

        return builder.build();
    }

    private OrderItemDTO toItemDTO(OrderDetail detail) {
        String productName = "N/A";
        String size = "-";
        String color = "-";

        if (detail.getVariant() != null) {
            if (detail.getVariant().getProduct() != null) {
                productName = detail.getVariant().getProduct().getName();
            }
            if (detail.getVariant().getSize() != null) {
                size = detail.getVariant().getSize().getSizeValue();
            }
            if (detail.getVariant().getColor() != null) {
                color = detail.getVariant().getColor().getColorName();
            }
        }

        return OrderItemDTO.builder()
                .productName(productName)
                .size(size)
                .color(color)
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .subtotal(detail.getUnitPrice().multiply(java.math.BigDecimal.valueOf(detail.getQuantity())))
                .build();
    }
}