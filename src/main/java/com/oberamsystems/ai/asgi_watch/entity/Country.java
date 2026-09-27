package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
public class Country {

    @Id
    @Column(name = "code", length = 16, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", nullable = false)
    private Region region;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Country(String code, String name, Region region) {
        this.code = code;
        this.name = name;
        this.region = region;
    }
}
