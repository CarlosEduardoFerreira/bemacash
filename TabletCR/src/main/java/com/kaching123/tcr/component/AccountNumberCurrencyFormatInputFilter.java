package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class AccountNumberCurrencyFormatInputFilter extends RegexpFormatInputFilter {

    private static final String REGEXP = "^(\\d{0,11})?(\\.[0-9]{0,2})?$";
    private static final String REGAEXP_BEFORE = "^(\\d{";
    private static final String REGAEXP_MIDDLE = ",";
    private static final String REGAEXP_AFTER = "})?$";

    public AccountNumberCurrencyFormatInputFilter(String min, String max) {
        super(REGAEXP_BEFORE + "0" + REGAEXP_MIDDLE + max + REGAEXP_AFTER);
    }


}
