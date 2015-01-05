package com.kaching123.tcr.component;

/**
 * Created by pkabakov on 09.01.14.
 */
public class SignedQuantityFormatInputFilter extends RegexpFormatInputFilter{

    private static final String REGEXP = "(^(\\d{0,7})?(\\.[0-9]{0,3})?-?$)|(^(-?\\d{0,7})?(\\.[0-9]{0,3})?$)";

    public SignedQuantityFormatInputFilter() {
        super(REGEXP);
    }
}