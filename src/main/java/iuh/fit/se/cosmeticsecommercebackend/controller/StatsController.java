package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.payload.RevenueStatsRespond;
import iuh.fit.se.cosmeticsecommercebackend.payload.TopProductRespond;
import iuh.fit.se.cosmeticsecommercebackend.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // Yêu cầu: Đăng nhập và có quyền ADMIN hoặc EMPLOYEE
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/revenue/daily")
    public ResponseEntity<List<RevenueStatsRespond>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<RevenueStatsRespond> revenueData = statsService.getDailyRevenue(startDate, endDate);
        return ResponseEntity.ok(revenueData);
    }

//     Yêu cầu: Đăng nhập và có quyền ADMIN hoặc EMPLOYEE
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/products/top5")
    public ResponseEntity<List<TopProductRespond>> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<TopProductRespond> topProducts = statsService.getTopSellingProducts(startDate, endDate);
        return ResponseEntity.ok(topProducts);
    }
}