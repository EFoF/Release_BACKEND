package com.service.releasenote.domain.detail.dao;

import com.service.releasenote.domain.detail.model.Detail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailRepository extends JpaRepository<Detail, Long> {
}
