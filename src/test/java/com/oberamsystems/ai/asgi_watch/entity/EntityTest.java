package com.oberamsystems.ai.asgi_watch.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testRegionEntity() {
        Region r = new Region();
        r.setCode("EU");
        r.setName("European Union");
        LocalDateTime now = LocalDateTime.now();
        r.setCreatedAt(now);

        assertEquals("EU", r.getCode());
        assertEquals("European Union", r.getName());
        assertEquals(now, r.getCreatedAt());

        Region r2 = new Region("NE", "Non-EU");
        assertEquals("NE", r2.getCode());
        assertEquals("Non-EU", r2.getName());
    }

    @Test
    void testCountryEntity() {
        Region r = new Region("EU", "European Union");
        Country c = new Country();
        c.setCode("DE");
        c.setName("Germany");
        c.setRegion(r);
        LocalDateTime now = LocalDateTime.now();
        c.setCreatedAt(now);

        assertEquals("DE", c.getCode());
        assertEquals("Germany", c.getName());
        assertEquals(r, c.getRegion());
        assertEquals(now, c.getCreatedAt());

        Country c2 = new Country("AT", "Austria", r);
        assertEquals("AT", c2.getCode());
        assertEquals("Austria", c2.getName());
        assertEquals(r, c2.getRegion());
    }

    @Test
    void testOperatorEntity() {
        Region r = new Region("EU", "European Union");
        Country c = new Country("DE", "Germany", r);
        Operator op = new Operator();
        op.setCode("OP1");
        op.setName("Uniper Energy Storage");
        op.setCountry(c);
        op.setPublicationLink("https://example.com");
        op.setTransparencyTemplate("https://template.example.com");
        LocalDateTime now = LocalDateTime.now();
        op.setCreatedAt(now);

        assertEquals("OP1", op.getCode());
        assertEquals("Uniper Energy Storage", op.getName());
        assertEquals(c, op.getCountry());
        assertEquals("https://example.com", op.getPublicationLink());
        assertEquals("https://template.example.com", op.getTransparencyTemplate());
        assertEquals(now, op.getCreatedAt());

        Operator op2 = new Operator("OP2", "Astora", c);
        assertEquals("OP2", op2.getCode());
        assertEquals("Astora", op2.getName());
        assertEquals(c, op2.getCountry());
    }

    @Test
    void testFacilityEntity() {
        Region r = new Region("EU", "European Union");
        Country c = new Country("DE", "Germany", r);
        Operator op = new Operator("OP1", "Uniper", c);

        Facility f = new Facility();
        f.setCode("FAC1");
        f.setName("Bierwang");
        f.setOperator(op);
        f.setCountry(c);
        f.setFacilityType("UGS");
        f.setLatitude(48.1);
        f.setLongitude(11.5);
        LocalDateTime now = LocalDateTime.now();
        f.setCreatedAt(now);

        assertEquals("FAC1", f.getCode());
        assertEquals("Bierwang", f.getName());
        assertEquals(op, f.getOperator());
        assertEquals(c, f.getCountry());
        assertEquals("UGS", f.getFacilityType());
        assertEquals(48.1, f.getLatitude());
        assertEquals(11.5, f.getLongitude());
        assertEquals(now, f.getCreatedAt());

        Facility f2 = new Facility("FAC2", "Epe", op, c, "Salt Cavern");
        assertEquals("FAC2", f2.getCode());
        assertEquals("Epe", f2.getName());
        assertEquals(op, f2.getOperator());
        assertEquals(c, f2.getCountry());
        assertEquals("Salt Cavern", f2.getFacilityType());
    }

    @Test
    void testRegionStorageDataEntity() {
        Region r = new Region("EU", "European Union");
        RegionStorageData rs = new RegionStorageData();
        rs.setId(10L);
        rs.setRegion(r);
        LocalDate day = LocalDate.of(2026, 9, 27);
        rs.setGasDay(day);
        rs.setGasDayStart(day);
        rs.setGasDayEnd(day);
        rs.setStatus("C");
        rs.setGasInStorage(100.5);
        rs.setFullPercentage(95.2);
        rs.setTrend(0.1);
        rs.setInjection(1.2);
        rs.setWithdrawal(0.5);
        rs.setNetWithdrawal(-0.7);
        rs.setWorkingGasVolume(105.0);
        rs.setInjectionCapacity(2.0);
        rs.setWithdrawalCapacity(3.0);
        rs.setCoveredCapacity(98.0);
        rs.setUpdatedAtSource("2026-09-27T10:00:00Z");
        LocalDateTime now = LocalDateTime.now();
        rs.setScrapedAt(now);

        assertEquals(10L, rs.getId());
        assertEquals(r, rs.getRegion());
        assertEquals(day, rs.getGasDay());
        assertEquals(day, rs.getGasDayStart());
        assertEquals(day, rs.getGasDayEnd());
        assertEquals("C", rs.getStatus());
        assertEquals(100.5, rs.getGasInStorage());
        assertEquals(95.2, rs.getFullPercentage());
        assertEquals(0.1, rs.getTrend());
        assertEquals(1.2, rs.getInjection());
        assertEquals(0.5, rs.getWithdrawal());
        assertEquals(-0.7, rs.getNetWithdrawal());
        assertEquals(105.0, rs.getWorkingGasVolume());
        assertEquals(2.0, rs.getInjectionCapacity());
        assertEquals(3.0, rs.getWithdrawalCapacity());
        assertEquals(98.0, rs.getCoveredCapacity());
        assertEquals("2026-09-27T10:00:00Z", rs.getUpdatedAtSource());
        assertEquals(now, rs.getScrapedAt());
    }

    @Test
    void testCountryStorageDataEntity() {
        Country c = new Country("DE", "Germany", new Region("EU", "European Union"));
        CountryStorageData cs = new CountryStorageData();
        cs.setId(20L);
        cs.setCountry(c);
        LocalDate day = LocalDate.of(2026, 9, 27);
        cs.setGasDay(day);
        cs.setGasDayStart(day);
        cs.setGasDayEnd(day);
        cs.setStatus("C");
        cs.setGasInStorage(200.0);
        cs.setFullPercentage(88.0);
        cs.setTrend(0.2);
        cs.setInjection(2.5);
        cs.setWithdrawal(1.0);
        cs.setNetWithdrawal(-1.5);
        cs.setWorkingGasVolume(227.0);
        cs.setInjectionCapacity(5.0);
        cs.setWithdrawalCapacity(6.0);
        cs.setContractedCapacity(220.0);
        cs.setAvailableCapacity(7.0);
        cs.setConsumption(30.0);
        cs.setConsumptionFull(35.0);
        cs.setCoveredCapacity(99.0);
        cs.setUpdatedAtSource("2026-09-27T10:00:00Z");
        LocalDateTime now = LocalDateTime.now();
        cs.setScrapedAt(now);

        assertEquals(20L, cs.getId());
        assertEquals(c, cs.getCountry());
        assertEquals(day, cs.getGasDay());
        assertEquals(day, cs.getGasDayStart());
        assertEquals(day, cs.getGasDayEnd());
        assertEquals("C", cs.getStatus());
        assertEquals(200.0, cs.getGasInStorage());
        assertEquals(88.0, cs.getFullPercentage());
        assertEquals(0.2, cs.getTrend());
        assertEquals(2.5, cs.getInjection());
        assertEquals(1.0, cs.getWithdrawal());
        assertEquals(-1.5, cs.getNetWithdrawal());
        assertEquals(227.0, cs.getWorkingGasVolume());
        assertEquals(5.0, cs.getInjectionCapacity());
        assertEquals(6.0, cs.getWithdrawalCapacity());
        assertEquals(220.0, cs.getContractedCapacity());
        assertEquals(7.0, cs.getAvailableCapacity());
        assertEquals(30.0, cs.getConsumption());
        assertEquals(35.0, cs.getConsumptionFull());
        assertEquals(99.0, cs.getCoveredCapacity());
        assertEquals("2026-09-27T10:00:00Z", cs.getUpdatedAtSource());
        assertEquals(now, cs.getScrapedAt());
    }

    @Test
    void testOperatorStorageDataEntity() {
        Country c = new Country("DE", "Germany", new Region("EU", "European Union"));
        Operator o = new Operator("OP1", "Uniper", c);
        OperatorStorageData os = new OperatorStorageData();
        os.setId(30L);
        os.setOperator(o);
        LocalDate day = LocalDate.of(2026, 9, 27);
        os.setGasDay(day);
        os.setGasDayStart(day);
        os.setGasDayEnd(day);
        os.setStatus("N");
        os.setGasInStorage(50.0);
        os.setFullPercentage(80.0);
        os.setTrend(-0.1);
        os.setInjection(0.5);
        os.setWithdrawal(1.2);
        os.setNetWithdrawal(0.7);
        os.setWorkingGasVolume(62.5);
        os.setInjectionCapacity(1.5);
        os.setWithdrawalCapacity(2.0);
        os.setContractedCapacity(60.0);
        os.setAvailableCapacity(2.5);
        os.setCoveredCapacity(95.0);
        os.setUpdatedAtSource("2026-09-27T10:00:00Z");
        LocalDateTime now = LocalDateTime.now();
        os.setScrapedAt(now);

        assertEquals(30L, os.getId());
        assertEquals(o, os.getOperator());
        assertEquals(day, os.getGasDay());
        assertEquals(day, os.getGasDayStart());
        assertEquals(day, os.getGasDayEnd());
        assertEquals("N", os.getStatus());
        assertEquals(50.0, os.getGasInStorage());
        assertEquals(80.0, os.getFullPercentage());
        assertEquals(-0.1, os.getTrend());
        assertEquals(0.5, os.getInjection());
        assertEquals(1.2, os.getWithdrawal());
        assertEquals(0.7, os.getNetWithdrawal());
        assertEquals(62.5, os.getWorkingGasVolume());
        assertEquals(1.5, os.getInjectionCapacity());
        assertEquals(2.0, os.getWithdrawalCapacity());
        assertEquals(60.0, os.getContractedCapacity());
        assertEquals(2.5, os.getAvailableCapacity());
        assertEquals(95.0, os.getCoveredCapacity());
        assertEquals("2026-09-27T10:00:00Z", os.getUpdatedAtSource());
        assertEquals(now, os.getScrapedAt());
    }

    @Test
    void testFacilityStorageDataEntity() {
        Country c = new Country("DE", "Germany", new Region("EU", "European Union"));
        Operator o = new Operator("OP1", "Uniper", c);
        Facility f = new Facility("FAC1", "Bierwang", o, c, "UGS");

        FacilityStorageData fs = new FacilityStorageData();
        fs.setId(40L);
        fs.setFacility(f);
        LocalDate day = LocalDate.of(2026, 9, 27);
        fs.setGasDay(day);
        fs.setGasDayStart(day);
        fs.setGasDayEnd(day);
        fs.setStatus("1");
        fs.setGasInStorage(12.3);
        fs.setFullPercentage(75.5);
        fs.setTrend(0.05);
        fs.setInjection(0.2);
        fs.setWithdrawal(0.1);
        fs.setNetWithdrawal(-0.1);
        fs.setWorkingGasVolume(16.3);
        fs.setInjectionCapacity(0.5);
        fs.setWithdrawalCapacity(0.8);
        fs.setContractedCapacity(16.0);
        fs.setAvailableCapacity(0.3);
        fs.setUpdatedAtSource("2026-09-27T10:00:00Z");
        LocalDateTime now = LocalDateTime.now();
        fs.setScrapedAt(now);

        assertEquals(40L, fs.getId());
        assertEquals(f, fs.getFacility());
        assertEquals(day, fs.getGasDay());
        assertEquals(day, fs.getGasDayStart());
        assertEquals(day, fs.getGasDayEnd());
        assertEquals("1", fs.getStatus());
        assertEquals(12.3, fs.getGasInStorage());
        assertEquals(75.5, fs.getFullPercentage());
        assertEquals(0.05, fs.getTrend());
        assertEquals(0.2, fs.getInjection());
        assertEquals(0.1, fs.getWithdrawal());
        assertEquals(-0.1, fs.getNetWithdrawal());
        assertEquals(16.3, fs.getWorkingGasVolume());
        assertEquals(0.5, fs.getInjectionCapacity());
        assertEquals(0.8, fs.getWithdrawalCapacity());
        assertEquals(16.0, fs.getContractedCapacity());
        assertEquals(0.3, fs.getAvailableCapacity());
        assertEquals("2026-09-27T10:00:00Z", fs.getUpdatedAtSource());
        assertEquals(now, fs.getScrapedAt());
    }

    @Test
    void testLombokBuildersAndIdentity() {
        Region r = Region.builder()
                .code("EU")
                .name("European Union")
                .createdAt(LocalDateTime.now())
                .build();
        assertEquals("EU", r.getCode());
        assertEquals("European Union", r.getName());
        assertTrue(r.toString().contains("EU"));

        Region rSame = Region.builder().code("EU").name("Different Name").build();
        Region rDiff = Region.builder().code("NE").name("Non-EU").build();
        assertEquals(r, rSame);
        assertNotEquals(r, rDiff);
        assertEquals(r.hashCode(), rSame.hashCode());

        Country c = Country.builder()
                .code("DE")
                .name("Germany")
                .region(r)
                .build();
        assertEquals("DE", c.getCode());
        assertTrue(c.toString().contains("DE"));
        Country cSame = Country.builder().code("DE").name("Other").build();
        assertEquals(c, cSame);
        assertEquals(c.hashCode(), cSame.hashCode());

        Operator op = Operator.builder()
                .code("OP1")
                .name("Uniper")
                .country(c)
                .build();
        assertEquals("OP1", op.getCode());
        assertTrue(op.toString().contains("OP1"));
        Operator opSame = Operator.builder().code("OP1").name("Other").build();
        assertEquals(op, opSame);
        assertEquals(op.hashCode(), opSame.hashCode());

        Facility f = Facility.builder()
                .code("F1")
                .name("Bierwang")
                .operator(op)
                .country(c)
                .facilityType("UGS")
                .build();
        assertEquals("F1", f.getCode());
        assertTrue(f.toString().contains("F1"));
        Facility fSame = Facility.builder().code("F1").name("Other").build();
        assertEquals(f, fSame);
        assertEquals(f.hashCode(), fSame.hashCode());

        LocalDate day = LocalDate.of(2026, 9, 28);
        RegionStorageData rsd = RegionStorageData.builder().id(100L).region(r).gasDay(day).build();
        RegionStorageData rsdSame = RegionStorageData.builder().id(100L).gasDay(day).build();
        assertEquals(rsd, rsdSame);
        assertEquals(rsd.hashCode(), rsdSame.hashCode());
        assertTrue(rsd.toString().contains("100"));

        CountryStorageData csd = CountryStorageData.builder().id(200L).country(c).gasDay(day).build();
        CountryStorageData csdSame = CountryStorageData.builder().id(200L).build();
        assertEquals(csd, csdSame);
        assertEquals(csd.hashCode(), csdSame.hashCode());
        assertTrue(csd.toString().contains("200"));

        OperatorStorageData osd = OperatorStorageData.builder().id(300L).operator(op).gasDay(day).build();
        OperatorStorageData osdSame = OperatorStorageData.builder().id(300L).build();
        assertEquals(osd, osdSame);
        assertEquals(osd.hashCode(), osdSame.hashCode());
        assertTrue(osd.toString().contains("300"));

        FacilityStorageData fsd = FacilityStorageData.builder().id(400L).facility(f).gasDay(day).build();
        FacilityStorageData fsdSame = FacilityStorageData.builder().id(400L).build();
        assertEquals(fsd, fsdSame);
        assertEquals(fsd.hashCode(), fsdSame.hashCode());
        assertTrue(fsd.toString().contains("400"));
    }
}
