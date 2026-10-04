package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Operator {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @ToString.Include
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
        this(code, name, country, null, null, null);
    }
}
