package com.oberamsystems.ai.asgi_watch.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void testCountryDto() {
        CountryDto dto = new CountryDto();
        dto.setCode("DE");
        dto.setName("Germany");
        dto.setFacilityCount(64);

        assertEquals("DE", dto.getCode());
        assertEquals("Germany", dto.getName());
        assertEquals(64, dto.getFacilityCount());

        CountryDto dto2 = new CountryDto("FR", "France", 8);
        assertEquals("FR", dto2.getCode());
        assertEquals("France", dto2.getName());
        assertEquals(8, dto2.getFacilityCount());
    }

    @Test
    void testFacilityHistoryPointDto() {
        FacilityHistoryPointDto point = new FacilityHistoryPointDto();
        point.setDate("2026-09-26");
        point.setFullPercentage(85.5);
        point.setGasInStorage(12.34);
        point.setWorkingGasVolume(15.0);
        point.setStatus("C");

        assertEquals("2026-09-26", point.getDate());
        assertEquals(85.5, point.getFullPercentage());
        assertEquals(12.34, point.getGasInStorage());
        assertEquals(15.0, point.getWorkingGasVolume());
        assertEquals("C", point.getStatus());

        FacilityHistoryPointDto point2 = new FacilityHistoryPointDto("2026-09-25", 80.0, 10.0, 12.5, "E");
        assertEquals("2026-09-25", point2.getDate());
        assertEquals(80.0, point2.getFullPercentage());
        assertEquals(10.0, point2.getGasInStorage());
        assertEquals(12.5, point2.getWorkingGasVolume());
        assertEquals("E", point2.getStatus());
    }

    @Test
    void testFacilityHistorySeriesDto() {
        FacilityHistorySeriesDto series = new FacilityHistorySeriesDto();
        series.setCode("21W0001");
        series.setName("UGS Test");
        series.setOperatorName("Operator A");
        series.setFacilityType("ASF");
        series.setWorkingGasVolume(20.0);

        List<FacilityHistoryPointDto> points = new ArrayList<>();
        points.add(new FacilityHistoryPointDto("2026-09-26", 50.0, 10.0, 20.0, "C"));
        series.setPoints(points);

        FacilityHistoryPointDto extraPoint = new FacilityHistoryPointDto("2026-09-27", 55.0, 11.0, 20.0, "C");
        series.addPoint(extraPoint);

        assertEquals("21W0001", series.getCode());
        assertEquals("UGS Test", series.getName());
        assertEquals("Operator A", series.getOperatorName());
        assertEquals("ASF", series.getFacilityType());
        assertEquals(20.0, series.getWorkingGasVolume());
        assertEquals(2, series.getPoints().size());

        FacilityHistorySeriesDto series2 = new FacilityHistorySeriesDto("21W0002", "UGS Test 2", "Operator B", "DSR", 30.0);
        assertEquals("21W0002", series2.getCode());
        assertEquals("UGS Test 2", series2.getName());
        assertEquals("Operator B", series2.getOperatorName());
        assertEquals("DSR", series2.getFacilityType());
        assertEquals(30.0, series2.getWorkingGasVolume());
    }

    @Test
    void testStorageNodeDto() {
        StorageNodeDto node = new StorageNodeDto();
        node.setId("reg_EU");
        node.setName("EU");
        node.setType("region");
        node.setCode("EU");
        node.setStatus("C");
        node.setGasInStorage(800.0);
        node.setFullPercentage(70.0);
        node.setTrend(0.5);
        node.setInjection(100.0);
        node.setWithdrawal(20.0);
        node.setNetWithdrawal(-80.0);
        node.setWorkingGasVolume(1100.0);
        node.setInjectionCapacity(300.0);
        node.setWithdrawalCapacity(250.0);
        node.setConsumption(3000.0);
        node.setConsumptionFull(25.0);
        node.setCoveredCapacity(99.0);
        node.setFacilityType("DSR");

        StorageNodeDto child = new StorageNodeDto();
        child.setId("ctry_DE");
        child.setName("Germany");
        node.addChild(child);

        assertEquals("reg_EU", node.getId());
        assertEquals("EU", node.getName());
        assertEquals("region", node.getType());
        assertEquals("EU", node.getCode());
        assertEquals("C", node.getStatus());
        assertEquals(800.0, node.getGasInStorage());
        assertEquals(70.0, node.getFullPercentage());
        assertEquals(0.5, node.getTrend());
        assertEquals(100.0, node.getInjection());
        assertEquals(20.0, node.getWithdrawal());
        assertEquals(-80.0, node.getNetWithdrawal());
        assertEquals(1100.0, node.getWorkingGasVolume());
        assertEquals(300.0, node.getInjectionCapacity());
        assertEquals(250.0, node.getWithdrawalCapacity());
        assertEquals(3000.0, node.getConsumption());
        assertEquals(25.0, node.getConsumptionFull());
        assertEquals(99.0, node.getCoveredCapacity());
        assertEquals("DSR", node.getFacilityType());
        assertEquals(1, node.getChildren().size());
        assertEquals("ctry_DE", node.getChildren().get(0).getId());

        List<StorageNodeDto> newChildren = new ArrayList<>();
        node.setChildren(newChildren);
        assertEquals(0, node.getChildren().size());
    }
}
