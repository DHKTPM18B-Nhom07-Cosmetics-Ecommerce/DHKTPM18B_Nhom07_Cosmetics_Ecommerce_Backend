package iuh.fit.se.cosmeticsecommercebackend.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueStatsRespond {
    private LocalDate date;
    private BigDecimal revenue;
    private Long orders;

    public RevenueStatsRespond(LocalDate date, BigDecimal revenue, Long orders) {
        this.date = date;
        this.revenue = revenue;
        this.orders = orders;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Long getOrders() {
        return orders;
    }

    public void setOrders(Long orders) {
        this.orders = orders;
    }
}
