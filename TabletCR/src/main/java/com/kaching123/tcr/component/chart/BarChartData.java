package com.kaching123.tcr.component.chart;

/**
 * Created by pkabakov on 20.01.14.
 */
public abstract class BarChartData<T> {
    final BarData<T>[] data;

    public BarChartData(BarData<T>[] data) {
        this.data = data;
    }
}
