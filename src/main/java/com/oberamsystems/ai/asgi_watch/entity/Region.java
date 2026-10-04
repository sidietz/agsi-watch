package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "regions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Region {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "code", length = 16, nullable = false)
    private String code;

    @ToString.Include
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Region(String code, String name) {
        this(code, name, null);
    }
}
