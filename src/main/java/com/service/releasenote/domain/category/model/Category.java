package com.service.releasenote.domain.category.model;

import com.service.releasenote.domain.detail.model.Detail;
import com.service.releasenote.domain.model.BaseEntity;
import com.service.releasenote.domain.project.model.Project;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {
    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToOne(mappedBy = "category", fetch = FetchType.LAZY)
    private Detail detail;
}
