package com.oberamsystems.ai.asgi_watch.dto;

import java.util.ArrayList;
import java.util.List;

public class StorageNodeDto {
    private String id;
    private String name;
    private String type; // "region", "country", "operator", "facility"
    private String code;
    private String status;
    private Double gasInStorage;
    private Double fullPercentage;
    private Double trend;
    private Double injection;
    private Double withdrawal;
    private Double netWithdrawal;
    private Double workingGasVolume;
    private Double injectionCapacity;
    private Double withdrawalCapacity;
    private Double consumption;
    private Double consumptionFull;
    private Double coveredCapacity;
    private String facilityType;
    private List<StorageNodeDto> children = new ArrayList<>();

    public StorageNodeDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getGasInStorage() { return gasInStorage; }
    public void setGasInStorage(Double gasInStorage) { this.gasInStorage = gasInStorage; }

    public Double getFullPercentage() { return fullPercentage; }
    public void setFullPercentage(Double fullPercentage) { this.fullPercentage = fullPercentage; }

    public Double getTrend() { return trend; }
    public void setTrend(Double trend) { this.trend = trend; }

    public Double getInjection() { return injection; }
    public void setInjection(Double injection) { this.injection = injection; }

    public Double getWithdrawal() { return withdrawal; }
    public void setWithdrawal(Double withdrawal) { this.withdrawal = withdrawal; }

    public Double getNetWithdrawal() { return netWithdrawal; }
    public void setNetWithdrawal(Double netWithdrawal) { this.netWithdrawal = netWithdrawal; }

    public Double getWorkingGasVolume() { return workingGasVolume; }
    public void setWorkingGasVolume(Double workingGasVolume) { this.workingGasVolume = workingGasVolume; }

    public Double getInjectionCapacity() { return injectionCapacity; }
    public void setInjectionCapacity(Double injectionCapacity) { this.injectionCapacity = injectionCapacity; }

    public Double getWithdrawalCapacity() { return withdrawalCapacity; }
    public void setWithdrawalCapacity(Double withdrawalCapacity) { this.withdrawalCapacity = withdrawalCapacity; }

    public Double getConsumption() { return consumption; }
    public void setConsumption(Double consumption) { this.consumption = consumption; }

    public Double getConsumptionFull() { return consumptionFull; }
    public void setConsumptionFull(Double consumptionFull) { this.consumptionFull = consumptionFull; }

    public Double getCoveredCapacity() { return coveredCapacity; }
    public void setCoveredCapacity(Double coveredCapacity) { this.coveredCapacity = coveredCapacity; }

    public String getFacilityType() { return facilityType; }
    public void setFacilityType(String facilityType) { this.facilityType = facilityType; }

    public List<StorageNodeDto> getChildren() { return children; }
    public void setChildren(List<StorageNodeDto> children) { this.children = children; }

    public void addChild(StorageNodeDto child) {
        this.children.add(child);
    }
}
