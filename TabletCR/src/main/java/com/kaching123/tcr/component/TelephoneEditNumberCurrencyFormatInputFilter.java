package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class TelephoneEditNumberCurrencyFormatInputFilter extends RegexpFormatInputFilter {

    private static final String REGEXP = "^(\\d{0,22})?(\\.[0-9]{0,2})?$";

    public TelephoneEditNumberCurrencyFormatInputFilter() {
        super(REGEXP);
    }
}
