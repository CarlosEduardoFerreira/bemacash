package com.kaching123.tcr.model;

import java.math.BigDecimal;

/**
 * Created by mboychenko on 5/25/2017.
 */

public class TaxGroupSale {
    public String taxGroupGuid;
    public String taxTitle;
    public BigDecimal totalSales;

    public TaxGroupSale(String taxGroupGuid, String taxTitle, BigDecimal totalSales){
        this.taxTitle = taxTitle;
        this.totalSales = totalSales;
        this.taxGroupGuid = taxGroupGuid;
    }
}
