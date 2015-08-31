package com.kaching123.tcr.model;

import java.math.BigDecimal;

/**
 * Created by Teli on 2015/8/7.
 */
public class departsSale {
    public String departTitle;
    public BigDecimal sales;
    public departsSale(String departTitle, BigDecimal sales){
        this.departTitle = departTitle;
        this.sales = sales;
    }
}
