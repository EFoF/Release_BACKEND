package com.service.releasenote.domain.category.dao;

import com.service.releasenote.domain.category.model.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryCustomRepository {

    @EntityGraph(attributePaths = {"project"})
    List<Category> findByProjectId(Long projectId);

    Boolean existsByProjectId(Long projectId);
    @EntityGraph(attributePaths = {"project"})
    Optional<Category> findById(Long id);

    @Query(value = "SELECT * FROM category c WHERE c.project_id = :project_id", nativeQuery = true)
    List<Category> findCategoryByProjectId(@Param("project_id") Long project_id);
}
