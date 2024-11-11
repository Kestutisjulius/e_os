package eu.minted.eos.service;

import eu.minted.eos.model.Product;
import eu.minted.eos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByUserId(Long userId) {
        return productRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRandomProducts(int i) {
        return productRepository.findRandomProducts(i);
    }

    @Override
    public Page<Product> getProductsByCategoryId(Long categoryId, PageRequest of) {
        return productRepository.getProductByCategoryId(categoryId, of);
    }

    @Override
    public boolean productExists(Map<String, Object> productData) {
        String name = productData.get("name").toString();
        String description = productData.get("description").toString();
        return productRepository.productExists(name, description);
    }

    @Override
    public void increaseStock(Long id, Integer quantity) {
    Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    product.setQuantity(product.getQuantity() + quantity);
    productRepository.save(product);
    }
}

