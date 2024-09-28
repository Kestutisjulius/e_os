package eu.minted.eos.repository;

import eu.minted.eos.model.Product;
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

   @Query(value = "SELECT * FROM products ORDER BY RAND() LIMIT :limit", nativeQuery = true)
   List<Product> findRandomProducts(@Param("limit") int limit);

}
