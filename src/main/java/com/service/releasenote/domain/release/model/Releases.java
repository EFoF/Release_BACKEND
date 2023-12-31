package com.service.releasenote.domain.release.model;

import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.model.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Releases extends BaseEntity {
    @Id
    @Column(name = "releases_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Enumerated(EnumType.STRING)
    private Tag tag;

    @Setter
    private String version;

    @Setter
    private LocalDateTime releaseDate;

    @Setter
    private String message;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}
