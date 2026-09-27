package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "regions")
@Getter
@Setter
@NoArgsConstructor
public class Region {

    @Id
    @Column(name = "code", length = 16, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Region(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
