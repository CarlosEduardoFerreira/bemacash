package com.kaching123.display.actions;

import com.kaching123.display.Action;

/**
 * Created by pkabakov on 26.02.14.
 */
public class SelectFirstDigitAction extends Action {

    private static final byte SELECT_DIGIT = 0x10;
    private static final byte FIRST_DIGIT_IDX = 0x00;

    private static final byte E_MOVE_TO_HOME_POSITION = 0x0B;

    private static final byte[] COMMAND_BYTES_SERIAL = {0x1b, 0x40, 0x1b,0x74,0x02,E_MOVE_TO_HOME_POSITION};
    private static final byte[] COMMAND_BYTES = {SELECT_DIGIT,FIRST_DIGIT_IDX};
    private boolean isSerial;
    public SelectFirstDigitAction(boolean isSerial){
        this.isSerial = isSerial;
    }

    @Override
    protected byte[] getCommand() {
        if (isSerial)
            return COMMAND_BYTES_SERIAL;
        return COMMAND_BYTES;
    }

}
