package com.service.releasenote.domain.project.dao;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.releasenote.domain.member.model.QMemberProject;
import com.service.releasenote.domain.project.dto.QProjectDto_ProjectPaginationDtoEach;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.project.model.QProject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.service.releasenote.domain.member.model.QMemberProject.*;
import static com.service.releasenote.domain.project.dto.ProjectDto.*;
import static com.service.releasenote.domain.project.model.QProject.*;

@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<ProjectPaginationDtoEach> paginationTest(Long memberId, Pageable pageable) {
        List<ProjectPaginationDtoEach> fetch = jpaQueryFactory
                .select(new QProjectDto_ProjectPaginationDtoEach(
                        memberProject.project.title,
                        memberProject.project.description,
                        memberProject.project.id,
                        memberProject.project.company.id,
                        memberProject.project.company.ImageURL,
                        memberProject.project.company.name
                ))
                .from(memberProject)
//                .leftJoin(memberProject.project, project)
                .join(memberProject.project, project)
                .where(memberProject.member.id.eq(memberId))
                .orderBy(project.company.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> count = jpaQueryFactory
                .select(project.count())
                .from(memberProject)
                .leftJoin(memberProject.project, project)
                .where(memberProject.member.id.eq(memberId));

        return PageableExecutionUtils.getPage(fetch, pageable, count::fetchOne);
    }
}
