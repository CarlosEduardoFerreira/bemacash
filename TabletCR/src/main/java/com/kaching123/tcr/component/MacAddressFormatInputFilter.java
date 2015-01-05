package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class MacAddressFormatInputFilter extends RegexpFormatInputFilter {

    private static final String REGEXP = "^([0-9A-F]{0,2})?(:[0-9A-F]{0,2}){0,5}$";

    public MacAddressFormatInputFilter() {
        super(REGEXP);
    }
}
