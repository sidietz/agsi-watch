package com.oberamsystems.ai.asgi_watch.mcp;

import com.oberamsystems.ai.asgi_watch.dto.CountryDto;
import com.oberamsystems.ai.asgi_watch.dto.FacilityHistorySeriesDto;
import com.oberamsystems.ai.asgi_watch.dto.StorageNodeDto;
import com.oberamsystems.ai.asgi_watch.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageMcpToolsTest {

    @Mock
    private StorageService storageService;

    private StorageMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new StorageMcpTools(storageService);
    }

    @Test
    void testGetAvailableGasDays() {
        when(storageService.getAvailableDates()).thenReturn(List.of("2026-09-27", "2026-09-26"));
        List<String> dates = tools.getAvailableGasDays();
        assertEquals(2, dates.size());
        assertEquals("2026-09-27", dates.get(0));
        verify(storageService).getAvailableDates();
    }

    @Test
    void testGetLatestGasDay() {
        when(storageService.getLatestDate()).thenReturn("2026-09-27");
        String latest = tools.getLatestGasDay();
        assertEquals("2026-09-27", latest);
        verify(storageService).getLatestDate();
    }

    @Test
    void testGetStorageOverviewSummary() {
        Map<String, Object> summary = Map.of("gasDay", "2026-09-27", "regions", List.of());
        when(storageService.getOverviewSummary("2026-09-27")).thenReturn(summary);
        Map<String, Object> result = tools.getStorageOverviewSummary("2026-09-27");
        assertEquals("2026-09-27", result.get("gasDay"));
        verify(storageService).getOverviewSummary("2026-09-27");
    }

    @Test
    void testGetMonitoredCountries() {
        CountryDto c1 = new CountryDto("DE", "Germany", 64);
        when(storageService.getCountries()).thenReturn(List.of(c1));
        List<CountryDto> countries = tools.getMonitoredCountries();
        assertEquals(1, countries.size());
        assertEquals("DE", countries.get(0).getCode());
        verify(storageService).getCountries();
    }

    @Test
    void testGetCountryStorageStatus() {
        StorageNodeDto cNode = new StorageNodeDto();
        cNode.setType("country");
        cNode.setCode("DE");
        cNode.setName("Germany");
        when(storageService.getCountryStorageStatus("DE", "2026-09-27")).thenReturn(cNode);

        StorageNodeDto result = tools.getCountryStorageStatus("DE", "2026-09-27");
        assertNotNull(result);
        assertEquals("DE", result.getCode());
        verify(storageService).getCountryStorageStatus("DE", "2026-09-27");
    }

    @Test
    void testGetFacilityHistory() {
        FacilityHistorySeriesDto series = new FacilityHistorySeriesDto("F1", "Facility 1", "Op 1", "UGS", 20.0);
        when(storageService.getFacilityHistory("DE", "2026-09-01", "2026-09-27")).thenReturn(List.of(series));

        List<FacilityHistorySeriesDto> result = tools.getFacilityHistory("DE", "2026-09-01", "2026-09-27");
        assertEquals(1, result.size());
        assertEquals("F1", result.get(0).getCode());
        verify(storageService).getFacilityHistory("DE", "2026-09-01", "2026-09-27");
    }

    @Test
    void testSearchFacilities() {
        StorageNodeDto fac = new StorageNodeDto();
        fac.setType("facility");
        fac.setCode("F1");
        fac.setName("Bierwang");
        when(storageService.searchFacilities("Bierwang", "2026-09-27")).thenReturn(List.of(fac));

        List<StorageNodeDto> result = tools.searchFacilities("Bierwang", "2026-09-27");
        assertEquals(1, result.size());
        assertEquals("Bierwang", result.get(0).getName());
        verify(storageService).searchFacilities("Bierwang", "2026-09-27");
    }
}
