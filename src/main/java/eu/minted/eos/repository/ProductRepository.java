package eu.minted.eos.repository;

import eu.minted.eos.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

   Optional<Product> findByName(String name);
   List<Product> findByUserId(Long userId);

   Page<Product> findAll(Pageable pageable);

   @Query(value = "SELECT * FROM products ORDER BY RAND() LIMIT :limit", nativeQuery = true)
   List<Product> findRandomProducts(@Param("limit") int limit);

   Page<Product>getProductByCategoryId(Long categoryId, PageRequest pageRequest);

   @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.name = :name AND p.description = :description")
   boolean productExists(@Param("name") String name, @Param("description") String description);
}
