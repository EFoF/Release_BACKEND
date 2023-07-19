package com.service.releasenote.domain.release.dao;

import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.release.model.Releases;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReleaseRepository extends JpaRepository<Releases, Long>, ReleaseCustomRepository {

    // category에 굳이 접근할 일이 없어서 fetch join은 적용하지 않겠다.
    // @EntityGraph(attributePaths = {"category"})
    List<Releases> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    List<Releases> findByCategoryIdIn(List<Long> categoryIdList);

}