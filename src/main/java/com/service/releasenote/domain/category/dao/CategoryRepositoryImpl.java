package com.service.releasenote.domain.category.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.category.model.QCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static com.service.releasenote.domain.category.model.QCategory.*;
import static com.service.releasenote.domain.company.model.QCompany.*;
import static com.service.releasenote.domain.project.model.QProject.*;

@Slf4j
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<Category> findByProject(Long projectId) {
        List<Category> categoryList = jpaQueryFactory
                .selectFrom(category)
                .innerJoin(category.project, project).fetchJoin()
                .where(category.project.id.eq(projectId))
                .fetch();
        return categoryList;
    }

    @Override
    public Optional<Category> findByIntersectionId(Long companyId, Long projectId, Long categoryId) {
        Category result = jpaQueryFactory
                .selectFrom(category)
                .innerJoin(category.project, project).fetchJoin()
                .innerJoin(project.company, company).fetchJoin()
                .where(
                        company.id.eq(companyId),
                        project.id.eq(projectId),
                        category.id.eq(categoryId)
                ).fetchOne();
        return Optional.ofNullable(result);
    }
}
