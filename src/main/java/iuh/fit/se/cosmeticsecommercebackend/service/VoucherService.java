package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Voucher;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VoucherService {
    List<Voucher> getAll();
    Optional<Voucher> findById(Long id);
    Voucher create(Voucher v, List<Long> catIds, List<Long> brandIds, List<Long> productIds);
    Voucher update(Long id, Voucher v, List<Long> catIds, List<Long> brandIds, List<Long> productIds);
    void delete(Long id);
    Voucher updateStatus(Long id, VoucherStatus newStatus);

    int importBulk(List<Map<String, Object>> rows);

}



