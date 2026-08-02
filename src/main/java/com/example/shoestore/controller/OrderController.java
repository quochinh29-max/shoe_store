package com.example.shoestore.controller;

import com.example.shoestore.dto.CreateOrderRequest;
import com.example.shoestore.dto.OrderDTO;
import com.example.shoestore.dto.OrderStatusUpdateRequest;
import com.example.shoestore.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * [MỚI] GET /api/orders/my
     * Lịch sử đơn hàng của người dùng đang đăng nhập (trang "Đơn hàng của tôi").
     * Đặt trước "/{id}" để tránh Spring hiểu nhầm "my" là 1 path-variable id.
     */
    @GetMapping("/my")
    public ResponseEntity<List<OrderDTO>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getMyOrders(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * [MỚI] POST /api/orders
     * Khách hàng đặt hàng (checkout) từ giỏ hàng phía frontend.
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                Authentication authentication) {
        OrderDTO created = orderService.createOrder(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getOrderStatus()));
    }
}
