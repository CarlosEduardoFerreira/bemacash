package com.kaching123.tcr.component;

/**
 * Created by gdubina on 29.11.13.
 */
public class CurrencyFormatInputFilter extends RegexpFormatInputFilter{

    private static final String REGEXP = "^(\\d{0,3}(,)?){0,3}(\\.[0-9]{0,2})?$";
    //"^(\\d{0,7}(,)*)?(\\.[0-9]{0,2})?$";

    public CurrencyFormatInputFilter() {
        super(REGEXP);
    }
}
