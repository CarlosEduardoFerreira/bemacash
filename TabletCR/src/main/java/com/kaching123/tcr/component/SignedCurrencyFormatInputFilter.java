package com.kaching123.tcr.component;

/**
 * Created by pkabakov on 09.01.14.
 */
public class SignedCurrencyFormatInputFilter extends RegexpFormatInputFilter{

    //private static final String REGEXP = "(^(\\d{0,7}(,)?)?(\\.[0-9]{0,2})?-?$)|(^(-?\\d{0,7})?(\\.[0-9]{0,2})?$)";
    private static final String REGEXP = "(^(\\d{0,7}(,)?){0,3}(\\.[0-9]{0,2})?-?$)|(^(-?\\d{0,7})?(\\.[0-9]{0,2})?$)";
    public SignedCurrencyFormatInputFilter() {
        super(REGEXP);
    }
}
