package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class PercentFormatInputFilter extends RegexpFormatInputFilter{

    private static final String REGEXP = "^(\\d{0,2})?(\\.[0-9]{0,2})?$";

    public PercentFormatInputFilter() {
        super(REGEXP);
    }
}