package com.grocery.pos.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ChartDataDto {
    private List<String> salesDates = new ArrayList<>();
    private List<BigDecimal> salesTotals = new ArrayList<>();
    private List<String> categoryLabels = new ArrayList<>();
    private List<Integer> categoryCounts = new ArrayList<>();
    private List<String> categoryColors = new ArrayList<>();

    public ChartDataDto() {
    }

    public List<String> getSalesDates() {
        return salesDates;
    }

    public void setSalesDates(List<String> salesDates) {
        this.salesDates = salesDates;
    }

    public List<BigDecimal> getSalesTotals() {
        return salesTotals;
    }

    public void setSalesTotals(List<BigDecimal> salesTotals) {
        this.salesTotals = salesTotals;
    }

    public List<String> getCategoryLabels() {
        return categoryLabels;
    }

    public void setCategoryLabels(List<String> categoryLabels) {
        this.categoryLabels = categoryLabels;
    }

    public List<Integer> getCategoryCounts() {
        return categoryCounts;
    }

    public void setCategoryCounts(List<Integer> categoryCounts) {
        this.categoryCounts = categoryCounts;
    }

    public List<String> getCategoryColors() {
        return categoryColors;
    }

    public void setCategoryColors(List<String> categoryColors) {
        this.categoryColors = categoryColors;
    }
}
