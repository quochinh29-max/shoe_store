package com.example.shoestore.controller;

import com.example.shoestore.dto.VoucherDTO;
import com.example.shoestore.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<VoucherDTO>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    /**
     * [MỚI] GET /api/vouchers/validate?code=SUMMER24
     * Tra cứu voucher theo mã — dùng ở trang giỏ hàng để khách xem trước số tiền
     * được giảm. Đặt trước "/{id}" để tránh xung đột path-variable.
     */
    @GetMapping("/validate")
    public ResponseEntity<VoucherDTO> validateVoucher(@RequestParam String code) {
        return ResponseEntity.ok(voucherService.getVoucherByCode(code));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherDTO> getVoucherById(@PathVariable Integer id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping
    public ResponseEntity<VoucherDTO> createVoucher(@Valid @RequestBody VoucherDTO dto) {
        VoucherDTO created = voucherService.createVoucher(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherDTO> updateVoucher(@PathVariable Integer id, @Valid @RequestBody VoucherDTO dto) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(Map.of("message", "Xóa voucher thành công"));
    }
}
