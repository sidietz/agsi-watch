package com.oberamsystems.ai.asgi_watch.controller;

import com.oberamsystems.ai.asgi_watch.dto.*;
import com.oberamsystems.ai.asgi_watch.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StorageApiControllerTest {

    private StorageService storageService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        storageService = Mockito.mock(StorageService.class);
        StorageApiController controller = new StorageApiController(storageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetDates() throws Exception {
        when(storageService.getAvailableDates()).thenReturn(List.of("2026-09-27", "2026-09-26"));

        mockMvc.perform(get("/api/dates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2026-09-27"))
                .andExpect(jsonPath("$[1]").value("2026-09-26"));
    }

    @Test
    void testGetTree() throws Exception {
        StorageNodeDto node = new StorageNodeDto();
        node.setId("reg_EU");
        node.setName("EU");
        when(storageService.getStorageTree(any())).thenReturn(List.of(node));

        mockMvc.perform(get("/api/tree?date=2026-09-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("reg_EU"))
                .andExpect(jsonPath("$[0].name").value("EU"));

        mockMvc.perform(get("/api/tree"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCountries() throws Exception {
        CountryDto country = new CountryDto("DE", "Germany", 64);
        when(storageService.getCountries()).thenReturn(List.of(country));

        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("DE"))
                .andExpect(jsonPath("$[0].name").value("Germany"))
                .andExpect(jsonPath("$[0].facilityCount").value(64));
    }

    @Test
    void testGetFacilityHistory() throws Exception {
        FacilityHistorySeriesDto series = new FacilityHistorySeriesDto("21W001", "UGS Jemgum", "SEFE", "DSR", 9.45);
        when(storageService.getFacilityHistory(eq("DE"), any(), any())).thenReturn(List.of(series));

        mockMvc.perform(get("/api/facilities/history?country=DE&from=2026-01-01&to=2026-09-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("21W001"))
                .andExpect(jsonPath("$[0].name").value("UGS Jemgum"));

        mockMvc.perform(get("/api/facilities/history"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSummary() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        summary.put("gasDay", "2026-09-27");
        when(storageService.getOverviewSummary(any())).thenReturn(summary);

        mockMvc.perform(get("/api/summary?date=2026-09-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gasDay").value("2026-09-27"));

        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isOk());
    }
}
