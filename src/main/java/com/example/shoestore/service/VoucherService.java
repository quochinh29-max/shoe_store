package com.example.shoestore.service;

import com.example.shoestore.dto.VoucherDTO;
import com.example.shoestore.entity.Voucher;
import com.example.shoestore.exception.ResourceNotFoundException;
import com.example.shoestore.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;

    @Transactional(readOnly = true)
    public List<VoucherDTO> getAllVouchers() {
        return voucherRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VoucherDTO getVoucherById(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", id));
        return toDTO(voucher);
    }

    /**
     * [MỚI] Tra cứu voucher theo mã — dùng ở trang giỏ hàng để khách xem trước
     * số tiền được giảm trước khi đặt hàng thật.
     */
    @Transactional(readOnly = true)
    public VoucherDTO getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "code", code));
        return toDTO(voucher);
    }

    @Transactional
    public VoucherDTO createVoucher(VoucherDTO dto) {
        if (voucherRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Mã voucher '" + dto.getCode() + "' đã tồn tại");
        }
        Voucher voucher = toEntity(dto);
        voucher.setUsedCount(0);
        Voucher saved = voucherRepository.save(voucher);
        return toDTO(saved);
    }

    @Transactional
    public VoucherDTO updateVoucher(Integer id, VoucherDTO dto) {
        Voucher existing = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", id));

        // Nếu đổi code, kiểm tra trùng với voucher khác
        if (!existing.getCode().equalsIgnoreCase(dto.getCode())
                && voucherRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Mã voucher '" + dto.getCode() + "' đã tồn tại");
        }

        existing.setCode(dto.getCode().toUpperCase());
        existing.setDiscountType(dto.getDiscountType());
        existing.setDiscountValue(dto.getDiscountValue());
        existing.setMinOrderValue(dto.getMinOrderValue());
        existing.setMaxDiscount(dto.getMaxDiscount());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setUsageLimit(dto.getUsageLimit());

        Voucher saved = voucherRepository.save(existing);
        return toDTO(saved);
    }

    @Transactional
    public void deleteVoucher(Integer id) {
        if (!voucherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Voucher", "id", id);
        }
        voucherRepository.deleteById(id);
    }

    // ─── Mapping helpers ───

    private VoucherDTO toDTO(Voucher v) {
        return VoucherDTO.builder()
                .id(v.getId())
                .code(v.getCode())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .maxDiscount(v.getMaxDiscount())
                .startDate(v.getStartDate())
                .endDate(v.getEndDate())
                .usageLimit(v.getUsageLimit())
                .usedCount(v.getUsedCount())
                .status(computeStatus(v))
                .build();
    }

    private Voucher toEntity(VoucherDTO dto) {
        return Voucher.builder()
                .code(dto.getCode().toUpperCase())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minOrderValue(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : java.math.BigDecimal.ZERO)
                .maxDiscount(dto.getMaxDiscount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usageLimit(dto.getUsageLimit())
                .usedCount(0)
                .build();
    }

    private String computeStatus(Voucher v) {
        LocalDateTime now = LocalDateTime.now();

        if (v.getUsageLimit() != null && v.getUsedCount() != null
                && v.getUsedCount() >= v.getUsageLimit()) {
            return "OUT_OF_USES";
        }
        if (v.getStartDate() != null && now.isBefore(v.getStartDate())) {
            return "UPCOMING";
        }
        if (v.getEndDate() != null && now.isAfter(v.getEndDate())) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}
