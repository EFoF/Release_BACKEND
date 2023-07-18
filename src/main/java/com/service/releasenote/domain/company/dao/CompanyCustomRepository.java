package com.service.releasenote.domain.company.dao;

import com.service.releasenote.domain.company.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyCustomRepository {
    Page<Company> findCompaniesByName(String name, Pageable pageable);
}
