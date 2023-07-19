package com.service.releasenote.domain.category.dao;

import com.service.releasenote.domain.category.model.Category;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface CategoryCustomRepository {

    List<Category> findByProject(Long projectId);
    Optional<Category> findByIntersectionId(Long companyId, Long projectId, Long categoryId);
}
