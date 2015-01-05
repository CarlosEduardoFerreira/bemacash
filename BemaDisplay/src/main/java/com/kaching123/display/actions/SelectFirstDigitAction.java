package com.kaching123.display.actions;

import com.kaching123.display.Action;

/**
 * Created by pkabakov on 26.02.14.
 */
public class SelectFirstDigitAction extends Action {

    private static final byte SELECT_DIGIT = 0x10;
    private static final byte FIRST_DIGIT_IDX = 0x00;

    private static final byte[] COMMAND_BYTES = {SELECT_DIGIT, FIRST_DIGIT_IDX};

    @Override
    protected byte[] getCommand() {
        return COMMAND_BYTES;
    }

}
