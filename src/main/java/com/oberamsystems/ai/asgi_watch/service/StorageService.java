package com.oberamsystems.ai.asgi_watch.service;

import com.oberamsystems.ai.asgi_watch.dto.*;
import com.oberamsystems.ai.asgi_watch.entity.*;
import com.oberamsystems.ai.asgi_watch.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class StorageService {

    private final CountryStorageDataRepository countryStorageDataRepository;
    private final CountryRepository countryRepository;
    private final FacilityRepository facilityRepository;
    private final OperatorRepository operatorRepository;
    private final RegionRepository regionRepository;
    private final RegionStorageDataRepository regionStorageDataRepository;
    private final FacilityStorageDataRepository facilityStorageDataRepository;

    public StorageService(
            CountryStorageDataRepository countryStorageDataRepository,
            CountryRepository countryRepository,
            FacilityRepository facilityRepository,
            OperatorRepository operatorRepository,
            RegionRepository regionRepository,
            RegionStorageDataRepository regionStorageDataRepository,
            FacilityStorageDataRepository facilityStorageDataRepository) {
        this.countryStorageDataRepository = countryStorageDataRepository;
        this.countryRepository = countryRepository;
        this.facilityRepository = facilityRepository;
        this.operatorRepository = operatorRepository;
        this.regionRepository = regionRepository;
        this.regionStorageDataRepository = regionStorageDataRepository;
        this.facilityStorageDataRepository = facilityStorageDataRepository;
    }

    public List<String> getAvailableDates() {
        return countryStorageDataRepository.findDistinctGasDays()
                .stream()
                .map(LocalDate::toString)
                .toList();
    }

    public String getLatestDate() {
        LocalDate latest = countryStorageDataRepository.findLatestGasDay();
        return latest != null ? latest.toString() : null;
    }

    public List<CountryDto> getCountries() {
        return countryRepository.findCountriesWithFacilityCount();
    }

    public List<StorageNodeDto> getStorageTree(String gasDay) {
        if (gasDay == null || gasDay.isBlank()) {
            gasDay = getLatestDate();
        }
        if (gasDay == null) {
            return Collections.emptyList();
        }

        LocalDate date = LocalDate.parse(gasDay);

        // 1. Fetch facilities with storage for this date
        Map<String, List<StorageNodeDto>> facilitiesByCtryAndOp = new HashMap<>();
        Map<String, Operator> operatorsByCode = new LinkedHashMap<>();
        Map<String, Set<String>> opCountries = new HashMap<>();
        Map<String, Set<String>> opCodesByCountry = new HashMap<>();

        List<Object[]> facRows = facilityRepository.findFacilitiesWithStorageForGasDay(date);
        for (Object[] row : facRows) {
            Facility f = (Facility) row[0];
            FacilityStorageData fs = (FacilityStorageData) row[1];
            StorageNodeDto node = createFacilityNode(f, fs);
            String ctryCode = f.getCountry().getCode();
            String opCode = f.getOperator().getCode();

            operatorsByCode.putIfAbsent(opCode, f.getOperator());
            opCountries.computeIfAbsent(opCode, k -> new HashSet<>()).add(ctryCode);
            opCodesByCountry.computeIfAbsent(ctryCode, k -> new LinkedHashSet<>()).add(opCode);
            facilitiesByCtryAndOp.computeIfAbsent(ctryCode + "_" + opCode, k -> new ArrayList<>()).add(node);
        }

        // 2. Fetch operators with storage for this date
        Map<String, OperatorStorageData> opStorageDataByCode = new HashMap<>();
        List<Object[]> opRows = operatorRepository.findOperatorsWithStorageForGasDay(date);
        for (Object[] row : opRows) {
            Operator o = (Operator) row[0];
            OperatorStorageData os = (OperatorStorageData) row[1];
            String opCode = o.getCode();
            operatorsByCode.putIfAbsent(opCode, o);
            if (os != null) {
                opStorageDataByCode.put(opCode, os);
            }
            if (o.getCountry() != null) {
                String homeCtry = o.getCountry().getCode();
                opCountries.computeIfAbsent(opCode, k -> new HashSet<>()).add(homeCtry);
                opCodesByCountry.computeIfAbsent(homeCtry, k -> new LinkedHashSet<>()).add(opCode);
            }
        }

        // 3. Group operators under their respective countries
        Comparator<StorageNodeDto> opComparator = Comparator
                .comparing(StorageNodeDto::getGasInStorage, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(StorageNodeDto::getName, Comparator.nullsLast(Comparator.naturalOrder()));

        Map<String, List<StorageNodeDto>> operatorsByCountry = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : opCodesByCountry.entrySet()) {
            String ctryCode = entry.getKey();
            Set<String> opCodes = entry.getValue();
            List<StorageNodeDto> opNodes = new ArrayList<>();

            for (String opCode : opCodes) {
                Operator o = operatorsByCode.get(opCode);
                if (o == null) {
                    continue;
                }

                List<StorageNodeDto> facChildren = facilitiesByCtryAndOp.getOrDefault(ctryCode + "_" + opCode, Collections.emptyList());
                boolean isHomeCountry = o.getCountry() != null && ctryCode.equals(o.getCountry().getCode());
                if (facChildren.isEmpty() && !isHomeCountry) {
                    continue;
                }

                boolean isCrossBorder = opCountries.getOrDefault(opCode, Collections.emptySet()).size() > 1;
                OperatorStorageData os = opStorageDataByCode.get(opCode);

                StorageNodeDto opNode = createCountryOperatorNode(ctryCode, o, facChildren, os, isCrossBorder);
                opNodes.add(opNode);
            }

            opNodes.sort(opComparator);
            operatorsByCountry.put(ctryCode, opNodes);
        }

        // 4. Fetch countries with storage for this date
        Map<String, List<StorageNodeDto>> countriesByRegion = new HashMap<>();
        List<Object[]> ctryRows = countryRepository.findCountriesWithStorageForGasDay(date);
        for (Object[] row : ctryRows) {
            Country c = (Country) row[0];
            CountryStorageData cs = (CountryStorageData) row[1];
            StorageNodeDto node = createCountryNode(c, cs);
            String ctryCode = c.getCode();
            List<StorageNodeDto> opChildren = operatorsByCountry.getOrDefault(ctryCode, Collections.emptyList());
            node.setChildren(opChildren);
            String regCode = c.getRegion().getCode();
            countriesByRegion.computeIfAbsent(regCode, k -> new ArrayList<>()).add(node);
        }

        // 5. Fetch regions with storage for this date
        List<StorageNodeDto> regions = new ArrayList<>();
        List<Object[]> regRows = regionRepository.findRegionsWithStorageForGasDay(date);
        for (Object[] row : regRows) {
            Region r = (Region) row[0];
            RegionStorageData rs = (RegionStorageData) row[1];
            StorageNodeDto node = createRegionNode(r, rs);
            String regCode = r.getCode();
            List<StorageNodeDto> ctryChildren = countriesByRegion.getOrDefault(regCode, Collections.emptyList());
            node.setChildren(ctryChildren);
            regions.add(node);
        }

        return regions;
    }

    public List<FacilityHistorySeriesDto> getFacilityHistory(String countryCode, String fromDate, String toDate) {
        LocalDate from = (fromDate != null && !fromDate.isBlank()) ? LocalDate.parse(fromDate) : null;
        LocalDate to = (toDate != null && !toDate.isBlank()) ? LocalDate.parse(toDate) : null;

        List<FacilityStorageData> historyList = facilityStorageDataRepository.findFacilityHistory(countryCode, from, to);

        Map<String, FacilityHistorySeriesDto> seriesMap = new LinkedHashMap<>();
        for (FacilityStorageData fs : historyList) {
            Facility f = fs.getFacility();
            Operator o = f.getOperator();
            String facCode = f.getCode();

            FacilityHistorySeriesDto series = seriesMap.computeIfAbsent(facCode, k ->
                new FacilityHistorySeriesDto(
                    facCode,
                    f.getName(),
                    o.getName(),
                    f.getFacilityType(),
                    fs.getWorkingGasVolume()
                )
            );

            series.addPoint(new FacilityHistoryPointDto(
                fs.getGasDay().toString(),
                fs.getFullPercentage(),
                fs.getGasInStorage(),
                fs.getWorkingGasVolume(),
                fs.getStatus()
            ));
        }

        return new ArrayList<>(seriesMap.values());
    }

    public Map<String, Object> getOverviewSummary(String gasDay) {
        final String targetGasDay = (gasDay == null || gasDay.isBlank()) ? getLatestDate() : gasDay;
        Map<String, Object> summary = new HashMap<>();
        summary.put("gasDay", targetGasDay);

        if (targetGasDay == null) {
            summary.put("regions", Collections.emptyList());
            return summary;
        }

        LocalDate date = LocalDate.parse(targetGasDay);
        List<RegionStorageData> regionDataList = regionStorageDataRepository.findByGasDayWithRegion(date);

        List<Map<String, Object>> regionsList = new ArrayList<>();
        for (RegionStorageData rs : regionDataList) {
            Map<String, Object> m = new HashMap<>();
            m.put("code", rs.getRegion().getCode());
            m.put("name", rs.getRegion().getName());
            m.put("gasInStorage", rs.getGasInStorage());
            m.put("workingGasVolume", rs.getWorkingGasVolume());
            m.put("fullPercentage", rs.getFullPercentage());
            m.put("injection", rs.getInjection());
            m.put("withdrawal", rs.getWithdrawal());
            m.put("netWithdrawal", rs.getNetWithdrawal());
            regionsList.add(m);
        }
        summary.put("regions", regionsList);

        return summary;
    }

    public StorageNodeDto getCountryStorageStatus(String countryCode, String gasDay) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }
        List<StorageNodeDto> tree = getStorageTree(gasDay);
        for (StorageNodeDto region : tree) {
            if (region.getChildren() != null) {
                for (StorageNodeDto country : region.getChildren()) {
                    if (countryCode.trim().equalsIgnoreCase(country.getCode())) {
                        return country;
                    }
                }
            }
        }
        return null;
    }

    public List<StorageNodeDto> searchFacilities(String query, String gasDay) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String q = query.toLowerCase().trim();
        List<StorageNodeDto> tree = getStorageTree(gasDay);
        List<StorageNodeDto> results = new ArrayList<>();
        for (StorageNodeDto region : tree) {
            if (region.getChildren() == null) continue;
            for (StorageNodeDto country : region.getChildren()) {
                if (country.getChildren() == null) continue;
                for (StorageNodeDto op : country.getChildren()) {
                    boolean opMatches = (op.getName() != null && op.getName().toLowerCase().contains(q))
                            || (op.getCode() != null && op.getCode().toLowerCase().contains(q));
                    if (op.getChildren() == null) continue;
                    for (StorageNodeDto fac : op.getChildren()) {
                        boolean facMatches = (fac.getName() != null && fac.getName().toLowerCase().contains(q))
                                || (fac.getCode() != null && fac.getCode().toLowerCase().contains(q))
                                || (fac.getFacilityType() != null && fac.getFacilityType().toLowerCase().contains(q));
                        if (opMatches || facMatches) {
                            results.add(fac);
                        }
                    }
                }
            }
        }
        return results;
    }

    private StorageNodeDto createFacilityNode(Facility f, FacilityStorageData fs) {
        StorageNodeDto node = new StorageNodeDto();
        node.setType("facility");
        node.setCode(f.getCode());
        node.setName(f.getName());
        node.setId("facility_" + f.getCode());
        node.setFacilityType(f.getFacilityType());
        if (fs != null) {
            node.setStatus(fs.getStatus());
            node.setGasInStorage(fs.getGasInStorage());
            node.setFullPercentage(fs.getFullPercentage());
            node.setTrend(fs.getTrend());
            node.setInjection(fs.getInjection());
            node.setWithdrawal(fs.getWithdrawal());
            node.setNetWithdrawal(fs.getNetWithdrawal());
            node.setWorkingGasVolume(fs.getWorkingGasVolume());
            node.setInjectionCapacity(fs.getInjectionCapacity());
            node.setWithdrawalCapacity(fs.getWithdrawalCapacity());
        }
        return node;
    }

    private StorageNodeDto createCountryOperatorNode(
            String ctryCode,
            Operator o,
            List<StorageNodeDto> facChildren,
            OperatorStorageData os,
            boolean isCrossBorder) {
        StorageNodeDto node = new StorageNodeDto();
        node.setType("operator");
        node.setCode(o.getCode());
        node.setName(o.getName());
        node.setId("operator_" + ctryCode + "_" + o.getCode());
        node.setChildren(facChildren != null ? facChildren : Collections.emptyList());

        if (!isCrossBorder && os != null) {
            node.setStatus(os.getStatus());
            node.setGasInStorage(os.getGasInStorage());
            node.setFullPercentage(os.getFullPercentage());
            node.setTrend(os.getTrend());
            node.setInjection(os.getInjection());
            node.setWithdrawal(os.getWithdrawal());
            node.setNetWithdrawal(os.getNetWithdrawal());
            node.setWorkingGasVolume(os.getWorkingGasVolume());
            node.setInjectionCapacity(os.getInjectionCapacity());
            node.setWithdrawalCapacity(os.getWithdrawalCapacity());
            node.setCoveredCapacity(os.getCoveredCapacity());
            return node;
        }

        if (facChildren != null && !facChildren.isEmpty()) {
            Double gisSum = null;
            Double wgvSum = null;
            Double injSum = null;
            Double withSum = null;
            Double netWithSum = null;
            Double injCapSum = null;
            Double withCapSum = null;
            Double trendSum = null;
            String status = null;

            for (StorageNodeDto f : facChildren) {
                if (f.getGasInStorage() != null) {
                    gisSum = (gisSum == null ? 0.0 : gisSum) + f.getGasInStorage();
                }
                if (f.getWorkingGasVolume() != null) {
                    wgvSum = (wgvSum == null ? 0.0 : wgvSum) + f.getWorkingGasVolume();
                }
                if (f.getInjection() != null) {
                    injSum = (injSum == null ? 0.0 : injSum) + f.getInjection();
                }
                if (f.getWithdrawal() != null) {
                    withSum = (withSum == null ? 0.0 : withSum) + f.getWithdrawal();
                }
                if (f.getNetWithdrawal() != null) {
                    netWithSum = (netWithSum == null ? 0.0 : netWithSum) + f.getNetWithdrawal();
                }
                if (f.getInjectionCapacity() != null) {
                    injCapSum = (injCapSum == null ? 0.0 : injCapSum) + f.getInjectionCapacity();
                }
                if (f.getWithdrawalCapacity() != null) {
                    withCapSum = (withCapSum == null ? 0.0 : withCapSum) + f.getWithdrawalCapacity();
                }
                if (f.getTrend() != null && trendSum == null && facChildren.size() == 1) {
                    trendSum = f.getTrend();
                }
                if (status == null && f.getStatus() != null) {
                    status = f.getStatus();
                }
            }

            if (gisSum != null) {
                gisSum = Math.round(gisSum * 10000.0) / 10000.0;
            }
            if (wgvSum != null) {
                wgvSum = Math.round(wgvSum * 10000.0) / 10000.0;
            }
            if (injSum != null) {
                injSum = Math.round(injSum * 100.0) / 100.0;
            }
            if (withSum != null) {
                withSum = Math.round(withSum * 100.0) / 100.0;
            }
            if (netWithSum != null) {
                netWithSum = Math.round(netWithSum * 100.0) / 100.0;
            }
            if (injCapSum != null) {
                injCapSum = Math.round(injCapSum * 100.0) / 100.0;
            }
            if (withCapSum != null) {
                withCapSum = Math.round(withCapSum * 100.0) / 100.0;
            }

            Double fullPct = null;
            if (gisSum != null && wgvSum != null && wgvSum > 0) {
                fullPct = Math.round((gisSum / wgvSum) * 10000.0) / 100.0;
            }

            Double trend = (os != null && ctryCode.equals(o.getCountry().getCode())) ? os.getTrend() : trendSum;
            String opStatus = (os != null && ctryCode.equals(o.getCountry().getCode()) && os.getStatus() != null) ? os.getStatus() : status;
            Double coveredCap = (os != null && ctryCode.equals(o.getCountry().getCode())) ? os.getCoveredCapacity() : null;

            node.setStatus(opStatus);
            node.setGasInStorage(gisSum);
            node.setFullPercentage(fullPct);
            node.setTrend(trend);
            node.setInjection(injSum);
            node.setWithdrawal(withSum);
            node.setNetWithdrawal(netWithSum);
            node.setWorkingGasVolume(wgvSum);
            node.setInjectionCapacity(injCapSum);
            node.setWithdrawalCapacity(withCapSum);
            node.setCoveredCapacity(coveredCap);
        } else if (os != null) {
            node.setStatus(os.getStatus());
            node.setGasInStorage(os.getGasInStorage());
            node.setFullPercentage(os.getFullPercentage());
            node.setTrend(os.getTrend());
            node.setInjection(os.getInjection());
            node.setWithdrawal(os.getWithdrawal());
            node.setNetWithdrawal(os.getNetWithdrawal());
            node.setWorkingGasVolume(os.getWorkingGasVolume());
            node.setInjectionCapacity(os.getInjectionCapacity());
            node.setWithdrawalCapacity(os.getWithdrawalCapacity());
            node.setCoveredCapacity(os.getCoveredCapacity());
        }

        return node;
    }

    private StorageNodeDto createCountryNode(Country c, CountryStorageData cs) {
        StorageNodeDto node = new StorageNodeDto();
        node.setType("country");
        node.setCode(c.getCode());
        node.setName(c.getName());
        node.setId("country_" + c.getCode());
        if (cs != null) {
            node.setStatus(cs.getStatus());
            node.setGasInStorage(cs.getGasInStorage());
            node.setFullPercentage(cs.getFullPercentage());
            node.setTrend(cs.getTrend());
            node.setInjection(cs.getInjection());
            node.setWithdrawal(cs.getWithdrawal());
            node.setNetWithdrawal(cs.getNetWithdrawal());
            node.setWorkingGasVolume(cs.getWorkingGasVolume());
            node.setInjectionCapacity(cs.getInjectionCapacity());
            node.setWithdrawalCapacity(cs.getWithdrawalCapacity());
            node.setConsumption(cs.getConsumption());
            node.setConsumptionFull(cs.getConsumptionFull());
            node.setCoveredCapacity(cs.getCoveredCapacity());
        }
        return node;
    }

    private StorageNodeDto createRegionNode(Region r, RegionStorageData rs) {
        StorageNodeDto node = new StorageNodeDto();
        node.setType("region");
        node.setCode(r.getCode());
        node.setName(r.getName());
        node.setId("region_" + r.getCode());
        if (rs != null) {
            node.setStatus(rs.getStatus());
            node.setGasInStorage(rs.getGasInStorage());
            node.setFullPercentage(rs.getFullPercentage());
            node.setTrend(rs.getTrend());
            node.setInjection(rs.getInjection());
            node.setWithdrawal(rs.getWithdrawal());
            node.setNetWithdrawal(rs.getNetWithdrawal());
            node.setWorkingGasVolume(rs.getWorkingGasVolume());
            node.setInjectionCapacity(rs.getInjectionCapacity());
            node.setWithdrawalCapacity(rs.getWithdrawalCapacity());
            node.setCoveredCapacity(rs.getCoveredCapacity());
        }
        return node;
    }
}
