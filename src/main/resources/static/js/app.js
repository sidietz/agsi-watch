/**
 * Main application controller for AGSI Watch viewer.
 * Manages tabs, global date state, data fetching, and component dispatching.
 */

const App = (() => {
    let currentDate = null;
    let currentCountry = "DE";

    async function init() {
        TableView.init();
        D3Chart.init();

        setupTabs();
        setupEventListeners();

        // 1. Fetch available dates and load initial data
        await loadDates();
        await loadCountries();

        // 2. Load summary and default views
        if (currentDate) {
            loadSummary(currentDate);
            loadTableData(currentDate);
        }
        loadChartData(currentCountry);
    }

    function setupTabs() {
        const tabBtns = document.querySelectorAll(".tab-btn");
        tabBtns.forEach(btn => {
            btn.addEventListener("click", () => {
                tabBtns.forEach(b => b.classList.remove("active"));
                btn.classList.add("active");

                const targetTab = btn.dataset.tab;
                document.querySelectorAll(".tab-pane").forEach(pane => {
                    pane.classList.remove("active");
                });
                document.getElementById(targetTab).classList.add("active");

                // If switching to chart, trigger resize to ensure proper dimensions
                if (targetTab === "tab-chart") {
                    window.dispatchEvent(new Event("resize"));
                }
            });
        });
    }

    function setupEventListeners() {
        const dateSelect = document.getElementById("gas-day-select");
        dateSelect.addEventListener("change", (e) => {
            currentDate = e.target.value;
            loadSummary(currentDate);
            loadTableData(currentDate);
        });

        const countrySelect = document.getElementById("chart-country-select");
        countrySelect.addEventListener("change", (e) => {
            currentCountry = e.target.value;
            loadChartData(currentCountry);
        });

        const refreshBtn = document.getElementById("refresh-btn");
        refreshBtn.addEventListener("click", () => {
            if (currentDate) {
                loadSummary(currentDate);
                loadTableData(currentDate);
            }
            loadChartData(currentCountry);
        });
    }

    async function loadDates() {
        try {
            const resp = await fetch("/api/dates");
            if (!resp.ok) throw new Error("Failed to fetch dates");
            const dates = await resp.json();

            const select = document.getElementById("gas-day-select");
            select.innerHTML = "";

            if (!dates || !dates.length) {
                select.innerHTML = '<option value="">No data found</option>';
                return;
            }

            dates.forEach((d, idx) => {
                const opt = document.createElement("option");
                opt.value = d;
                opt.textContent = idx === 0 ? `${d} (Latest)` : d;
                select.appendChild(opt);
            });

            currentDate = dates[0];
            select.value = currentDate;
        } catch (err) {
            console.error("Error loading dates:", err);
            document.getElementById("gas-day-select").innerHTML = '<option value="">Error loading dates</option>';
        }
    }

    async function loadCountries() {
        try {
            const resp = await fetch("/api/countries");
            if (!resp.ok) throw new Error("Failed to fetch countries");
            const countries = await resp.json();

            const select = document.getElementById("chart-country-select");
            select.innerHTML = "";

            countries.forEach(c => {
                const opt = document.createElement("option");
                opt.value = c.code;
                opt.textContent = `${c.name} (${c.code}) - ${c.facilityCount} facilities`;
                if (c.code === "DE") {
                    opt.selected = true;
                }
                select.appendChild(opt);
            });

            if (select.value) {
                currentCountry = select.value;
            }
        } catch (err) {
            console.error("Error loading countries:", err);
        }
    }

    async function loadSummary(date) {
        try {
            const resp = await fetch(`/api/summary?date=${encodeURIComponent(date)}`);
            if (!resp.ok) throw new Error("Failed to fetch summary");
            const data = await resp.json();

            const regions = data.regions || [];
            const eu = regions.find(r => r.code === "EU");
            const noneu = regions.find(r => r.code === "Non-EU");

            if (eu) {
                const gis = eu.gasInStorage !== null ? eu.gasInStorage.toFixed(2) + " TWh" : "-- TWh";
                const full = eu.fullPercentage !== null ? eu.fullPercentage.toFixed(2) + "%" : "--%";
                const cap = eu.workingGasVolume !== null ? "Capacity: " + eu.workingGasVolume.toFixed(2) + " TWh" : "Capacity: --";

                document.getElementById("kpi-eu-gis").textContent = gis;
                document.getElementById("kpi-eu-full").textContent = full;
                document.getElementById("kpi-eu-cap").textContent = cap;
                document.getElementById("kpi-eu-bar").style.width = Math.min(100, (eu.fullPercentage || 0)) + "%";
            }

            if (noneu) {
                const gis = noneu.gasInStorage !== null ? noneu.gasInStorage.toFixed(2) + " TWh" : "-- TWh";
                const full = noneu.fullPercentage !== null ? noneu.fullPercentage.toFixed(2) + "%" : "--%";
                const cap = noneu.workingGasVolume !== null ? "Capacity: " + noneu.workingGasVolume.toFixed(2) + " TWh" : "Capacity: --";

                document.getElementById("kpi-noneu-gis").textContent = gis;
                document.getElementById("kpi-noneu-full").textContent = full;
                document.getElementById("kpi-noneu-cap").textContent = cap;
                document.getElementById("kpi-noneu-bar").style.width = Math.min(100, (noneu.fullPercentage || 0)) + "%";
            }
        } catch (err) {
            console.error("Error loading summary:", err);
        }
    }

    async function loadTableData(date) {
        const tbody = document.getElementById("table-body");
        tbody.innerHTML = '<tr><td colspan="11" class="loading-cell">Fetching gas storage inventory for ' + date + '...</td></tr>';

        try {
            const resp = await fetch(`/api/tree?date=${encodeURIComponent(date)}`);
            if (!resp.ok) throw new Error("Failed to fetch table tree");
            const data = await resp.json();
            TableView.render(data);
        } catch (err) {
            console.error("Error loading tree data:", err);
            tbody.innerHTML = '<tr><td colspan="11" class="loading-cell" style="color:var(--danger)">Failed to load data from server.</td></tr>';
        }
    }

    async function loadChartData(countryCode) {
        const container = document.getElementById("d3-chart-container");
        container.innerHTML = '<div style="display:flex; justify-content:center; align-items:center; height:100%; color:#64748b;">Loading facility time-series...</div>';

        try {
            const resp = await fetch(`/api/facilities/history?country=${encodeURIComponent(countryCode)}`);
            if (!resp.ok) throw new Error("Failed to fetch facility history");
            const data = await resp.json();
            D3Chart.render(data);
        } catch (err) {
            console.error("Error loading chart data:", err);
            container.innerHTML = '<div style="display:flex; justify-content:center; align-items:center; height:100%; color:var(--danger);">Failed to load facility time-series data.</div>';
        }
    }

    return { init };
})();

document.addEventListener("DOMContentLoaded", () => {
    App.init();
});
