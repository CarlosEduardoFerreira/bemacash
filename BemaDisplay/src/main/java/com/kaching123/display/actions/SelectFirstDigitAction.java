package com.kaching123.display.actions;

import com.kaching123.display.Action;

/**
 * Created by pkabakov on 26.02.14.
 */
public class SelectFirstDigitAction extends Action {

    private static final byte SELECT_DIGIT = 0x10;
    private static final byte FIRST_DIGIT_IDX = 0x00;

    private static final byte E_MOVE_TO_HOME_POSITION = 0x0B;

    private static final byte[] COMMAND_BYTES = {E_MOVE_TO_HOME_POSITION};

    @Override
    protected byte[] getCommand() {
        return COMMAND_BYTES;
    }

}
