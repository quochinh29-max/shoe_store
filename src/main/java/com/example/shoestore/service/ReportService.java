package com.example.shoestore.service;

import com.example.shoestore.dto.DailyRevenueDTO;
import com.example.shoestore.dto.RevenueSummaryDTO;
import com.example.shoestore.dto.TopProductDTO;
import com.example.shoestore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;

    /**
     * Tổng quan doanh thu + số đơn theo trạng thái, trong N ngày gần nhất.
     */
    public RevenueSummaryDTO getSummary(int days) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(days - 1L).with(LocalTime.MIN);

        BigDecimal totalRevenue = orderRepository.sumRevenueBetween(from, to);
        long totalOrders = orderRepository.countOrdersBetween(from, to);
        long completedOrders = orderRepository.countByStatusBetween("COMPLETED", from, to);
        long pendingOrders = orderRepository.countByStatusBetween("PENDING", from, to);
        long shippingOrders = orderRepository.countByStatusBetween("SHIPPING", from, to);
        long cancelledOrders = orderRepository.countByStatusBetween("CANCELLED", from, to);

        BigDecimal avgOrderValue = completedOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RevenueSummaryDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .pendingOrders(pendingOrders)
                .shippingOrders(shippingOrders)
                .cancelledOrders(cancelledOrders)
                .averageOrderValue(avgOrderValue)
                .build();
    }

    /**
     * Doanh thu theo từng ngày trong N ngày gần nhất — dùng để vẽ biểu đồ.
     */
    public List<DailyRevenueDTO> getDailyRevenue(int days) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(days - 1L).with(LocalTime.MIN);

        List<Object[]> rows = orderRepository.findDailyRevenue(from, to);
        List<DailyRevenueDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            long orderCount = toLong(row[1]);
            BigDecimal revenue = toBigDecimal(row[2]);
            result.add(DailyRevenueDTO.builder()
                    .date(date)
                    .orderCount(orderCount)
                    .revenue(revenue)
                    .build());
        }
        return result;
    }

    /**
     * Top N sản phẩm bán chạy nhất trong khoảng thời gian.
     */
    public List<TopProductDTO> getTopProducts(int days, int limit) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(days - 1L).with(LocalTime.MIN);

        List<Object[]> rows = orderRepository.findTopSellingProducts(from, to, limit);
        List<TopProductDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            result.add(TopProductDTO.builder()
                    .productId((Integer) row[0])
                    .productName((String) row[1])
                    .totalSold(toLong(row[2]))
                    .revenue(toBigDecimal(row[3]))
                    .build());
        }
        return result;
    }

    // ─── Helpers: chuyển đổi kiểu dữ liệu trả về từ native query một cách an toàn ───

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Date) {
            return ((Date) value).toLocalDate();
        }
        if (value instanceof java.time.LocalDate) {
            return (LocalDate) value;
        }
        return LocalDate.parse(value.toString());
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }
}
