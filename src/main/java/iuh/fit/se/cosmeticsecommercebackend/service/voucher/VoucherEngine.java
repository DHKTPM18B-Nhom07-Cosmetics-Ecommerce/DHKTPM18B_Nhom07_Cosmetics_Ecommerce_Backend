package iuh.fit.se.cosmeticsecommercebackend.service.voucher;

import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.Voucher;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class VoucherEngine {

    /* ================= PREVIEW (CHO FE) ================= */

    public DiscountResult preview(
            BigDecimal subtotal,
            BigDecimal shippingFee,
            List<Map<String, Object>> items,
            List<Voucher> vouchers
    ) {
        Order fake = new Order();
        fake.setSubtotal(subtotal);
        fake.setShippingFee(shippingFee);
        fake.setOrderDetails(buildFakeDetails(items));

        return applyInternal(fake, vouchers);
    }

    /* ================= APPLY THẬT (KHI TẠO ORDER) ================= */

    public DiscountResult apply(Order order, List<Voucher> vouchers) {
        return applyInternal(order, vouchers);
    }

    /* ================= CORE ================= */

    private DiscountResult applyInternal(
            Order order,
            List<Voucher> vouchers
    ) {
        DiscountResult result = new DiscountResult();

        BigDecimal orderDiscount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        Map<Long, BigDecimal> itemDiscounts = new HashMap<>();

        for (Voucher v : vouchers) {
            if (v.getType() == VoucherType.SHIPPING_FREE) {
                shippingDiscount = shippingDiscount.add(order.getShippingFee());
            }

            if (v.getType() == VoucherType.AMOUNT) {
                orderDiscount = orderDiscount.add(v.getValue());
            }

            if (v.getType() == VoucherType.PERCENT) {
                BigDecimal percent = v.getValue().divide(BigDecimal.valueOf(100));
                BigDecimal raw = order.getSubtotal().multiply(percent);

                if (v.getMaxDiscount() != null) {
                    raw = raw.min(v.getMaxDiscount());
                }
                orderDiscount = orderDiscount.add(raw);
            }
        }

        result.setOrderDiscount(orderDiscount);
        result.setShippingDiscount(shippingDiscount);
        result.setItemDiscounts(itemDiscounts);

        return result;
    }

    /* ================= HELPER ================= */

    private List<OrderDetail> buildFakeDetails(List<Map<String, Object>> items) {
        List<OrderDetail> list = new ArrayList<>();

        for (Map<String, Object> i : items) {
            OrderDetail od = new OrderDetail();
            BigDecimal price = new BigDecimal(i.get("price").toString());
            int qty = Integer.parseInt(i.get("quantity").toString());
            od.setQuantity(qty);
            od.setUnitPrice(price);
            od.recalc();
            list.add(od);
        }
        return list;
    }
}
