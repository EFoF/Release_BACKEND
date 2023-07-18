package com.service.releasenote.domain.company.dao;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.releasenote.domain.company.model.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.service.releasenote.domain.company.model.QCompany.company;

@RequiredArgsConstructor
@Slf4j
public class CompanyRepositoryImpl implements CompanyCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Company> findCompaniesByName(String name, Pageable pageable) {
        List<Company> results = findCompaniesByNameQuery(name, pageable);

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(company.count())
                .from(company)
                .where(company.name.contains(name));  // %name%

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);

    }

    public List<Company> findCompaniesByNameQuery(String name, Pageable pageable) {
        List<Company> results = jpaQueryFactory
                .selectFrom(company)
                .where(company.name.contains(name))  // %name%
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return results;

    }

}
