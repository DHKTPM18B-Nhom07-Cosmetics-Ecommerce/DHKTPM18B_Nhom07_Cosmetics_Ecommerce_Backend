package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Voucher;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.VoucherRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.VoucherService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository repo;

    public VoucherServiceImpl(VoucherRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Voucher> getAll() {
        List<Voucher> list = repo.findAll();
        list.forEach(this::updateStatusIfNeeded);
        return list;
    }

    @Override
    public Optional<Voucher> getById(Long id) {
        return repo.findById(id).map(v -> {
            updateStatusIfNeeded(v);
            return v;
        });
    }

    @Override
    public Voucher create(Voucher v) {
        v.setStatus(VoucherStatus.UPCOMING);
        return repo.save(v);
    }

    @Override
    public Voucher update(Long id, Voucher newVoucher) {
        return repo.findById(id)
                .map(v -> {
                    v.setCode(newVoucher.getCode());
                    v.setType(newVoucher.getType());
                    v.setValue(newVoucher.getValue());
                    v.setMaxDiscount(newVoucher.getMaxDiscount());
                    v.setMinOrderAmount(newVoucher.getMinOrderAmount());
                    v.setStartAt(newVoucher.getStartAt());
                    v.setEndAt(newVoucher.getEndAt());
                    v.setMaxUses(newVoucher.getMaxUses());
                    v.setPerUserLimit(newVoucher.getPerUserLimit());
                    v.setStackable(newVoucher.isStackable());
                    v.setStatus(newVoucher.getStatus());
                    return repo.save(v);
                })
                .orElseThrow(() -> new RuntimeException("Voucher not found with id: " + id));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    /**
     * Hàm tự động cập nhật trạng thái voucher dựa vào thời gian hiện tại
     */
    private void updateStatusIfNeeded(Voucher v) {
        LocalDateTime now = LocalDateTime.now();

        if (v.getStatus() == VoucherStatus.DISABLED)
            return;

        if (now.isBefore(v.getStartAt()))
            v.setStatus(VoucherStatus.UPCOMING);
        else if (now.isAfter(v.getEndAt()))
            v.setStatus(VoucherStatus.EXPIRED);
        else
            v.setStatus(VoucherStatus.ACTIVE);
    }
}
