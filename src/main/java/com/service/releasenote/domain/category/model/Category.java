package com.service.releasenote.domain.category.model;

import com.service.releasenote.domain.detail.model.Detail;
import com.service.releasenote.domain.model.BaseEntity;
import com.service.releasenote.domain.project.model.Project;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter
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

    @ManyToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToOne(fetch = LAZY, mappedBy = "category", cascade = CascadeType.ALL)
    private Detail detail;
}
