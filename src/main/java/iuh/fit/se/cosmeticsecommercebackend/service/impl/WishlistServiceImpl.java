package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.repository.CustomerRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductVariantRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của WishlistService
 * Xử lý logic cho danh sách yêu thích
 */
@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    /**
     * Thêm sản phẩm vào danh sách yêu thích
     * Kiểm tra sản phẩm đã tồn tại chưa để tránh trùng lặp
     */
    @Override
    @Transactional
    public void addToWishlist(Long accountId, Long productVariantId) {
        // Tìm customer từ account ID
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với Account ID: " + accountId));

        // Kiểm tra product variant có tồn tại không
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productVariantId));

        // Lấy danh sách wishlist hiện tại
        List<Long> wishList = customer.getWishList();
        if (wishList == null) {
            wishList = new ArrayList<>();
        }

        // Kiểm tra đã có trong wishlist chưa
        if (wishList.contains(productVariantId)) {
            throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích");
        }

        // Thêm vào wishlist
        wishList.add(productVariantId);
        customer.setWishList(wishList);
        customerRepository.save(customer);
    }

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     */
    @Override
    @Transactional
    public void removeFromWishlist(Long accountId, Long productVariantId) {
        // Tìm customer từ account ID
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với Account ID: " + accountId));

        // Lấy danh sách wishlist
        List<Long> wishList = customer.getWishList();
        if (wishList == null || wishList.isEmpty()) {
            throw new RuntimeException("Danh sách yêu thích đang trống");
        }

        // Xóa khỏi wishlist
        boolean removed = wishList.remove(productVariantId);
        if (!removed) {
            throw new RuntimeException("Sản phẩm không có trong danh sách yêu thích");
        }

        customer.setWishList(wishList);
        customerRepository.save(customer);
    }

    /**
     * Lấy danh sách tất cả sản phẩm yêu thích
     * Trả về danh sách ProductVariant đầy đủ thông tin
     */
    @Override
    public List<ProductVariant> getWishlist(Long accountId) {
        // Tìm customer từ account ID
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với Account ID: " + accountId));

        // Lấy danh sách ID
        List<Long> wishListIds = customer.getWishList();
        if (wishListIds == null || wishListIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Lấy thông tin chi tiết các ProductVariant
        return wishListIds.stream()
                .map(id -> productVariantRepository.findById(id).orElse(null))
                .filter(pv -> pv != null) // Loại bỏ các sản phẩm đã bị xóa
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra sản phẩm có trong wishlist không
     */
    @Override
    public boolean isInWishlist(Long accountId, Long productVariantId) {
        // Tìm customer từ account ID
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với Account ID: " + accountId));

        // Kiểm tra trong wishlist
        List<Long> wishList = customer.getWishList();
        if (wishList == null) {
            return false;
        }

        return wishList.contains(productVariantId);
    }

    /**
     * Xóa toàn bộ wishlist
     */
    @Override
    @Transactional
    public void clearWishlist(Long accountId) {
        // Tìm customer từ account ID
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với Account ID: " + accountId));

        // Xóa toàn bộ wishlist
        customer.setWishList(new ArrayList<>());
        customerRepository.save(customer);
    }
}
