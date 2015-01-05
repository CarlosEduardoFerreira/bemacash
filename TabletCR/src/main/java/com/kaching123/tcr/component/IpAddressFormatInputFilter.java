package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class IpAddressFormatInputFilter extends RegexpFormatInputFilter {

    private static final String REGEXP = "^(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)?(\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)?){0,3}$";

    public IpAddressFormatInputFilter() {
        super(REGEXP);
    }
}
