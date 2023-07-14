package com.service.releasenote.domain.company.dao;

import com.service.releasenote.domain.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
