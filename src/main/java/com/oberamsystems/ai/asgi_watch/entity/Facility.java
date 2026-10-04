package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "facilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Facility {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @ToString.Include
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_code", nullable = false)
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    @ToString.Include
    @Column(name = "facility_type", length = 32)
    private String facilityType;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Facility(String code, String name, Operator operator, Country country, String facilityType) {
        this(code, name, operator, country, facilityType, null, null, null);
    }
}
