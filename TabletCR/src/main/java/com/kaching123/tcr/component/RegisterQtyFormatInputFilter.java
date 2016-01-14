package com.kaching123.tcr.component;

/**
 * Created by azablotskiy on 05-Jun-15.
 */
public class RegisterQtyFormatInputFilter extends RegexpFormatInputFilter {

//    private static final String REGEXP = "^(\\d{0,6}(\\.)*)?(\\,{1}[0-9]{1,3})?$";
    private static final String REGEXP = "^(\\d{0,6}(.)*)?(\\,[0-9]{1,3})?$";
//    private static final String REGEXP = "^(\\d{0,7}(.)*)?(\\,[0-9]{1,2})?$";

    public RegisterQtyFormatInputFilter() {
        super(REGEXP);
    }

}
