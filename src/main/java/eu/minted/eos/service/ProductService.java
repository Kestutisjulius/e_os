package eu.minted.eos.service;

import eu.minted.eos.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product createProduct(Product product);
    Optional<Product>getProductById(Long id);
    Optional<Product>getProductByName(String name);
    List<Product>getAllProducts();
    Product updateProduct(Product product);
    void deleteProduct(Long id);
}
