package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Voucher;
import java.util.List;
import java.util.Optional;

public interface VoucherService {
    List<Voucher> getAll();
    Optional<Voucher> getById(Long id);
    Voucher create(Voucher voucher);
    Voucher update(Long id, Voucher voucher);
    void delete(Long id);
}
