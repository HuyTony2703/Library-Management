export const chartTheme = {
    axis: "var(--chart-axis)",
    bar: "var(--chart-bar)",
    grid: "var(--chart-grid)",
    hover: "var(--chart-hover)",
    tooltipBg: "var(--chart-tooltip-bg)",
    tooltipBorder: "var(--chart-tooltip-border)",
    tooltipText: "var(--chart-tooltip-text)"
};

export const chartAxisTick = {
    fill: chartTheme.axis,
    fontSize: 13,
    fontWeight: 700
};

export const chartTooltipStyle = {
    background: chartTheme.tooltipBg,
    border: `1px solid ${chartTheme.tooltipBorder}`,
    borderRadius: 14,
    color: chartTheme.tooltipText,
    boxShadow: "var(--chart-tooltip-shadow)"
};

export const chartTooltipLabelStyle = {
    color: chartTheme.tooltipText,
    fontWeight: 900
};

export const chartTooltipItemStyle = {
    color: chartTheme.tooltipText,
    fontWeight: 800
};
