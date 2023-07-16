package com.service.releasenote.domain.release.dao;

import com.service.releasenote.domain.release.model.Releases;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReleaseRepository extends JpaRepository<Releases, Long>, ReleaseCustomRepository {

    @EntityGraph(attributePaths = {"category"})
    List<Releases> findByCategoryId(Long categoryId);
}