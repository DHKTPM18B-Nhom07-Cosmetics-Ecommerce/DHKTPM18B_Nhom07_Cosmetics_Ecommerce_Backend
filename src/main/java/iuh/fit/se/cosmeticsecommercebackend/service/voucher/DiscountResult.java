package iuh.fit.se.cosmeticsecommercebackend.service.voucher;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DiscountResult {

    private BigDecimal orderDiscount = BigDecimal.ZERO;
    private BigDecimal shippingDiscount = BigDecimal.ZERO;
    private Map<Long, BigDecimal> itemDiscounts = new HashMap<>();

    public BigDecimal getOrderDiscount() {
        return orderDiscount;
    }

    public void setOrderDiscount(BigDecimal orderDiscount) {
        this.orderDiscount = (orderDiscount == null)
                ? BigDecimal.ZERO
                : orderDiscount.max(BigDecimal.ZERO);
    }

    public void addOrderDiscount(BigDecimal d) {
        if (d != null) {
            setOrderDiscount(this.orderDiscount.add(d));
        }
    }

    public BigDecimal getShippingDiscount() {
        return shippingDiscount;
    }

    public void setShippingDiscount(BigDecimal shippingDiscount) {
        this.shippingDiscount = (shippingDiscount == null)
                ? BigDecimal.ZERO
                : shippingDiscount.max(BigDecimal.ZERO);
    }

    public Map<Long, BigDecimal> getItemDiscounts() {
        return itemDiscounts;
    }

    public void setItemDiscounts(Map<Long, BigDecimal> itemDiscounts) {
        this.itemDiscounts = (itemDiscounts == null)
                ? new HashMap<>()
                : itemDiscounts;
    }

    public void addItemDiscount(Long orderDetailId, BigDecimal discount) {
        if (orderDetailId != null && discount != null) {
            itemDiscounts.put(orderDetailId, discount.max(BigDecimal.ZERO));
        }
    }
}
