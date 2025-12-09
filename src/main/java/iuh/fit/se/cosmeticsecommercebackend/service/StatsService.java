package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.payload.RevenueStatsRespond;
import iuh.fit.se.cosmeticsecommercebackend.payload.TopProductRespond;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final OrderRepository orderRepository;

    // Tiêm ProductVariantRepository (để lấy Top Products)
    private final ProductVariantRepository productVariantRepository;

    public StatsService(OrderRepository orderRepository, ProductVariantRepository productVariantRepository) {
        this.orderRepository = orderRepository;
        this.productVariantRepository = productVariantRepository;
    }

    /**
     * Lấy dữ liệu doanh thu theo ngày trong khoảng thời gian
     */
    public List<RevenueStatsRespond> getDailyRevenue(LocalDateTime startDate, LocalDateTime endDate) {

        List<Object[]> rawData = orderRepository.findDailyRevenueAndOrders(startDate, endDate);

        return rawData.stream().map(row -> {

            // 1. ÉP KIỂU an toàn sang java.sql.Date (do Native SQL trả về)
            Date sqlDate = (Date) row[0];
            // 2. Chuyển đổi sang java.time.LocalDate
            java.time.LocalDate date = sqlDate.toLocalDate();

            BigDecimal revenue = (BigDecimal) row[1];
            Long orders = (Long) row[2];

            return new RevenueStatsRespond(date, revenue, orders);
        }).collect(Collectors.toList());
    }

    /**
     * Lấy Top 5 sản phẩm bán chạy nhất trong khoảng thời gian.
     */
    public List<TopProductRespond> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate) {

        List<Object[]> rawData = orderRepository.findTopSellingProductVariants(startDate, endDate);

        return rawData.stream().map(row -> {
            TopProductRespond dto = new TopProductRespond();

            dto.setVariantId(((Number) row[0]).longValue());
            dto.setSales(((Number) row[1]).longValue());
            dto.setRevenue((BigDecimal) row[2]);

            // THIẾT KẾ: Lấy tên Sản phẩm (Bắt buộc phải gọi repository khác trong thực tế)
            // Hiện tại: Giả lập tên sản phẩm/variant.
            ProductVariant variant = productVariantRepository.findById(dto.getVariantId()).orElse(null);
            dto.setName(variant.getProduct().getName() + " " + variant.getVariantName());

            return dto;
        }).collect(Collectors.toList());
    }
}