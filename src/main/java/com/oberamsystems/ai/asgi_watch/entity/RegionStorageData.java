package com.oberamsystems.ai.asgi_watch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "region_storage_data")
@Getter
@Setter
@NoArgsConstructor
public class RegionStorageData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", nullable = false)
    private Region region;

    @Column(name = "gas_day", nullable = false)
    private LocalDate gasDay;

    @Column(name = "gas_day_start")
    private LocalDate gasDayStart;

    @Column(name = "gas_day_end")
    private LocalDate gasDayEnd;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "gas_in_storage")
    private Double gasInStorage;

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

    @Column(name = "working_gas_volume")
    private Double workingGasVolume;

    @Column(name = "injection_capacity")
    private Double injectionCapacity;

    @Column(name = "withdrawal_capacity")
    private Double withdrawalCapacity;

    @Column(name = "covered_capacity")
    private Double coveredCapacity;

    @Column(name = "updated_at_source", length = 64)
    private String updatedAtSource;

    @Column(name = "scraped_at")
    private LocalDateTime scrapedAt;
}
