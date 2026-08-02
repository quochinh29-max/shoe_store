package com.example.shoestore.service;

import com.example.shoestore.dto.CustomerDTO;
import com.example.shoestore.dto.OrderSummaryDTO;
import com.example.shoestore.entity.Order;
import com.example.shoestore.entity.User;
import com.example.shoestore.exception.ResourceNotFoundException;
import com.example.shoestore.repository.OrderRepository;
import com.example.shoestore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * Danh sách khách hàng (loại trừ tài khoản ADMIN), kèm thống kê số đơn + tổng chi tiêu.
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return userRepository.findByRoleNot(User.Role.ADMIN)
                .stream()
                .map(this::toDTOWithStats)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return toDTOWithStats(user);
    }

    /**
     * Lịch sử đơn hàng của 1 khách hàng, mới nhất trước.
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> getCustomerOrders(Integer customerId) {
        if (!userRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        return orderRepository.findByUserIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toOrderSummary)
                .collect(Collectors.toList());
    }

    // ─── Mapping helpers ───

    private CustomerDTO toDTOWithStats(User user) {
        long totalOrders = orderRepository.countByUserId(user.getId());
        BigDecimal totalSpent = orderRepository.sumSpentByUserId(user.getId());

        return CustomerDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .totalOrders(totalOrders)
                .totalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO)
                .build();
    }

    private OrderSummaryDTO toOrderSummary(Order order) {
        return OrderSummaryDTO.builder()
                .id(order.getId())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}