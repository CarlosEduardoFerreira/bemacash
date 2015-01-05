package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class SunpassCredencialEditNumberCurrencyFormatInputFilter extends RegexpFormatInputFilter {

    private static final String REGEXP = "^(\\d{0,13})?(\\.[0-9]{0,2})?$";

    public SunpassCredencialEditNumberCurrencyFormatInputFilter() {
        super(REGEXP);
    }
}
