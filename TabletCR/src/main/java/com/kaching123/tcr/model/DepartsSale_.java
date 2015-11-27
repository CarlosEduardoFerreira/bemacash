package com.kaching123.tcr.model;

import java.math.BigDecimal;

/**
 * Created by Teli on 2015/8/7.
 */
public class DepartsSale_ {
    public String departTitle;
    public BigDecimal sales;
    public DepartsSale_(String departTitle, BigDecimal sales){
        this.departTitle = departTitle;
        this.sales = sales;
    }
}
