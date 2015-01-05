package com.kaching123.tcr.component.chart;

/**
 * Created by pkabakov on 20.01.14.
 */

import java.math.BigDecimal;

public abstract class BarData<T> {
    protected final T description;
    protected final BigDecimal amount;
    protected float scale;

    protected BarData(T description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}