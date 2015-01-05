package com.kaching123.tcr.component;

/**
 * Created by vkompaniets on 05.06.2014.
 */
public class PortFormatInputFilter extends RegexpFormatInputFilter {
    private static final String REGEXP = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

    public PortFormatInputFilter() {
        super(REGEXP);
    }
}
