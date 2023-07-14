package com.service.releasenote.domain.category.dao;

import com.service.releasenote.domain.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
