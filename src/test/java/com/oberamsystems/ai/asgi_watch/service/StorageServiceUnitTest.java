package com.oberamsystems.ai.asgi_watch.service;

import com.oberamsystems.ai.asgi_watch.dto.StorageNodeDto;
import com.oberamsystems.ai.asgi_watch.entity.*;
import com.oberamsystems.ai.asgi_watch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceUnitTest {

    @Mock
    private CountryStorageDataRepository countryStorageDataRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private FacilityRepository facilityRepository;
    @Mock
    private OperatorRepository operatorRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private RegionStorageDataRepository regionStorageDataRepository;
    @Mock
    private FacilityStorageDataRepository facilityStorageDataRepository;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(
                countryStorageDataRepository,
                countryRepository,
                facilityRepository,
                operatorRepository,
                regionRepository,
                regionStorageDataRepository,
                facilityStorageDataRepository
        );
    }

    @Test
    void testGetStorageTreeWhenLatestDateIsNull() {
        when(countryStorageDataRepository.findLatestGasDay()).thenReturn(null);
        List<StorageNodeDto> result = storageService.getStorageTree(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOverviewSummaryWhenTargetGasDayIsNull() {
        when(countryStorageDataRepository.findLatestGasDay()).thenReturn(null);
        Map<String, Object> summary = storageService.getOverviewSummary(null);
        assertNotNull(summary);
        assertNull(summary.get("gasDay"));
        assertEquals(Collections.emptyList(), summary.get("regions"));
    }

    @Test
    void testGetStorageTreeWithNullStorageData() {
        LocalDate date = LocalDate.of(2026, 9, 27);
        Region r = new Region("EU", "European Union");
        Country c = new Country("DE", "Germany", r);
        Operator o = new Operator("OP1", "Operator 1", c);
        Facility f = new Facility("F1", "Facility 1", o, c, "UGS");

        // Null storage data objects (fs, os, cs, rs are null)
        when(facilityRepository.findFacilitiesWithStorageForGasDay(date))
                .thenReturn(List.<Object[]>of(new Object[]{f, null}));
        when(operatorRepository.findOperatorsWithStorageForGasDay(date))
                .thenReturn(List.<Object[]>of(new Object[]{o, null}));
        when(countryRepository.findCountriesWithStorageForGasDay(date))
                .thenReturn(List.<Object[]>of(new Object[]{c, null}));
        when(regionRepository.findRegionsWithStorageForGasDay(date))
                .thenReturn(List.<Object[]>of(new Object[]{r, null}));

        List<StorageNodeDto> tree = storageService.getStorageTree("2026-09-27");
        assertNotNull(tree);
        assertEquals(1, tree.size());
        StorageNodeDto regNode = tree.get(0);
        assertEquals("region", regNode.getType());
        assertNull(regNode.getGasInStorage());
        assertEquals(1, regNode.getChildren().size());

        StorageNodeDto ctryNode = regNode.getChildren().get(0);
        assertEquals("country", ctryNode.getType());
        assertNull(ctryNode.getGasInStorage());
        assertEquals(1, ctryNode.getChildren().size());

        StorageNodeDto opNode = ctryNode.getChildren().get(0);
        assertEquals("operator", opNode.getType());
        assertNull(opNode.getGasInStorage());
        assertEquals(1, opNode.getChildren().size());

        StorageNodeDto facNode = opNode.getChildren().get(0);
        assertEquals("facility", facNode.getType());
        assertNull(facNode.getGasInStorage());
    }

    @Test
    void testCrossBorderOperatorsRollupCalculation() {
        LocalDate date = LocalDate.of(2026, 9, 27);
        Region r = new Region("EU", "European Union");
        Country ctryDE = new Country("DE", "Germany", r);
        Country ctryAT = new Country("AT", "Austria", r);

        Operator opCross = new Operator("OP_CROSS", "Cross Border Operator", ctryDE);
        Facility fDe = new Facility("F_DE", "Facility DE", opCross, ctryDE, "UGS");
        Facility fAt = new Facility("F_AT", "Facility AT", opCross, ctryAT, "UGS");

        FacilityStorageData fsDe = FacilityStorageData.builder()
                .gasInStorage(10.0)
                .workingGasVolume(20.0)
                .injection(1.0)
                .withdrawal(0.5)
                .netWithdrawal(-0.5)
                .injectionCapacity(5.0)
                .withdrawalCapacity(4.0)
                .trend(0.1)
                .status("C")
                .build();

        FacilityStorageData fsAt = FacilityStorageData.builder()
                .gasInStorage(5.0)
                .workingGasVolume(10.0)
                .injection(0.2)
                .withdrawal(0.1)
                .netWithdrawal(-0.1)
                .injectionCapacity(2.0)
                .withdrawalCapacity(1.5)
                .trend(0.05)
                .status("E")
                .build();

        OperatorStorageData osCross = OperatorStorageData.builder()
                .gasInStorage(10.0)
                .workingGasVolume(20.0)
                .trend(0.1)
                .status("C")
                .build();

        CountryStorageData csDe = CountryStorageData.builder().gasInStorage(10.0).build();
        CountryStorageData csAt = CountryStorageData.builder().gasInStorage(5.0).build();
        RegionStorageData rsEu = RegionStorageData.builder().gasInStorage(15.0).build();

        when(facilityRepository.findFacilitiesWithStorageForGasDay(date))
                .thenReturn(List.<Object[]>of(
                        new Object[]{fDe, fsDe},
                        new Object[]{fAt, fsAt}
                ));
        when(operatorRepository.findOperatorsWithStorageForGasDay(date))
                .thenReturn(List.<Object[]>of(
                        new Object[]{opCross, osCross}
                ));
        when(countryRepository.findCountriesWithStorageForGasDay(date))
                .thenReturn(List.<Object[]>of(
                        new Object[]{ctryDE, csDe},
                        new Object[]{ctryAT, csAt}
                ));
        when(regionRepository.findRegionsWithStorageForGasDay(date))
                .thenReturn(List.<Object[]>of(
                        new Object[]{r, rsEu}
                ));

        List<StorageNodeDto> tree = storageService.getStorageTree("2026-09-27");
        assertNotNull(tree);
        assertEquals(1, tree.size());

        StorageNodeDto regNode = tree.get(0);
        assertEquals(2, regNode.getChildren().size());

        StorageNodeDto deNode = regNode.getChildren().stream()
                .filter(c -> "DE".equals(c.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals(1, deNode.getChildren().size());
        StorageNodeDto deOp = deNode.getChildren().get(0);
        assertEquals("operator_DE_OP_CROSS", deOp.getId());
        assertEquals(10.0, deOp.getGasInStorage());
        assertEquals(20.0, deOp.getWorkingGasVolume());
        assertEquals(50.0, deOp.getFullPercentage());
        assertEquals(1, deOp.getChildren().size());
        assertEquals("F_DE", deOp.getChildren().get(0).getCode());

        StorageNodeDto atNode = regNode.getChildren().stream()
                .filter(c -> "AT".equals(c.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals(1, atNode.getChildren().size());
        StorageNodeDto atOp = atNode.getChildren().get(0);
        assertEquals("operator_AT_OP_CROSS", atOp.getId());
        assertEquals(5.0, atOp.getGasInStorage());
        assertEquals(10.0, atOp.getWorkingGasVolume());
        assertEquals(50.0, atOp.getFullPercentage());
        assertEquals(1, atOp.getChildren().size());
        assertEquals("F_AT", atOp.getChildren().get(0).getCode());
    }
}
