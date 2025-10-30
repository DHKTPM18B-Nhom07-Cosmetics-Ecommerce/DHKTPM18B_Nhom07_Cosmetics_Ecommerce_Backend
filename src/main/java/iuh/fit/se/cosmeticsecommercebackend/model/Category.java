package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho danh mục sản phẩm
 * Quan hệ 1-n với Product
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "products")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Quan hệ 1-n với Product
     * mappedBy = "category" tham chiếu đến thuộc tính category trong Product entity
     * cascade = CascadeType.ALL: các thao tác trên Category sẽ cascade sang Product
     * orphanRemoval = true: tự động xóa Product khi bị remove khỏi collection
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    

}

