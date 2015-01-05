package com.kaching123.tcr.model;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 18.02.14.
 */
public class SalesByCustomerModel {
    public String customerName;
    public String customerPhone;
    public String customerEmail;
    public BigDecimal totalAmount = BigDecimal.ZERO;

    public SalesByCustomerModel(String customerName, String customerPhone, String customerEmail) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
    }
}
