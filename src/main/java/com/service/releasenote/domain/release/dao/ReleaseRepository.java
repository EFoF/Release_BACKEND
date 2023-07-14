package com.service.releasenote.domain.release.dao;

import com.service.releasenote.domain.release.model.Releases;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseRepository extends JpaRepository<Releases, Long> {
}
