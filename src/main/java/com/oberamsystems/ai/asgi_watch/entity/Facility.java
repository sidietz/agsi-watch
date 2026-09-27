package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "facilities")
@Getter
@Setter
@NoArgsConstructor
public class Facility {

    @Id
    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_code", nullable = false)
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    @Column(name = "facility_type", length = 32)
    private String facilityType;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Facility(String code, String name, Operator operator, Country country, String facilityType) {
        this.code = code;
        this.name = name;
        this.operator = operator;
        this.country = country;
        this.facilityType = facilityType;
    }
}
