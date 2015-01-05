package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class OrderNumberFormatInputFilter extends RegexpFormatInputFilter{

    private static final String REGEXP = "^(\\d{1,3})(-(\\d{0,10}))?$";

    public OrderNumberFormatInputFilter() {
        super(REGEXP);
    }
}
