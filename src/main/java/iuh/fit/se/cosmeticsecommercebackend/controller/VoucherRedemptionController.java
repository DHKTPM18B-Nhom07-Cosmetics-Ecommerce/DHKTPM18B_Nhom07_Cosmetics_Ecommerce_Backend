package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.VoucherRedemption;
import iuh.fit.se.cosmeticsecommercebackend.service.VoucherRedemptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voucher-redemptions")
public class VoucherRedemptionController {

    private final VoucherRedemptionService service;

    public VoucherRedemptionController(VoucherRedemptionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VoucherRedemption>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherRedemption> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VoucherRedemption> create(@RequestBody VoucherRedemption redemption) {
        return ResponseEntity.ok(service.create(redemption));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherRedemption> update(@PathVariable Long id, @RequestBody VoucherRedemption redemption) {
        return ResponseEntity.ok(service.update(id, redemption));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
