package com.service.releasenote.domain.release.dao;

import com.service.releasenote.domain.release.model.Releases;

import java.util.Optional;

public interface ReleaseCustomRepository {

    Optional<Releases> findByCategoryIdAndReleaseId(Long projectId, Long categoryId);
}
