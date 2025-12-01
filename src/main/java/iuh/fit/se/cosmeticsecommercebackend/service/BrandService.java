package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Brand;
import java.util.List;

public interface BrandService {
    //CRUD co ban
    Brand createBrand(Brand brand);
    Brand findById(Long id);
    List<Brand> getAll();
    Brand updateBrand(Long id, Brand brandDetails);
    void deleteBrand(Long id);  //chi xoa mem (set isActive = false)
    
    //Tim kiem
    List<Brand> findByName(String name);
    List<Brand> findByActive(boolean isActive);
    List<Brand> findByNameAndActive(String name, boolean isActive);
    
    //Nghiep vu khac
    Brand deactivateBrand(Long id);  //vo hieu hoa thuong hieu
    Brand activateBrand(Long id);    //kich hoat lai thuong hieu
}