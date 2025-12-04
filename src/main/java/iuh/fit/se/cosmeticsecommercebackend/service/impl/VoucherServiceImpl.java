package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.model.Voucher;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherScope;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherStatus;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherType;
import iuh.fit.se.cosmeticsecommercebackend.repository.BrandRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.CategoryRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.VoucherRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.VoucherService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;
    private final ProductRepository productRepo;

    public VoucherServiceImpl(
            VoucherRepository voucherRepo,
            CategoryRepository categoryRepo,
            BrandRepository brandRepo,
            ProductRepository productRepo
    ) {
        this.voucherRepo = voucherRepo;
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.productRepo = productRepo;
    }

    /* ============================================================
                            GET / CRUD
       ============================================================ */

    @Override
    public List<Voucher> getAll() {
        List<Voucher> list = voucherRepo.findAll();
        list.forEach(v -> v.setStatus(autoStatus(v)));
        return list;
    }

    @Override
    public Optional<Voucher> findById(Long id) {
        return voucherRepo.findById(id)
                .map(v -> {
                    v.setStatus(autoStatus(v));
                    return v;
                });
    }

    @Override
    public Voucher create(Voucher v,
                          List<Long> categoryIds,
                          List<Long> brandIds,
                          List<Long> productIds) {

        // đảm bảo code luôn IN HOA
        if (v.getCode() != null) {
            v.setCode(v.getCode().toUpperCase());
        }

        applyScopeLinks(v, categoryIds, brandIds, productIds);

        v.setStatus(VoucherStatus.UPCOMING);

        return voucherRepo.save(v);
    }

    @Override
    public Voucher update(Long id,
                          Voucher newV,
                          List<Long> categoryIds,
                          List<Long> brandIds,
                          List<Long> productIds) {

        return voucherRepo.findById(id).map(v -> {

            // KHÔNG cho sửa code ở đây, giữ nguyên v.getCode()

            v.setType(newV.getType());
            v.setValue(newV.getValue());
            v.setMaxDiscount(newV.getMaxDiscount());
            v.setMinOrderAmount(newV.getMinOrderAmount());
            v.setMaxUses(newV.getMaxUses());
            v.setPerUserLimit(newV.getPerUserLimit());
            v.setStackable(newV.isStackable());
            v.setStartAt(newV.getStartAt());
            v.setEndAt(newV.getEndAt());

            /* ======== COMBO FINAL ======== */
            v.setMinItemCount(newV.getMinItemCount());

            v.setScope(newV.getScope());
            applyScopeLinks(v, categoryIds, brandIds, productIds);

            if (v.getStatus() != VoucherStatus.DISABLED) {
                v.setStatus(autoStatus(v));
            }

            return voucherRepo.save(v);

        }).orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
    }

    @Override
    public void delete(Long id) {
        voucherRepo.deleteById(id);
    }

    @Override
    public Voucher updateStatus(Long id, VoucherStatus newStatus) {
        return voucherRepo.findById(id).map(v -> {

            if (v.getStatus() == VoucherStatus.EXPIRED)
                throw new RuntimeException("Voucher đã hết hạn");

            if (newStatus == VoucherStatus.EXPIRED)
                throw new RuntimeException("Không thể set EXPIRED thủ công");

            LocalDateTime now = LocalDateTime.now();

            if (newStatus == VoucherStatus.ACTIVE &&
                    (now.isBefore(v.getStartAt()) || now.isAfter(v.getEndAt())))
                throw new RuntimeException("Không thể bật ACTIVE ngoài thời gian hiệu lực");

            v.setStatus(newStatus);
            return voucherRepo.save(v);

        }).orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
    }

    /* ============================================================
                            AUTO STATUS
       ============================================================ */

    private VoucherStatus autoStatus(Voucher v) {

        if (v.getStatus() == VoucherStatus.DISABLED)
            return VoucherStatus.DISABLED;

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(v.getStartAt()))
            return VoucherStatus.UPCOMING;

        if (now.isAfter(v.getEndAt()))
            return VoucherStatus.EXPIRED;

        return VoucherStatus.ACTIVE;
    }

    /* ============================================================
                     APPLY SCOPE LINKS
       ============================================================ */

    private void applyScopeLinks(
            Voucher v,
            List<Long> categoryIds,
            List<Long> brandIds,
            List<Long> productIds
    ) {
        v.getCategories().clear();
        v.getBrands().clear();
        v.getProducts().clear();

        if (v.getScope() == VoucherScope.CATEGORY)
            v.getCategories().addAll(categoryRepo.findAllById(categoryIds));

        if (v.getScope() == VoucherScope.BRAND)
            v.getBrands().addAll(brandRepo.findAllById(brandIds));

        if (v.getScope() == VoucherScope.PRODUCT)
            v.getProducts().addAll(productRepo.findAllById(productIds));
    }

    /* ============================================================
                     BULK IMPORT – CLEAN
       ============================================================ */

    @Override
    @Transactional
    public int importBulk(List<Map<String, Object>> rows) {

        for (Map<String, Object> m : rows) {

            Voucher v = new Voucher();

            // luôn lưu code IN HOA
            v.setCode(m.get("code").toString().trim().toUpperCase());

            v.setType(VoucherType.valueOf(m.get("type").toString().toUpperCase()));
            v.setScope(VoucherScope.valueOf(m.get("scope").toString().toUpperCase()));

            v.setValue(new BigDecimal(m.get("value").toString()));

            v.setMaxDiscount(m.get("maxDiscount") == null || m.get("maxDiscount").toString().trim().isEmpty()
                    ? null
                    : new BigDecimal(m.get("maxDiscount").toString()));

            v.setMinOrderAmount(new BigDecimal(
                    m.get("minOrderAmount") == null ? "0" : m.get("minOrderAmount").toString()
            ));

            v.setStartAt(LocalDateTime.parse(m.get("startAt").toString().replace(" ", "T")));
            v.setEndAt(LocalDateTime.parse(m.get("endAt").toString().replace(" ", "T")));

            v.setMaxUses(parseIntClean(m.get("maxUses")));
            v.setPerUserLimit(parseIntClean(m.get("perUserLimit")));
            v.setStackable(m.get("stackable").toString().equalsIgnoreCase("true"));

            /* COMBO final */
            v.setMinItemCount(parseIntClean(m.get("minItemCount")));

            v.setStatus(VoucherStatus.UPCOMING);

            List<Long> catIds = parseIds(m.get("categoryIds"));
            List<Long> brandIds = parseIds(m.get("brandIds"));
            List<Long> productIds = parseIds(m.get("productIds"));

            applyScopeLinks(v, catIds, brandIds, productIds);
            voucherRepo.save(v);
        }

        return rows.size();
    }

    private List<Long> parseIds(Object obj) {
        if (obj == null) return List.of();

        String s = obj.toString().trim();
        if (s.isEmpty()) return List.of();

        List<Long> ids = new ArrayList<>();

        for (String p : s.split(",")) {
            try {
                String val = p.trim();
                if (val.endsWith(".0"))
                    val = val.substring(0, val.length() - 2);
                ids.add(Long.valueOf(val));
            } catch (Exception ignore) {}
        }

        return ids;
    }

    /* ============================================================
                        APPLY VOUCHER
       ============================================================ */

    @Override
    public Map<String, Object> applyVoucher(String code, List<Map<String, Object>> items) {

        Map<String, Object> res = new HashMap<>();

        if (code == null || code.trim().isEmpty()) {
            res.put("valid", false);
            res.put("message", "Thiếu mã voucher");
            res.put("discount", 0);
            return res;
        }

        Voucher v = voucherRepo.findByCodeIgnoreCase(code.trim()).orElse(null);

        if (v == null) {
            res.put("valid", false);
            res.put("message", "Voucher không tồn tại");
            res.put("discount", 0);
            return res;
        }

        // 1. auto status
        VoucherStatus runtimeStatus = autoStatus(v);

        if (v.getStatus() != runtimeStatus && v.getStatus() != VoucherStatus.DISABLED) {
            v.setStatus(runtimeStatus);
            voucherRepo.save(v);
        }

        if (runtimeStatus != VoucherStatus.ACTIVE) {
            res.put("valid", false);
            res.put("message", "Voucher không khả dụng");
            res.put("discount", 0);
            return res;
        }

        // 2. tổng số lượng sp
        int totalItems = items.stream()
                .mapToInt(i -> safeInt(i.getOrDefault("quantity", 0)))
                .sum();

        if (v.getMinItemCount() != null && totalItems < v.getMinItemCount()) {
            res.put("valid", false);
            res.put("message", "Cần mua tối thiểu " + v.getMinItemCount() + " sản phẩm");
            res.put("discount", 0);
            return res;
        }

        // 3. lấy danh sách productId
        List<Long> productIds = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Object raw = item.get("productId");
            if (raw == null) continue; // bỏ qua item lỗi
            try {
                productIds.add(Long.valueOf(raw.toString()));
            } catch (Exception ignored) {}
        }

        if (productIds.isEmpty()) {
            res.put("valid", false);
            res.put("message", "Không có productId hợp lệ trong giỏ");
            res.put("discount", 0);
            return res;
        }

        List<Product> cartProducts = productRepo.findAllById(productIds);

        boolean validScope = switch (v.getScope()) {
            case GLOBAL -> true;
            case PRODUCT -> cartProducts.stream().anyMatch(p -> v.getProducts().contains(p));
            case BRAND -> cartProducts.stream().anyMatch(p -> v.getBrands().contains(p.getBrand()));
            case CATEGORY -> cartProducts.stream().anyMatch(p -> v.getCategories().contains(p.getCategory()));
        };

        if (!validScope) {
            res.put("valid", false);
            res.put("message", "Không sản phẩm nào trong giỏ thuộc phạm vi áp dụng");
            res.put("discount", 0);
            return res;
        }

        // 4. tính tổng tiền
        BigDecimal total = BigDecimal.ZERO;

        for (Map<String, Object> item : items) {
            Object rawPid = item.get("productId");
            if (rawPid == null) continue;

            Long pid;
            try {
                pid = Long.valueOf(rawPid.toString());
            } catch (Exception e) {
                continue;
            }

            int qty = safeInt(item.getOrDefault("quantity", 0));

            Product p = productRepo.findById(pid).orElse(null);
            if (p == null || p.getVariants().isEmpty()) continue;

            BigDecimal price = p.getVariants().get(0).getPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        if (total.compareTo(v.getMinOrderAmount()) < 0) {
            res.put("valid", false);
            res.put("message", "Chưa đạt đơn tối thiểu " + v.getMinOrderAmount());
            res.put("discount", 0);
            return res;
        }

        // 5. tính discount
        BigDecimal discount = switch (v.getType()) {
            case PERCENT -> total.multiply(v.getValue()).divide(BigDecimal.valueOf(100));
            case AMOUNT -> v.getValue();
            case SHIPPING_FREE -> BigDecimal.valueOf(30000);
        };

        if (v.getMaxDiscount() != null)
            discount = discount.min(v.getMaxDiscount());

        res.put("valid", true);
        res.put("message", "Áp dụng thành công");
        res.put("discount", discount);

        return res;
    }


    /* ============================================================
                           HELPERS
       ============================================================ */

    private int safeInt(Object o) {
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    // parse int cho import Excel, xử lý "500.0"
    private Integer parseIntClean(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        if (s.isEmpty()) return null;
        if (s.endsWith(".0")) {
            s = s.substring(0, s.length() - 2);
        }
        try {
            return Integer.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }
}
