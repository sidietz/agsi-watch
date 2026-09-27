package com.oberamsystems.ai.asgi_watch.service;

import com.oberamsystems.ai.asgi_watch.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Service
public class StorageService {

    private final JdbcTemplate jdbcTemplate;

    public StorageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getAvailableDates() {
        String sql = "SELECT DISTINCT gas_day FROM country_storage_data ORDER BY gas_day DESC;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("gas_day"));
    }

    public String getLatestDate() {
        String sql = "SELECT MAX(gas_day) FROM country_storage_data;";
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    public List<CountryDto> getCountries() {
        String sql = """
            SELECT c.code, c.name, COUNT(DISTINCT f.code) AS facility_count
            FROM countries c
            JOIN facilities f ON c.code = f.country_code
            GROUP BY c.code, c.name
            ORDER BY facility_count DESC, c.name ASC;
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new CountryDto(rs.getString("code"), rs.getString("name"), rs.getInt("facility_count"))
        );
    }

    public List<StorageNodeDto> getStorageTree(String gasDay) {
        if (gasDay == null || gasDay.isBlank()) {
            gasDay = getLatestDate();
        }
        if (gasDay == null) {
            return Collections.emptyList();
        }

        LocalDate date = LocalDate.parse(gasDay);

        // 1. Fetch all facilities for this date
        String facSql = """
            SELECT f.code, f.name, f.operator_code, f.country_code, f.facility_type,
                   fs.status, fs.gas_in_storage, fs.full_percentage, fs.trend,
                   fs.injection, fs.withdrawal, fs.net_withdrawal, fs.working_gas_volume,
                   fs.injection_capacity, fs.withdrawal_capacity
            FROM facilities f
            LEFT JOIN facility_storage_data fs ON f.code = fs.facility_code AND fs.gas_day = ?
            ORDER BY fs.gas_in_storage DESC NULLS LAST, f.name ASC;
        """;
        Map<String, List<StorageNodeDto>> facilitiesByOp = new HashMap<>();
        jdbcTemplate.query(facSql, ps -> ps.setObject(1, date), rs -> {
            StorageNodeDto node = mapRowToNode(rs, "facility");
            node.setFacilityType(rs.getString("facility_type"));
            String opCode = rs.getString("operator_code");
            facilitiesByOp.computeIfAbsent(opCode, k -> new ArrayList<>()).add(node);
        });

        // 2. Fetch all operators for this date
        String opSql = """
            SELECT o.code, o.name, o.country_code,
                   os.status, os.gas_in_storage, os.full_percentage, os.trend,
                   os.injection, os.withdrawal, os.net_withdrawal, os.working_gas_volume,
                   os.injection_capacity, os.withdrawal_capacity, os.covered_capacity
            FROM operators o
            LEFT JOIN operator_storage_data os ON o.code = os.operator_code AND os.gas_day = ?
            ORDER BY os.gas_in_storage DESC NULLS LAST, o.name ASC;
        """;
        Map<String, List<StorageNodeDto>> operatorsByCountry = new HashMap<>();
        jdbcTemplate.query(opSql, ps -> ps.setObject(1, date), rs -> {
            StorageNodeDto node = mapRowToNode(rs, "operator");
            node.setCoveredCapacity(getDouble(rs, "covered_capacity"));
            String ctryCode = rs.getString("country_code");
            String opCode = rs.getString("code");
            List<StorageNodeDto> facChildren = facilitiesByOp.getOrDefault(opCode, Collections.emptyList());
            node.setChildren(facChildren);
            operatorsByCountry.computeIfAbsent(ctryCode, k -> new ArrayList<>()).add(node);
        });

        // 3. Fetch all countries for this date
        String ctrySql = """
            SELECT c.code, c.name, c.region_code,
                   cs.status, cs.gas_in_storage, cs.full_percentage, cs.trend,
                   cs.injection, cs.withdrawal, cs.net_withdrawal, cs.working_gas_volume,
                   cs.injection_capacity, cs.withdrawal_capacity, cs.consumption,
                   cs.consumption_full, cs.covered_capacity
            FROM countries c
            LEFT JOIN country_storage_data cs ON c.code = cs.country_code AND cs.gas_day = ?
            ORDER BY cs.gas_in_storage DESC NULLS LAST, c.name ASC;
        """;
        Map<String, List<StorageNodeDto>> countriesByRegion = new HashMap<>();
        jdbcTemplate.query(ctrySql, ps -> ps.setObject(1, date), rs -> {
            StorageNodeDto node = mapRowToNode(rs, "country");
            node.setConsumption(getDouble(rs, "consumption"));
            node.setConsumptionFull(getDouble(rs, "consumption_full"));
            node.setCoveredCapacity(getDouble(rs, "covered_capacity"));
            String regCode = rs.getString("region_code");
            String ctryCode = rs.getString("code");
            List<StorageNodeDto> opChildren = operatorsByCountry.getOrDefault(ctryCode, Collections.emptyList());
            node.setChildren(opChildren);
            countriesByRegion.computeIfAbsent(regCode, k -> new ArrayList<>()).add(node);
        });

        // 4. Fetch all regions
        String regSql = """
            SELECT r.code, r.name,
                   rs.status, rs.gas_in_storage, rs.full_percentage, rs.trend,
                   rs.injection, rs.withdrawal, rs.net_withdrawal, rs.working_gas_volume,
                   rs.injection_capacity, rs.withdrawal_capacity, rs.covered_capacity
            FROM regions r
            LEFT JOIN region_storage_data rs ON r.code = rs.region_code AND rs.gas_day = ?
            ORDER BY r.code ASC;
        """;
        List<StorageNodeDto> regions = new ArrayList<>();
        jdbcTemplate.query(regSql, ps -> ps.setObject(1, date), rs -> {
            StorageNodeDto node = mapRowToNode(rs, "region");
            node.setCoveredCapacity(getDouble(rs, "covered_capacity"));
            String regCode = rs.getString("code");
            List<StorageNodeDto> ctryChildren = countriesByRegion.getOrDefault(regCode, Collections.emptyList());
            node.setChildren(ctryChildren);
            regions.add(node);
        });

        return regions;
    }

    public List<FacilityHistorySeriesDto> getFacilityHistory(String countryCode, String fromDate, String toDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT f.code AS facility_code,
                   f.name AS facility_name,
                   o.name AS operator_name,
                   f.facility_type,
                   fs.gas_day,
                   fs.status,
                   fs.gas_in_storage,
                   fs.working_gas_volume,
                   fs.full_percentage
            FROM facilities f
            JOIN operators o ON f.operator_code = o.code
            JOIN facility_storage_data fs ON f.code = fs.facility_code
            WHERE f.country_code = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(countryCode);

        if (fromDate != null && !fromDate.isBlank()) {
            sql.append(" AND fs.gas_day >= ?::date ");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append(" AND fs.gas_day <= ?::date ");
            params.add(toDate);
        }

        sql.append(" ORDER BY f.name ASC, fs.gas_day ASC;");

        Map<String, FacilityHistorySeriesDto> seriesMap = new LinkedHashMap<>();

        jdbcTemplate.query(sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        }, rs -> {
            String facCode = rs.getString("facility_code");
            FacilityHistorySeriesDto series = seriesMap.computeIfAbsent(facCode, k -> {
                try {
                    return new FacilityHistorySeriesDto(
                        facCode,
                        rs.getString("facility_name"),
                        rs.getString("operator_name"),
                        rs.getString("facility_type"),
                        getDouble(rs, "working_gas_volume")
                    );
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            Double fullPct = getDouble(rs, "full_percentage");
            Double gis = getDouble(rs, "gas_in_storage");
            Double wgv = getDouble(rs, "working_gas_volume");
            String day = rs.getString("gas_day");
            String status = rs.getString("status");

            series.addPoint(new FacilityHistoryPointDto(day, fullPct, gis, wgv, status));
        });

        return new ArrayList<>(seriesMap.values());
    }

    public Map<String, Object> getOverviewSummary(String gasDay) {
        final String targetGasDay = (gasDay == null || gasDay.isBlank()) ? getLatestDate() : gasDay;
        Map<String, Object> summary = new HashMap<>();
        summary.put("gasDay", targetGasDay);

        String sql = """
            SELECT r.code, r.name, rs.gas_in_storage, rs.working_gas_volume, rs.full_percentage,
                   rs.injection, rs.withdrawal, rs.net_withdrawal
            FROM regions r
            JOIN region_storage_data rs ON r.code = rs.region_code
            WHERE rs.gas_day = ?::date;
        """;
        List<Map<String, Object>> regionsList = jdbcTemplate.query(sql, ps -> ps.setString(1, targetGasDay), (rs, rowNum) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("code", rs.getString("code"));
            m.put("name", rs.getString("name"));
            m.put("gasInStorage", getDouble(rs, "gas_in_storage"));
            m.put("workingGasVolume", getDouble(rs, "working_gas_volume"));
            m.put("fullPercentage", getDouble(rs, "full_percentage"));
            m.put("injection", getDouble(rs, "injection"));
            m.put("withdrawal", getDouble(rs, "withdrawal"));
            m.put("netWithdrawal", getDouble(rs, "net_withdrawal"));
            return m;
        });
        summary.put("regions", regionsList);

        return summary;
    }

    private StorageNodeDto mapRowToNode(ResultSet rs, String type) throws SQLException {
        StorageNodeDto node = new StorageNodeDto();
        node.setType(type);
        node.setCode(rs.getString("code"));
        node.setName(rs.getString("name"));
        node.setId(type + "_" + node.getCode());
        node.setStatus(rs.getString("status"));
        node.setGasInStorage(getDouble(rs, "gas_in_storage"));
        node.setFullPercentage(getDouble(rs, "full_percentage"));
        node.setTrend(getDouble(rs, "trend"));
        node.setInjection(getDouble(rs, "injection"));
        node.setWithdrawal(getDouble(rs, "withdrawal"));
        node.setNetWithdrawal(getDouble(rs, "net_withdrawal"));
        node.setWorkingGasVolume(getDouble(rs, "working_gas_volume"));
        node.setInjectionCapacity(getDouble(rs, "injection_capacity"));
        node.setWithdrawalCapacity(getDouble(rs, "withdrawal_capacity"));
        return node;
    }

    private static Double getDouble(ResultSet rs, String column) throws SQLException {
        double val = rs.getDouble(column);
        return rs.wasNull() ? null : val;
    }
}
