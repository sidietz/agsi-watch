package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "country_storage_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CountryStorageData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    @ToString.Include
    @Column(name = "gas_day", nullable = false)
    private LocalDate gasDay;

    @Column(name = "gas_day_start")
    private LocalDate gasDayStart;

    @Column(name = "gas_day_end")
    private LocalDate gasDayEnd;

    @Column(name = "status", length = 1)
    private String status;

    @ToString.Include
    @Column(name = "gas_in_storage")
    private Double gasInStorage;

    @ToString.Include
    @Column(name = "full_percentage")
    private Double fullPercentage;

    @Column(name = "trend")
    private Double trend;

    @Column(name = "injection")
    private Double injection;

    @Column(name = "withdrawal")
    private Double withdrawal;

    @Column(name = "net_withdrawal")
    private Double netWithdrawal;

    @ToString.Include
    @Column(name = "working_gas_volume")
    private Double workingGasVolume;

    @Column(name = "injection_capacity")
    private Double injectionCapacity;

    @Column(name = "withdrawal_capacity")
    private Double withdrawalCapacity;

    @Column(name = "contracted_capacity")
    private Double contractedCapacity;

    @Column(name = "available_capacity")
    private Double availableCapacity;

    @Column(name = "consumption")
    private Double consumption;

    @Column(name = "consumption_full")
    private Double consumptionFull;

    @Column(name = "covered_capacity")
    private Double coveredCapacity;

    @Column(name = "updated_at_source", length = 64)
    private String updatedAtSource;

    @Column(name = "scraped_at")
    private LocalDateTime scrapedAt;
}
