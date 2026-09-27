package com.oberamsystems.ai.asgi_watch.controller;

import com.oberamsystems.ai.asgi_watch.dto.*;
import com.oberamsystems.ai.asgi_watch.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StorageApiController {

    private final StorageService storageService;

    public StorageApiController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/dates")
    public ResponseEntity<List<String>> getDates() {
        return ResponseEntity.ok(storageService.getAvailableDates());
    }

    @GetMapping("/tree")
    public ResponseEntity<List<StorageNodeDto>> getTree(@RequestParam(required = false) String date) {
        return ResponseEntity.ok(storageService.getStorageTree(date));
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryDto>> getCountries() {
        return ResponseEntity.ok(storageService.getCountries());
    }

    @GetMapping("/facilities/history")
    public ResponseEntity<List<FacilityHistorySeriesDto>> getFacilityHistory(
            @RequestParam(defaultValue = "DE") String country,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(storageService.getFacilityHistory(country, from, to));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@RequestParam(required = false) String date) {
        return ResponseEntity.ok(storageService.getOverviewSummary(date));
    }
}
