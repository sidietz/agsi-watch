package com.oberamsystems.ai.asgi_watch.dto;

public class FacilityHistoryPointDto {
    private String date;
    private Double fullPercentage;
    private Double gasInStorage;
    private Double workingGasVolume;
    private String status;

    public FacilityHistoryPointDto() {}

    public FacilityHistoryPointDto(String date, Double fullPercentage, Double gasInStorage, Double workingGasVolume, String status) {
        this.date = date;
        this.fullPercentage = fullPercentage;
        this.gasInStorage = gasInStorage;
        this.workingGasVolume = workingGasVolume;
        this.status = status;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Double getFullPercentage() { return fullPercentage; }
    public void setFullPercentage(Double fullPercentage) { this.fullPercentage = fullPercentage; }

    public Double getGasInStorage() { return gasInStorage; }
    public void setGasInStorage(Double gasInStorage) { this.gasInStorage = gasInStorage; }

    public Double getWorkingGasVolume() { return workingGasVolume; }
    public void setWorkingGasVolume(Double workingGasVolume) { this.workingGasVolume = workingGasVolume; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
