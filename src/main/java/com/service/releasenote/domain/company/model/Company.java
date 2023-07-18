package com.service.releasenote.domain.company.model;

import com.service.releasenote.domain.model.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
public class Company extends BaseTimeEntity {

    @Id
    @Column(name = "company_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String ImageURL;

    @Builder
    public Company(Long id, String name, String ImageURL) {
        this.id = id;
        this.name = name;
        this.ImageURL = ImageURL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
