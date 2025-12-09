package iuh.fit.se.cosmeticsecommercebackend.payload;

import java.math.BigDecimal;

public class TopProductRespond {
    private Long variantId;
    private String name;
    private Long sales;
    private BigDecimal revenue;

    public TopProductRespond() {
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSales() {
        return sales;
    }

    public void setSales(Long sales) {
        this.sales = sales;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
