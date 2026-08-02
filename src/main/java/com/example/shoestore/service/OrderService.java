package com.example.shoestore.service;

import com.example.shoestore.dto.OrderDTO;
import com.example.shoestore.dto.OrderItemDTO;
import com.example.shoestore.entity.Order;
import com.example.shoestore.entity.OrderDetail;
import com.example.shoestore.exception.ResourceNotFoundException;
import com.example.shoestore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

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
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Integer id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return toDTO(order, true);
    }

    /**
     * Cập nhật trạng thái đơn hàng (PENDING → CONFIRMED → SHIPPING → COMPLETED, hoặc CANCELLED).
     */
    @Transactional
    public OrderDTO updateOrderStatus(Integer id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if ("CANCELLED".equals(order.getOrderStatus()) || "COMPLETED".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "Không thể đổi trạng thái của đơn hàng đã " +
                            ("CANCELLED".equals(order.getOrderStatus()) ? "hủy" : "hoàn thành"));
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