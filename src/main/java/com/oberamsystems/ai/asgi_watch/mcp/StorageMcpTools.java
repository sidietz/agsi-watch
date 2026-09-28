package com.oberamsystems.ai.asgi_watch.mcp;

import com.oberamsystems.ai.asgi_watch.dto.CountryDto;
import com.oberamsystems.ai.asgi_watch.dto.FacilityHistorySeriesDto;
import com.oberamsystems.ai.asgi_watch.dto.StorageNodeDto;
import com.oberamsystems.ai.asgi_watch.service.StorageService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Model Context Protocol (MCP) tool provider for AGSI Watch gas storage inventory.
 * Exposes tools for AI agents to query European gas storage metrics, time-series,
 * regional rollups, country status, and facilities.
 */
@Component
public class StorageMcpTools {

    private final StorageService storageService;

    public StorageMcpTools(StorageService storageService) {
        this.storageService = storageService;
    }

    @McpTool(
        name = "getAvailableGasDays",
        description = "Retrieve all available gas days (dates formatted as YYYY-MM-DD) for which storage inventory data has been mined, ordered from newest to oldest."
    )
    public List<String> getAvailableGasDays() {
        return storageService.getAvailableDates();
    }

    @McpTool(
        name = "getLatestGasDay",
        description = "Retrieve the most recent gas day date available in the database (YYYY-MM-DD)."
    )
    public String getLatestGasDay() {
        return storageService.getLatestDate();
    }

    @McpTool(
        name = "getStorageOverviewSummary",
        description = "Get the high-level regional storage summary (gas in storage, working gas volume capacity, full percentage, net withdrawal, injection, withdrawal) for a specific gas day (or latest day if omitted)."
    )
    public Map<String, Object> getStorageOverviewSummary(
        @McpToolParam(description = "Target gas day in YYYY-MM-DD format (optional, defaults to latest available date)", required = false)
        String gasDay
    ) {
        return storageService.getOverviewSummary(gasDay);
    }

    @McpTool(
        name = "getMonitoredCountries",
        description = "List all monitored European countries with their ISO code, name, and total count of active gas storage facilities."
    )
    public List<CountryDto> getMonitoredCountries() {
        return storageService.getCountries();
    }

    @McpTool(
        name = "getCountryStorageStatus",
        description = "Get detailed gas storage inventory status for a specific country (e.g. 'DE', 'AT', 'FR', 'IT', 'NL') on a given gas day. Includes gas in storage, technical capacity, percent full, trend, daily injection/withdrawal, national consumption, and list of storage operators and facilities."
    )
    public StorageNodeDto getCountryStorageStatus(
        @McpToolParam(description = "Two-letter ISO country code, e.g. DE, AT, FR, IT, NL, UA", required = true)
        String countryCode,
        @McpToolParam(description = "Target gas day in YYYY-MM-DD format (optional, defaults to latest available date)", required = false)
        String gasDay
    ) {
        return storageService.getCountryStorageStatus(countryCode, gasDay);
    }

    @McpTool(
        name = "getFacilityHistory",
        description = "Get historical time-series storage data for all facilities within a specific country across an optional date range. Returns percent full, gas in storage volume (TWh), technical capacity, and status over time."
    )
    public List<FacilityHistorySeriesDto> getFacilityHistory(
        @McpToolParam(description = "Two-letter ISO country code, e.g. DE, AT, FR", required = true)
        String countryCode,
        @McpToolParam(description = "Start date inclusive in YYYY-MM-DD format (optional)", required = false)
        String fromDate,
        @McpToolParam(description = "End date inclusive in YYYY-MM-DD format (optional)", required = false)
        String toDate
    ) {
        return storageService.getFacilityHistory(countryCode, fromDate, toDate);
    }

    @McpTool(
        name = "searchFacilities",
        description = "Search for storage facilities or operators by name or storage type (e.g. 'Bierwang', 'Uniper', 'Salt Cavern', 'Aquifer', 'Depleted Field') on a given date."
    )
    public List<StorageNodeDto> searchFacilities(
        @McpToolParam(description = "Search query string for facility name, operator name, or facility type", required = true)
        String query,
        @McpToolParam(description = "Target gas day in YYYY-MM-DD format (optional, defaults to latest available date)", required = false)
        String gasDay
    ) {
        return storageService.searchFacilities(query, gasDay);
    }
}
