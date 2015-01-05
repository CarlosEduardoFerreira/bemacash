package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class QuantityFormatInputFilter extends RegexpFormatInputFilter{

    private static final String REGEXP = "^(\\d{0,7})?(\\.[0-9]{0,3})?$";

    public QuantityFormatInputFilter() {
        super(REGEXP);
    }
}
