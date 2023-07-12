package com.service.releasenote.domain.detail.model;

import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.model.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Detail extends BaseTimeEntity {

    @Id
    @Column(name = "detail_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String markdown;

    @OneToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
