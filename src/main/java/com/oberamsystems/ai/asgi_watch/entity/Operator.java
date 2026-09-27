package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "operators")
@Getter
@Setter
@NoArgsConstructor
public class Operator {

    @Id
    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    @Column(name = "publication_link", columnDefinition = "TEXT")
    private String publicationLink;

    @Column(name = "transparency_template", columnDefinition = "TEXT")
    private String transparencyTemplate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Operator(String code, String name, Country country) {
        this.code = code;
        this.name = name;
        this.country = country;
    }
}
