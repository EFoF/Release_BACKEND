package com.service.releasenote.domain.release.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.releasenote.domain.category.model.QCategory;
import com.service.releasenote.domain.release.model.QReleases;
import com.service.releasenote.domain.release.model.Releases;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.service.releasenote.domain.category.model.QCategory.*;
import static com.service.releasenote.domain.release.model.QReleases.*;

@RequiredArgsConstructor
public class ReleaseRepositoryImpl implements ReleaseCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public Optional<Releases> findByCategoryIdAndReleaseId(Long categoryId, Long releaseId) {
        Releases result = jpaQueryFactory
                .selectFrom(releases)
                .innerJoin(releases.category, category)
                // .fetchJoin() -> 지연 로딩 객체에 접근할 일이 없다. 따라서 필요가 없음
                .where(
                        releases.category.id.eq(categoryId),
                        releases.id.eq(releaseId)
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
