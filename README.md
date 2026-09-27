# AGSI Watch - European Gas Storage Viewer

A Spring Boot & D3.js web application for visualizing and auditing European Underground Gas Storage transparency data from PostgreSQL (`agsi` database).

## Features

1. **Storage Inventory Status (AGSI Table View)**:
   - Replicates the official [agsi.gie.eu](https://agsi.gie.eu/) 4-tier tree:
     `Region -> Country -> Operator (SSO) -> Storage Facility (UGS)`
   - Expandable / collapsible nodes with search filtering.
   - Status indicators (`C` Confirmed, `E` Estimated, `N` No Data).
   - Core metrics: Gas in Storage (TWh), Fullness (%), Trend (% pts), Consumption (TWh), Stock/Consumption ratio (%), Injection (GWh/d), Withdrawal (GWh/d), Technical Capacity (TWh), and Market Coverage (%).

2. **Facility Storage Time-Series (D3.js Line Chart)**:
   - Interactive multi-line chart displaying the development over time of the percentual storage (`full_percentage`) of all storage facilities of a selected country.
   - 269 gas days of historical data (January 2026 – September 2026).
   - Reference indicators for EU 90% storage target and 100% technical capacity.
   - Interactive crosshair with inspection tooltip (date, facility name, operator, full %, gas in storage, capacity).
   - Interactive sidebar facility legend allowing hover highlights and click-to-pin isolation.
   - Range presets: `All (2026)`, `Last 90 Days`, `Last 30 Days`.

3. **REST API**:
   - `GET /api/dates`: Available gas days in descending order.
   - `GET /api/tree?date=YYYY-MM-DD`: Hierarchical inventory tree for the selected gas day.
   - `GET /api/countries`: List of countries with facility counts.
   - `GET /api/facilities/history?country=DE`: Historical daily points for each facility in that country.
   - `GET /api/summary?date=YYYY-MM-DD`: Key regional headline metrics.

---

## Running the Application

### Prerequisites
- Java 25 (OpenJDK / GraalVM)
- PostgreSQL running locally with database `agsi`

### Build & Run
```bash
cd asgi-watch
./gradlew bootRun
```
Or run the pre-built jar:
```bash
java -jar build/libs/asgi-watch-0.0.1-SNAPSHOT.jar
```

Open your browser at:
👉 **[http://localhost:8080/](http://localhost:8080/)**
