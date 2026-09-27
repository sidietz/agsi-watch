package com.oberamsystems.ai.asgi_watch.dto;

import java.util.ArrayList;
import java.util.List;

public class FacilityHistorySeriesDto {
    private String code;
    private String name;
    private String operatorName;
    private String facilityType;
    private Double workingGasVolume;
    private List<FacilityHistoryPointDto> points = new ArrayList<>();

    public FacilityHistorySeriesDto() {}

    public FacilityHistorySeriesDto(String code, String name, String operatorName, String facilityType, Double workingGasVolume) {
        this.code = code;
        this.name = name;
        this.operatorName = operatorName;
        this.facilityType = facilityType;
        this.workingGasVolume = workingGasVolume;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getFacilityType() { return facilityType; }
    public void setFacilityType(String facilityType) { this.facilityType = facilityType; }

    public Double getWorkingGasVolume() { return workingGasVolume; }
    public void setWorkingGasVolume(Double workingGasVolume) { this.workingGasVolume = workingGasVolume; }

    public List<FacilityHistoryPointDto> getPoints() { return points; }
    public void setPoints(List<FacilityHistoryPointDto> points) { this.points = points; }

    public void addPoint(FacilityHistoryPointDto point) {
        this.points.add(point);
    }
}
