package eu.minted.eos.service;

import eu.minted.eos.model.Category;

import java.util.List;

public interface CategoryService {
    Category getCategoryByName(String name);

    List<Category> getAllCategories();

    Category getCategoryById(Long categoryId);
}
