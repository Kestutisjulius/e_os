package eu.minted.eos.repository;

import eu.minted.eos.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category getCategoryByName(String name);

    Category getCategoryById(Long categoryId);
}
