/**
 * AGSI Hierarchical Tree Table View
 * Renders Region > Country > Operator > Facility with expandable nodes and AGSI styling.
 */

const TableView = (() => {
    let treeData = [];
    const expandedState = new Set(); // Stores IDs of expanded nodes

    function init() {
        document.getElementById("expand-all-btn").addEventListener("click", expandAll);
        document.getElementById("collapse-all-btn").addEventListener("click", collapseAll);
        document.getElementById("table-search").addEventListener("input", handleSearch);
    }

    function render(data) {
        treeData = data || [];
        const tbody = document.getElementById("table-body");
        tbody.innerHTML = "";

        if (!treeData.length) {
            tbody.innerHTML = '<tr><td colspan="11" class="loading-cell">No gas storage data found for this date.</td></tr>';
            return;
        }

        // Initially expand Regions (level 0) and Countries (level 1) by default
        if (expandedState.size === 0) {
            treeData.forEach(reg => {
                expandedState.add(reg.id);
                (reg.children || []).forEach(ctry => {
                    expandedState.add(ctry.id);
                });
            });
        }

        const fragment = document.createDocumentFragment();
        treeData.forEach(region => {
            renderNodeRecursive(region, 0, null, fragment);
        });

        tbody.appendChild(fragment);
    }

    function renderNodeRecursive(node, level, parentId, container) {
        const hasChildren = node.children && node.children.length > 0;
        const isExpanded = expandedState.has(node.id);
        const tr = document.createElement("tr");

        tr.id = `row-${node.id}`;
        tr.className = `tree-row tree-level-${level}`;
        if (parentId) {
            tr.setAttribute("data-parent", parentId);
        }
        tr.setAttribute("data-node-id", node.id);
        tr.setAttribute("data-level", level);

        // Status Badge
        const statusClass = node.status === "C" ? "badge-c" : (node.status === "E" ? "badge-e" : "badge-n");
        const statusHtml = node.status ? `<span class="legend-badge ${statusClass}">${node.status}</span>` : "";

        // Name with indentation & toggle button
        const indentPx = level * 20;
        let toggleHtml = "";
        if (hasChildren) {
            toggleHtml = `<span class="toggle-icon ${isExpanded ? 'expanded' : ''}" data-target="${node.id}">${isExpanded ? '−' : '+'}</span>`;
        } else {
            toggleHtml = `<span style="display:inline-block; width:16px;"></span>`;
        }

        let typeBadge = "";
        if (node.type === "facility" && node.facilityType) {
            typeBadge = `<span class="facility-type-tag">${node.facilityType}</span>`;
        }

        // Full % Badge
        let fullHtml = "-";
        if (node.fullPercentage !== null && node.fullPercentage !== undefined) {
            const fullVal = node.fullPercentage;
            let colorCls = "full-level-green";
            if (fullVal < 10) colorCls = "full-level-red";
            else if (fullVal < 30) colorCls = "full-level-orange";
            else if (fullVal < 50) colorCls = "full-level-yellow";
            else if (fullVal < 70) colorCls = "full-level-lime";
            else if (fullVal >= 85) colorCls = "full-level-emerald";

            fullHtml = `<span class="full-pill ${colorCls}">${fullVal.toFixed(2)}%</span>`;
        }

        // Trend
        let trendHtml = "-";
        if (node.trend !== null && node.trend !== undefined) {
            const trendVal = node.trend;
            if (trendVal > 0) {
                trendHtml = `<span class="trend-positive">+${trendVal.toFixed(2)}</span>`;
            } else if (trendVal < 0) {
                trendHtml = `<span class="trend-negative">${trendVal.toFixed(2)}</span>`;
            } else {
                trendHtml = `<span class="trend-neutral">0.00</span>`;
            }
        }

        tr.innerHTML = `
            <td class="col-status">${statusHtml}</td>
            <td class="col-name" style="padding-left: ${indentPx + 8}px;">
                <div class="tree-node-cell">
                    ${toggleHtml}
                    <span class="node-name-text">${escapeHtml(node.name)}</span>
                    ${typeBadge}
                </div>
            </td>
            <td class="col-num">${formatNumber(node.gasInStorage, 2)}</td>
            <td class="col-num">${fullHtml}</td>
            <td class="col-num">${trendHtml}</td>
            <td class="col-num">${formatNumber(node.consumption, 1)}</td>
            <td class="col-num">${node.consumptionFull !== null && node.consumptionFull !== undefined ? node.consumptionFull.toFixed(1) + '%' : '-'}</td>
            <td class="col-num">${formatNumber(node.injection, 1)}</td>
            <td class="col-num">${formatNumber(node.withdrawal, 1)}</td>
            <td class="col-num">${formatNumber(node.workingGasVolume, 2)}</td>
            <td class="col-num">${node.coveredCapacity !== null && node.coveredCapacity !== undefined ? node.coveredCapacity.toFixed(1) + '%' : '-'}</td>
        `;

        // Toggle click handler
        if (hasChildren) {
            const toggleBtn = tr.querySelector(".toggle-icon");
            if (toggleBtn) {
                toggleBtn.addEventListener("click", (e) => {
                    e.stopPropagation();
                    toggleNode(node.id);
                });
            }
        }

        container.appendChild(tr);

        // Recurse children
        if (hasChildren) {
            node.children.forEach(child => {
                renderNodeRecursive(child, level + 1, node.id, container);
            });
        }
    }

    function toggleNode(nodeId) {
        if (expandedState.has(nodeId)) {
            expandedState.delete(nodeId);
        } else {
            expandedState.add(nodeId);
        }
        applyVisibility();
    }

    function applyVisibility() {
        const rows = document.querySelectorAll("#table-body tr.tree-row");
        const visibleParents = new Set(["root"]);

        // First pass: mark which rows should be visible
        treeData.forEach(reg => {
            const regVisible = true;
            setRowDisplay(`row-${reg.id}`, true, expandedState.has(reg.id));

            (reg.children || []).forEach(ctry => {
                const ctryVisible = expandedState.has(reg.id);
                setRowDisplay(`row-${ctry.id}`, ctryVisible, expandedState.has(ctry.id));

                (ctry.children || []).forEach(op => {
                    const opVisible = ctryVisible && expandedState.has(ctry.id);
                    setRowDisplay(`row-${op.id}`, opVisible, expandedState.has(op.id));

                    (op.children || []).forEach(fac => {
                        const facVisible = opVisible && expandedState.has(op.id);
                        setRowDisplay(`row-${fac.id}`, facVisible, false);
                    });
                });
            });
        });
    }

    function setRowDisplay(rowId, isVisible, isExpanded) {
        const el = document.getElementById(rowId);
        if (!el) return;
        el.style.display = isVisible ? "" : "none";
        const toggleIcon = el.querySelector(".toggle-icon");
        if (toggleIcon) {
            toggleIcon.textContent = isExpanded ? '−' : '+';
            if (isExpanded) {
                toggleIcon.classList.add("expanded");
            } else {
                toggleIcon.classList.remove("expanded");
            }
        }
    }

    function expandAll() {
        function collect(nodes) {
            nodes.forEach(n => {
                if (n.children && n.children.length > 0) {
                    expandedState.add(n.id);
                    collect(n.children);
                }
            });
        }
        collect(treeData);
        applyVisibility();
    }

    function collapseAll() {
        expandedState.clear();
        applyVisibility();
    }

    function handleSearch(e) {
        const term = e.target.value.toLowerCase().trim();
        const rows = document.querySelectorAll("#table-body tr.tree-row");
        if (!term) {
            applyVisibility();
            return;
        }

        // Show matching rows and their ancestor path
        rows.forEach(r => {
            const text = r.textContent.toLowerCase();
            if (text.includes(term)) {
                r.style.display = "";
            } else {
                r.style.display = "none";
            }
        });
    }

    function formatNumber(val, decimals = 2) {
        if (val === null || val === undefined || isNaN(val)) return "-";
        return val.toLocaleString(undefined, {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        });
    }

    function escapeHtml(str) {
        if (!str) return "";
        return str.replace(/[&<>'"]/g, tag => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            "'": '&#39;',
            '"': '&quot;'
        }[tag] || tag));
    }

    return { init, render, expandAll, collapseAll };
})();
