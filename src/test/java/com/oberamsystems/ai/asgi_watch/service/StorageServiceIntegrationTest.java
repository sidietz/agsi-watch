package com.oberamsystems.ai.asgi_watch.service;

import com.oberamsystems.ai.asgi_watch.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StorageServiceIntegrationTest {

    @Autowired
    private StorageService storageService;

    @Test
    void testGetAvailableDates() {
        List<String> dates = storageService.getAvailableDates();
        assertNotNull(dates);
        assertFalse(dates.isEmpty());
    }

    @Test
    void testGetLatestDate() {
        String latest = storageService.getLatestDate();
        assertNotNull(latest);
        assertFalse(latest.isBlank());
    }

    @Test
    void testGetCountries() {
        List<CountryDto> countries = storageService.getCountries();
        assertNotNull(countries);
        assertFalse(countries.isEmpty());
        boolean hasDE = countries.stream().anyMatch(c -> "DE".equals(c.getCode()));
        assertTrue(hasDE);
    }

    @Test
    void testGetStorageTreeWithDate() {
        String latest = storageService.getLatestDate();
        List<StorageNodeDto> tree = storageService.getStorageTree(latest);
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
        boolean hasEU = tree.stream().anyMatch(r -> "EU".equals(r.getCode()));
        assertTrue(hasEU);
    }

    @Test
    void testGetStorageTreeWithNullAndBlankDate() {
        List<StorageNodeDto> tree1 = storageService.getStorageTree(null);
        assertNotNull(tree1);
        assertFalse(tree1.isEmpty());

        List<StorageNodeDto> tree2 = storageService.getStorageTree("   ");
        assertNotNull(tree2);
        assertFalse(tree2.isEmpty());
    }

    @Test
    void testGetFacilityHistory() {
        List<FacilityHistorySeriesDto> series = storageService.getFacilityHistory("DE", null, null);
        assertNotNull(series);
        assertFalse(series.isEmpty());
        assertTrue(series.get(0).getPoints().size() > 0);
    }

    @Test
    void testGetFacilityHistoryWithDates() {
        List<FacilityHistorySeriesDto> series = storageService.getFacilityHistory("DE", "2026-09-01", "2026-09-27");
        assertNotNull(series);
        assertFalse(series.isEmpty());

        List<FacilityHistorySeriesDto> seriesFromOnly = storageService.getFacilityHistory("DE", "2026-09-01", null);
        assertNotNull(seriesFromOnly);

        List<FacilityHistorySeriesDto> seriesToOnly = storageService.getFacilityHistory("DE", null, "2026-09-27");
        assertNotNull(seriesToOnly);
    }

    @Test
    void testGetOverviewSummary() {
        Map<String, Object> summary = storageService.getOverviewSummary(null);
        assertNotNull(summary);
        assertTrue(summary.containsKey("regions"));
    }

    @Test
    void testGetOverviewSummaryWithDate() {
        String latest = storageService.getLatestDate();
        Map<String, Object> summary = storageService.getOverviewSummary(latest);
        assertNotNull(summary);
        assertEquals(latest, summary.get("gasDay"));

        Map<String, Object> summaryBlank = storageService.getOverviewSummary("  ");
        assertNotNull(summaryBlank);
    }
}
