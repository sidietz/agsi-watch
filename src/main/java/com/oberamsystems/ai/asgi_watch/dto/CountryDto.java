package com.oberamsystems.ai.asgi_watch.dto;

public class CountryDto {
    private String code;
    private String name;
    private int facilityCount;

    public CountryDto() {}

    public CountryDto(String code, String name, int facilityCount) {
        this.code = code;
        this.name = name;
        this.facilityCount = facilityCount;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getFacilityCount() { return facilityCount; }
    public void setFacilityCount(int facilityCount) { this.facilityCount = facilityCount; }
}
