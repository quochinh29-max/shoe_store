package com.example.shoestore.controller;

import com.example.shoestore.dto.DailyRevenueDTO;
import com.example.shoestore.dto.RevenueSummaryDTO;
import com.example.shoestore.dto.TopProductDTO;
import com.example.shoestore.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/reports/summary?days=30
     * Tổng quan doanh thu + số đơn theo trạng thái trong N ngày gần nhất.
     */
    @GetMapping("/summary")
    public ResponseEntity<RevenueSummaryDTO> getSummary(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.getSummary(days));
    }

    /**
     * GET /api/reports/daily?days=30
     * Doanh thu theo từng ngày, dùng để vẽ biểu đồ.
     */
    @GetMapping("/daily")
    public ResponseEntity<List<DailyRevenueDTO>> getDailyRevenue(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.getDailyRevenue(days));
    }

    /**
     * GET /api/reports/top-products?days=30&limit=5
     * Top sản phẩm bán chạy nhất.
     */
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reportService.getTopProducts(days, limit));
    }
}
