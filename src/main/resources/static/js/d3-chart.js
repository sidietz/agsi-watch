/**
 * D3.js Multi-Line Time-Series Chart for Storage Facilities
 * Visualizes the percentual storage (full_percentage) development over time.
 */

const D3Chart = (() => {
    let rawData = [];
    let currentFilteredData = [];
    let activeRangeDays = "all";
    let pinnedFacilityCode = null;
    let colorScale = null;

    function init() {
        // Date range button handlers
        document.querySelectorAll(".range-btn-group .btn-chip").forEach(btn => {
            btn.addEventListener("click", (e) => {
                document.querySelectorAll(".range-btn-group .btn-chip").forEach(b => b.classList.remove("active"));
                e.target.classList.add("active");
                activeRangeDays = e.target.dataset.range;
                updateChart();
            });
        });

        // Top 5 / All / Clear buttons
        document.getElementById("chart-top5-btn").addEventListener("click", highlightTop5);
        document.getElementById("chart-all-btn").addEventListener("click", showAll);
        document.getElementById("chart-clear-btn").addEventListener("click", clearHighlights);

        // Sidebar search
        document.getElementById("facility-filter-input").addEventListener("input", filterLegend);

        // Window resize handler
        window.addEventListener("resize", () => {
            if (currentFilteredData.length > 0) {
                render(rawData);
            }
        });
    }

    function render(seriesData) {
        rawData = seriesData || [];
        pinnedFacilityCode = null;

        // Parse date strings to Date objects
        rawData.forEach(series => {
            series.points.forEach(p => {
                p.dateObj = new Date(p.date);
            });
            // Sort ascending by date
            series.points.sort((a, b) => a.dateObj - b.dateObj);
        });

        // Setup color scale
        const colors = [
            "#2563eb", "#16a34a", "#dc2626", "#d97706", "#9333ea",
            "#0891b2", "#ea580c", "#4f46e5", "#059669", "#b91c1c",
            "#475569", "#0284c7", "#ca8a04", "#7c3aed", "#be123c",
            "#15803d", "#c026d3", "#0d9488", "#b45309", "#4338ca"
        ];
        colorScale = d3.scaleOrdinal()
            .domain(rawData.map(s => s.code))
            .range(colors);

        updateChart();
        renderLegend(rawData);
    }

    function updateChart() {
        const container = document.getElementById("d3-chart-container");
        container.innerHTML = "";

        if (!rawData.length) {
            container.innerHTML = '<div style="display:flex; justify-content:center; align-items:center; height:100%; color:#64748b;">No facility historical data found for this country.</div>';
            return;
        }

        // Apply range filter
        let maxDate = d3.max(rawData, s => d3.max(s.points, p => p.dateObj));
        if (!maxDate) return;

        let minDate = d3.min(rawData, s => d3.min(s.points, p => p.dateObj));
        if (activeRangeDays === "30") {
            minDate = new Date(maxDate.getTime() - 30 * 24 * 60 * 60 * 1000);
        } else if (activeRangeDays === "90") {
            minDate = new Date(maxDate.getTime() - 90 * 24 * 60 * 60 * 1000);
        }

        currentFilteredData = rawData.map(series => ({
            ...series,
            filteredPoints: series.points.filter(p => p.dateObj >= minDate && p.dateObj <= maxDate && p.fullPercentage !== null)
        })).filter(s => s.filteredPoints.length > 0);

        if (!currentFilteredData.length) {
            container.innerHTML = '<div style="display:flex; justify-content:center; align-items:center; height:100%; color:#64748b;">No data points in selected date range.</div>';
            return;
        }

        const width = container.clientWidth || 800;
        const height = container.clientHeight || 500;
        const margin = { top: 25, right: 35, bottom: 45, left: 55 };

        const svg = d3.select(container)
            .append("svg")
            .attr("width", width)
            .attr("height", height)
            .attr("viewBox", [0, 0, width, height])
            .attr("style", "max-width: 100%; height: auto; font-family: sans-serif;");

        // Tooltip container
        const tooltip = d3.select(container)
            .append("div")
            .attr("class", "d3-tooltip")
            .style("display", "none");

        // Scales
        const xScale = d3.scaleTime()
            .domain([minDate, maxDate])
            .range([margin.left, width - margin.right]);

        // Max Y (at least 105% to accommodate full facilities and targets)
        const maxY = Math.max(105, d3.max(currentFilteredData, s => d3.max(s.filteredPoints, p => p.fullPercentage)) || 100);
        const yScale = d3.scaleLinear()
            .domain([0, maxY])
            .nice()
            .range([height - margin.bottom, margin.top]);

        // Grid lines
        svg.append("g")
            .attr("class", "grid")
            .attr("transform", `translate(${margin.left},0)`)
            .call(d3.axisLeft(yScale)
                .tickSize(-(width - margin.left - margin.right))
                .tickFormat("")
            )
            .call(g => g.select(".domain").remove())
            .call(g => g.selectAll(".tick line")
                .attr("stroke", "#f1f5f9")
                .attr("stroke-dasharray", "2,2")
            );

        // X Axis
        const xAxis = d3.axisBottom(xScale)
            .ticks(Math.max(5, Math.floor(width / 90)))
            .tickFormat(d3.timeFormat("%b %d"));

        svg.append("g")
            .attr("transform", `translate(0,${height - margin.bottom})`)
            .call(xAxis)
            .call(g => g.select(".domain").attr("stroke", "#cbd5e1"))
            .call(g => g.selectAll(".tick text").attr("fill", "#64748b").attr("font-size", "11px"));

        // Y Axis
        const yAxis = d3.axisLeft(yScale)
            .ticks(6)
            .tickFormat(d => d + "%");

        svg.append("g")
            .attr("transform", `translate(${margin.left},0)`)
            .call(yAxis)
            .call(g => g.select(".domain").attr("stroke", "#cbd5e1"))
            .call(g => g.selectAll(".tick text").attr("fill", "#64748b").attr("font-size", "11px"));

        // Reference Line: 90% Target
        if (yScale(90) >= margin.top && yScale(90) <= height - margin.bottom) {
            svg.append("line")
                .attr("x1", margin.left)
                .attr("x2", width - margin.right)
                .attr("y1", yScale(90))
                .attr("y2", yScale(90))
                .attr("stroke", "#f59e0b")
                .attr("stroke-width", 1.5)
                .attr("stroke-dasharray", "4,4");

            svg.append("text")
                .attr("x", width - margin.right - 5)
                .attr("y", yScale(90) - 4)
                .attr("text-anchor", "end")
                .attr("fill", "#d97706")
                .attr("font-size", "10px")
                .attr("font-weight", "600")
                .text("EU Target 90%");
        }

        // Reference Line: 100% Capacity
        if (yScale(100) >= margin.top && yScale(100) <= height - margin.bottom) {
            svg.append("line")
                .attr("x1", margin.left)
                .attr("x2", width - margin.right)
                .attr("y1", yScale(100))
                .attr("y2", yScale(100))
                .attr("stroke", "#ef4444")
                .attr("stroke-width", 1.5)
                .attr("stroke-opacity", 0.7);

            svg.append("text")
                .attr("x", width - margin.right - 5)
                .attr("y", yScale(100) - 4)
                .attr("text-anchor", "end")
                .attr("fill", "#dc2626")
                .attr("font-size", "10px")
                .attr("font-weight", "600")
                .text("100% Technical WGV");
        }

        // Line generator
        const lineGen = d3.line()
            .x(d => xScale(d.dateObj))
            .y(d => yScale(d.fullPercentage))
            .curve(d3.curveMonotoneX);

        // Path group
        const pathGroup = svg.append("g").attr("class", "lines-group");

        const linePaths = pathGroup.selectAll(".facility-line")
            .data(currentFilteredData)
            .enter()
            .append("path")
            .attr("class", "facility-line")
            .attr("id", d => `line-${d.code.replace(/[^a-zA-Z0-9]/g, '_')}`)
            .attr("d", d => lineGen(d.filteredPoints))
            .attr("fill", "none")
            .attr("stroke", d => colorScale(d.code))
            .attr("stroke-width", d => (d.code === pinnedFacilityCode ? 3.5 : 2.0))
            .attr("stroke-opacity", d => {
                if (pinnedFacilityCode) {
                    return d.code === pinnedFacilityCode ? 1.0 : 0.15;
                }
                return 0.85;
            });

        // Vertical crosshair guideline
        const crosshair = svg.append("line")
            .attr("class", "crosshair")
            .attr("stroke", "#94a3b8")
            .attr("stroke-width", 1)
            .attr("stroke-dasharray", "3,3")
            .attr("y1", margin.top)
            .attr("y2", height - margin.bottom)
            .style("display", "none");

        // Hover tracking overlay
        const overlay = svg.append("rect")
            .attr("width", width - margin.left - margin.right)
            .attr("height", height - margin.top - margin.bottom)
            .attr("transform", `translate(${margin.left},${margin.top})`)
            .attr("fill", "none")
            .attr("pointer-events", "all")
            .style("cursor", "crosshair");

        const bisectDate = d3.bisector(d => d.dateObj).center;

        overlay.on("pointerenter", () => {
            crosshair.style("display", null);
            tooltip.style("display", null);
        }).on("pointerleave", () => {
            crosshair.style("display", "none");
            tooltip.style("display", "none");
        }).on("pointermove", (event) => {
            const [mouseX, mouseY] = d3.pointer(event);
            const hoveredDate = xScale.invert(mouseX + margin.left);

            crosshair
                .attr("x1", mouseX + margin.left)
                .attr("x2", mouseX + margin.left);

            // Find nearest points for each facility on this date
            const pointsAtDate = [];
            currentFilteredData.forEach(series => {
                const idx = bisectDate(series.filteredPoints, hoveredDate);
                const p = series.filteredPoints[idx];
                if (p) {
                    const yPos = yScale(p.fullPercentage);
                    const distY = Math.abs(yPos - (mouseY + margin.top));
                    pointsAtDate.push({
                        series: series,
                        point: p,
                        distY: distY,
                        yPos: yPos
                    });
                }
            });

            if (!pointsAtDate.length) return;

            // Sort by proximity to mouse cursor on Y-axis
            pointsAtDate.sort((a, b) => a.distY - b.distY);
            const closest = pointsAtDate[0];

            // Render tooltip
            const dateStr = d3.timeFormat("%B %d, %Y")(closest.point.dateObj);
            const fac = closest.series;
            const fullPct = closest.point.fullPercentage.toFixed(2);
            const gis = closest.point.gasInStorage !== null ? closest.point.gasInStorage.toFixed(2) + " TWh" : "-";
            const wgv = closest.point.workingGasVolume !== null ? closest.point.workingGasVolume.toFixed(2) + " TWh" : "-";
            const color = colorScale(fac.code);

            tooltip.html(`
                <div class="d3-tooltip-title" style="border-left: 3px solid ${color}; padding-left: 6px;">
                    ${fac.name}
                </div>
                <div class="d3-tooltip-row">
                    <span style="color:#cbd5e1;">Gas Day:</span>
                    <span>${dateStr}</span>
                </div>
                <div class="d3-tooltip-row">
                    <span style="color:#cbd5e1;">Operator:</span>
                    <span>${fac.operatorName || '-'}</span>
                </div>
                <div class="d3-tooltip-row">
                    <span style="color:#cbd5e1;">Fullness:</span>
                    <span class="d3-tooltip-val" style="color:${color}; font-size:1rem;">${fullPct}%</span>
                </div>
                <div class="d3-tooltip-row">
                    <span style="color:#cbd5e1;">Gas in Storage:</span>
                    <span>${gis}</span>
                </div>
                <div class="d3-tooltip-row">
                    <span style="color:#cbd5e1;">Working Volume:</span>
                    <span>${wgv}</span>
                </div>
            `);

            // Tooltip position
            const tooltipNode = tooltip.node();
            const tipWidth = tooltipNode.offsetWidth || 220;
            const tipHeight = tooltipNode.offsetHeight || 120;

            let left = mouseX + margin.left + 15;
            if (left + tipWidth > width) {
                left = mouseX + margin.left - tipWidth - 15;
            }
            let top = mouseY + margin.top - tipHeight / 2;
            top = Math.max(10, Math.min(height - tipHeight - 10, top));

            tooltip
                .style("left", `${left}px`)
                .style("top", `${top}px`);
        });
    }

    function renderLegend(seriesData) {
        const container = document.getElementById("facility-legend-list");
        container.innerHTML = "";
        document.getElementById("sidebar-fac-count").textContent = seriesData.length;

        // Sort descending by technical capacity or latest fullness
        const sorted = [...seriesData].sort((a, b) => {
            const aCap = a.workingGasVolume || 0;
            const bCap = b.workingGasVolume || 0;
            return bCap - aCap;
        });

        sorted.forEach(series => {
            const latestPoint = series.points[series.points.length - 1];
            const fullPct = latestPoint && latestPoint.fullPercentage !== null 
                ? latestPoint.fullPercentage.toFixed(1) + "%" 
                : "-";
            const color = colorScale(series.code);

            const div = document.createElement("div");
            div.className = "facility-legend-item";
            div.id = `legend-item-${series.code.replace(/[^a-zA-Z0-9]/g, '_')}`;
            div.innerHTML = `
                <div class="legend-info">
                    <span class="color-dot" style="background-color: ${color};"></span>
                    <span class="facility-title" title="${series.name} (${series.operatorName || ''})">${series.name}</span>
                </div>
                <span class="legend-val">${fullPct}</span>
            `;

            // Hover interactions
            div.addEventListener("mouseenter", () => highlightFacility(series.code));
            div.addEventListener("mouseleave", () => {
                if (!pinnedFacilityCode) {
                    resetLineHighlights();
                } else {
                    highlightFacility(pinnedFacilityCode);
                }
            });

            // Click to pin/toggle
            div.addEventListener("click", () => {
                if (pinnedFacilityCode === series.code) {
                    pinnedFacilityCode = null;
                    resetLineHighlights();
                } else {
                    pinnedFacilityCode = series.code;
                    highlightFacility(series.code);
                }
            });

            container.appendChild(div);
        });
    }

    function highlightFacility(code) {
        const safeCode = code.replace(/[^a-zA-Z0-9]/g, '_');
        d3.selectAll(".facility-line")
            .attr("stroke-opacity", d => d.code === code ? 1.0 : 0.12)
            .attr("stroke-width", d => d.code === code ? 3.5 : 1.5);

        // Highlight matching line and move to front
        const activeLine = d3.select(`#line-${safeCode}`);
        if (activeLine.node()) {
            activeLine.raise();
        }

        // Highlight matching legend item
        document.querySelectorAll(".facility-legend-item").forEach(item => {
            if (item.id === `legend-item-${safeCode}`) {
                item.classList.add("highlighted");
                item.classList.remove("dimmed");
            } else {
                item.classList.remove("highlighted");
                item.classList.add("dimmed");
            }
        });
    }

    function resetLineHighlights() {
        d3.selectAll(".facility-line")
            .attr("stroke-opacity", 0.85)
            .attr("stroke-width", 2.0);

        document.querySelectorAll(".facility-legend-item").forEach(item => {
            item.classList.remove("highlighted");
            item.classList.remove("dimmed");
        });
    }

    function highlightTop5() {
        pinnedFacilityCode = null;
        const sorted = [...rawData].sort((a, b) => (b.workingGasVolume || 0) - (a.workingGasVolume || 0));
        const top5Codes = new Set(sorted.slice(0, 5).map(s => s.code));

        d3.selectAll(".facility-line")
            .attr("stroke-opacity", d => top5Codes.has(d.code) ? 1.0 : 0.1)
            .attr("stroke-width", d => top5Codes.has(d.code) ? 3.0 : 1.2);

        document.querySelectorAll(".facility-legend-item").forEach(item => {
            const itemCode = item.id.replace('legend-item-', '');
            const matching = [...top5Codes].some(c => c.replace(/[^a-zA-Z0-9]/g, '_') === itemCode);
            if (matching) {
                item.classList.add("highlighted");
                item.classList.remove("dimmed");
            } else {
                item.classList.remove("highlighted");
                item.classList.add("dimmed");
            }
        });
    }

    function showAll() {
        pinnedFacilityCode = null;
        resetLineHighlights();
    }

    function clearHighlights() {
        pinnedFacilityCode = null;
        resetLineHighlights();
    }

    function filterLegend(e) {
        const query = e.target.value.toLowerCase().trim();
        document.querySelectorAll(".facility-legend-item").forEach(item => {
            const title = item.querySelector(".facility-title").textContent.toLowerCase();
            item.style.display = title.includes(query) ? "flex" : "none";
        });
    }

    return { init, render };
})();
