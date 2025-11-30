package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Brand;
import iuh.fit.se.cosmeticsecommercebackend.service.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/brands")

public class BrandController {
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    // --- CRUD CO BAN ---
    
    /** POST /api/brands : Tao thuong hieu moi */
    @PostMapping
    public ResponseEntity<Brand> createBrand(@RequestBody Brand brand) {
        try {
            Brand newBrand = brandService.createBrand(brand);
            return new ResponseEntity<>(newBrand, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /** GET /api/brands/{id} : Lay thong tin thuong hieu theo ID */
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        try {
            Brand brand = brandService.findById(id);
            return ResponseEntity.ok(brand);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** GET /api/brands : Lay tat ca thuong hieu */
    @GetMapping
    public List<Brand> getAllBrands() {
        return brandService.getAll();
    }

    /** PUT /api/brands/{id} : Cap nhat thong tin thuong hieu */
    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @RequestBody Brand brandDetails) {
        try {
            Brand updatedBrand = brandService.updateBrand(id, brandDetails);
            return ResponseEntity.ok(updatedBrand);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** DELETE /api/brands/{id} : Xoa mem thuong hieu */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // --- TIM KIEM ---
    
    /** GET /api/brands/search?name=... : Tim kiem theo ten */
    @GetMapping("/search")
    public List<Brand> searchBrands(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active
    ) {
        if (name != null && active != null) {
            return brandService.findByNameAndActive(name, active);
        } else if (name != null) {
            return brandService.findByName(name);
        } else if (active != null) {
            return brandService.findByActive(active);
        }
        return brandService.getAll();
    }

    // --- THAY DOI TRANG THAI ---
    
    /** POST /api/brands/{id}/deactivate : Vo hieu hoa thuong hieu */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Brand> deactivateBrand(@PathVariable Long id) {
        try {
            Brand brand = brandService.deactivateBrand(id);
            return ResponseEntity.ok(brand);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** POST /api/brands/{id}/activate : Kich hoat lai thuong hieu */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Brand> activateBrand(@PathVariable Long id) {
        try {
            Brand brand = brandService.activateBrand(id);
            return ResponseEntity.ok(brand);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}