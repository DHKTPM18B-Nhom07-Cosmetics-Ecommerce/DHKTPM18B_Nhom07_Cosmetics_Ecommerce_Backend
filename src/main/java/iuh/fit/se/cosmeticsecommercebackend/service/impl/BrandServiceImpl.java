package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Brand;
import iuh.fit.se.cosmeticsecommercebackend.repository.BrandRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.BrandService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;

    public BrandServiceImpl(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public Brand createBrand(Brand brand) {
        // Mac dinh active = true khi tao moi
        brand.setActive(true);
        return brandRepository.save(brand);
    }

    @Override
    public Brand findById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thương hiệu với ID: " + id));
    }

    @Override
    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    @Override
    public Brand updateBrand(Long id, Brand brandDetails) {
        Brand existingBrand = findById(id);
        
        // Cap nhat thong tin
        existingBrand.setName(brandDetails.getName());
        existingBrand.setDescription(brandDetails.getDescription());
        existingBrand.setLogo(brandDetails.getLogo());
        // Khong cap nhat isActive o day, dung method rieng
        
        return brandRepository.save(existingBrand);
    }

    @Override
    public void deleteBrand(Long id) {
        Brand brand = findById(id);
        // Xoa mem bang cach set isActive = false
        brand.setActive(false);
        brandRepository.save(brand);
    }

    @Override
    public List<Brand> findByName(String name) {
        return brandRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Brand> findByActive(boolean isActive) {
        return brandRepository.findByIsActive(isActive);
    }

    @Override
    public List<Brand> findByNameAndActive(String name, boolean isActive) {
        return brandRepository.findByNameContainingIgnoreCaseAndIsActive(name, isActive);
    }

    @Override
    public Brand deactivateBrand(Long id) {
        Brand brand = findById(id);
        brand.setActive(false);
        return brandRepository.save(brand);
    }

    @Override
    public Brand activateBrand(Long id) {
        Brand brand = findById(id);
        brand.setActive(true);
        return brandRepository.save(brand);
    }
}