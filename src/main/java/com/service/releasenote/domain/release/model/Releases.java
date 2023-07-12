package com.service.releasenote.domain.release.model;

import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Releases extends BaseEntity {
    @Id
    @Column(name = "releases_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Tag tag;

    private String version;

    private LocalDateTime release_date;

    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}
