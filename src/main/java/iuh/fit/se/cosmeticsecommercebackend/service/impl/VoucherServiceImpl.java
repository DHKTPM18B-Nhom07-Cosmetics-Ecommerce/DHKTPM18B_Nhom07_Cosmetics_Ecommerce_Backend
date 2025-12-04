package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Voucher;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherScope;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherStatus;

import iuh.fit.se.cosmeticsecommercebackend.repository.BrandRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.CategoryRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.VoucherRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.VoucherService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Voucher create(
            Voucher v,
            List<Long> categoryIds,
            List<Long> brandIds,
            List<Long> productIds
    ) {
        applyScopeLinks(v, categoryIds, brandIds, productIds);
        v.setStatus(VoucherStatus.UPCOMING);
        return voucherRepo.save(v);
    }

    @Override
    public Voucher update(
            Long id,
            Voucher newV,
            List<Long> categoryIds,
            List<Long> brandIds,
            List<Long> productIds
    ) {
        return voucherRepo.findById(id).map(v -> {

            // ========== UPDATE BASIC ==========
            v.setType(newV.getType());
            v.setValue(newV.getValue());
            v.setMaxDiscount(newV.getMaxDiscount());
            v.setMinOrderAmount(newV.getMinOrderAmount());
            v.setMaxUses(newV.getMaxUses());
            v.setPerUserLimit(newV.getPerUserLimit());
            v.setStackable(newV.isStackable());
            v.setStartAt(newV.getStartAt());
            v.setEndAt(newV.getEndAt());

            // ========== UPDATE SCOPE ==========
            v.setScope(newV.getScope());
            applyScopeLinks(v, categoryIds, brandIds, productIds);

            // ========== AUTO STATUS ==========
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
        // Clear existing links
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
                         BULK IMPORT (OPTION C)
       ============================================================ */

    @Override
    @Transactional
    public int importBulk(List<Map<String, Object>> rows) {

        for (Map<String, Object> m : rows) {

            Voucher v = new Voucher();

            v.setCode(m.get("code").toString());

            v.setType(Enum.valueOf(
                    iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherType.class,
                    m.get("type").toString().toUpperCase()
            ));

            v.setScope(Enum.valueOf(
                    iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherScope.class,
                    m.get("scope").toString().toUpperCase()
            ));

            v.setValue(new java.math.BigDecimal(m.get("value").toString()));

            v.setMaxDiscount(
                    m.get("maxDiscount") == null ? null :
                            new java.math.BigDecimal(m.get("maxDiscount").toString())
            );

            v.setMinOrderAmount(
                    new java.math.BigDecimal(
                            m.get("minOrderAmount") == null ? "0" : m.get("minOrderAmount").toString()
                    )
            );

            v.setStartAt(LocalDateTime.parse(m.get("startAt").toString().replace(" ", "T")));
            v.setEndAt(LocalDateTime.parse(m.get("endAt").toString().replace(" ", "T")));

            v.setMaxUses(m.get("maxUses") == null ? null : Integer.valueOf(m.get("maxUses").toString()));
            v.setPerUserLimit(m.get("perUserLimit") == null ? null : Integer.valueOf(m.get("perUserLimit").toString()));

            v.setStackable(m.get("stackable").toString().equalsIgnoreCase("true"));
            v.setStatus(VoucherStatus.UPCOMING);

            List<Long> catIds = parseIds(m.get("categoryIds"));
            List<Long> brandIds = parseIds(m.get("brandIds"));
            List<Long> productIds = parseIds(m.get("productIds"));

            voucherRepo.save(v);
            applyScopeLinks(v, catIds, brandIds, productIds);
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
                if (val.endsWith(".0")) val = val.substring(0, val.length() - 2);
                ids.add(Long.valueOf(val));
            } catch (Exception ignore) {}
        }

        return ids;
    }
}
