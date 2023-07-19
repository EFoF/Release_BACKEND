package com.service.releasenote.domain.category.dao;

import com.service.releasenote.domain.category.model.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryCustomRepository {

    @EntityGraph(attributePaths = {"project"})
    List<Category> findByProjectId(Long projectId);

    Boolean existsByProjectId(Long projectId);
    @EntityGraph(attributePaths = {"project"})
    Optional<Category> findById(Long id);
}
