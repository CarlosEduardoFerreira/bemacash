package com.kaching123.tcr.component;

/**
 * Created by azablotskiy on 04-Jun-15.
 */
public class UnsignedQuantityFormatInputFilter extends RegexpFormatInputFilter {

    private static final String REGEXP = "^(\\d{1,2}\\.?)?(\\d{1,4})?";
//    private static final String REGEXP = "^(\\d{0,7}(.)*)?";

    public UnsignedQuantityFormatInputFilter() {
        super(REGEXP);
    }

}
