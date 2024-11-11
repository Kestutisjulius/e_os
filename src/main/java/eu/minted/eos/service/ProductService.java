package eu.minted.eos.service;

import eu.minted.eos.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductService {
    Product createProduct(Product product);
    Optional<Product>getProductById(Long id);
    Optional<Product>getProductByName(String name);
    Page<Product> getAllProducts(Pageable pageable);

    List<Product>getProductsByUserId(Long userId);
    Product updateProduct(Product product);

    List<Product>getRandomProducts(int count);
    void deleteProduct(Long id);

    Page<Product> getProductsByCategoryId(Long categoryId, PageRequest of);

    boolean productExists(Map<String, Object> productData);

    void increaseStock(Long id, Integer quantity);
}
