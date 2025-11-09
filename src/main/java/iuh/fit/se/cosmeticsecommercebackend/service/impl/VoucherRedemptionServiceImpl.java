package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.VoucherRedemption;
import iuh.fit.se.cosmeticsecommercebackend.repository.VoucherRedemptionRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.VoucherRedemptionService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherRedemptionServiceImpl implements VoucherRedemptionService {

    private final VoucherRedemptionRepository repo;

    public VoucherRedemptionServiceImpl(VoucherRedemptionRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<VoucherRedemption> getAll() {
        return repo.findAll();
    }

    @Override
    public Optional<VoucherRedemption> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public VoucherRedemption create(VoucherRedemption redemption) {
        return repo.save(redemption);
    }

    @Override
    public VoucherRedemption update(Long id, VoucherRedemption newData) {
        return repo.findById(id)
                .map(r -> {
                    r.setVoucher(newData.getVoucher());
                    r.setOrder(newData.getOrder());
                    r.setCustomer(newData.getCustomer());
                    r.setAmountDiscounted(newData.getAmountDiscounted());
                    return repo.save(r);
                })
                .orElseThrow(() -> new RuntimeException("Redemption not found with id: " + id));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
