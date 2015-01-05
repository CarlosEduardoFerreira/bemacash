package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class LicensePlateNumberCurrencyFormatInputFilter extends RegexpFormatInputFilter {

    private static final String REGEXP = "^(\\w{0,6})?$";

    public LicensePlateNumberCurrencyFormatInputFilter() {
        super(REGEXP);
    }
}
