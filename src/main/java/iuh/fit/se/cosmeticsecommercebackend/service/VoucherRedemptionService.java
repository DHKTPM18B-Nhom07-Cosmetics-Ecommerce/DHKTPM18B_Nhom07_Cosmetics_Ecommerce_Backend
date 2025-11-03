package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.VoucherRedemption;
import java.util.List;
import java.util.Optional;

public interface VoucherRedemptionService {
    List<VoucherRedemption> getAll();
    Optional<VoucherRedemption> getById(Long id);
    VoucherRedemption create(VoucherRedemption redemption);
    VoucherRedemption update(Long id, VoucherRedemption redemption);
    void delete(Long id);
}
