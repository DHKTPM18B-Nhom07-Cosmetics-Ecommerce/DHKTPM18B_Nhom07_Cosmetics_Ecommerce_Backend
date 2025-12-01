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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /* =============================
            GET ALL
       ============================= */
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

    /* =============================
                 CREATE
       ============================= */
    @Override
    public Voucher create(Voucher v,
                          List<Long> categoryIds,
                          List<Long> brandIds,
                          List<Long> productIds) {

        applyScopeLinks(v, categoryIds, brandIds, productIds);

        // Create = luôn UPCOMING theo yêu cầu
        v.setStatus(VoucherStatus.UPCOMING);

        return voucherRepo.save(v);
    }

    /* =============================
                 UPDATE
       ============================= */
    @Override
    public Voucher update(Long id,
                          Voucher newV,
                          List<Long> categoryIds,
                          List<Long> brandIds,
                          List<Long> productIds) {

        return voucherRepo.findById(id).map(v -> {

            v.setType(newV.getType());
            v.setValue(newV.getValue());
            v.setMaxDiscount(newV.getMaxDiscount());
            v.setMinOrderAmount(newV.getMinOrderAmount());
            v.setStartAt(newV.getStartAt());
            v.setEndAt(newV.getEndAt());
            v.setMaxUses(newV.getMaxUses());
            v.setPerUserLimit(newV.getPerUserLimit());
            v.setStackable(newV.isStackable());
            v.setScope(newV.getScope());

            applyScopeLinks(v, categoryIds, brandIds, productIds);

            // nếu không bị DISABLED thì cập nhật theo thời gian
            if (v.getStatus() != VoucherStatus.DISABLED) {
                v.setStatus(autoStatus(v));
            }

            return voucherRepo.save(v);

        }).orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
    }

    /* =============================
                 DELETE
       ============================= */
    @Override
    public void delete(Long id) {
        voucherRepo.deleteById(id);
    }

    /* =============================
           ADMIN UPDATE STATUS
       ============================= */
    @Override
    public Voucher updateStatus(Long id, VoucherStatus newStatus) {
        return voucherRepo.findById(id).map(v -> {

            if (v.getStatus() == VoucherStatus.EXPIRED)
                throw new RuntimeException("Voucher đã hết hạn, không thể cập nhật.");

            if (newStatus == VoucherStatus.EXPIRED)
                throw new RuntimeException("Không thể đặt EXPIRED thủ công.");

            LocalDateTime now = LocalDateTime.now();

            if (newStatus == VoucherStatus.ACTIVE &&
                    (now.isBefore(v.getStartAt()) || now.isAfter(v.getEndAt())))
                throw new RuntimeException("Không thể bật ACTIVE ngoài thời gian hiệu lực.");

            v.setStatus(newStatus);
            return voucherRepo.save(v);

        }).orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
    }

    /* =============================
             AUTO STATUS CALC
       ============================= */
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

    /* =============================
             SCOPE MAPPING
       ============================= */
    private void applyScopeLinks(Voucher v,
                                 List<Long> categoryIds,
                                 List<Long> brandIds,
                                 List<Long> productIds) {

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
}
