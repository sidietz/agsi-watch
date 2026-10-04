package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Country {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "code", length = 16, nullable = false)
    private String code;

    @ToString.Include
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", nullable = false)
    private Region region;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Country(String code, String name, Region region) {
        this(code, name, region, null);
    }
}
