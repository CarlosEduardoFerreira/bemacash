package com.kaching123.display.actions;

import com.kaching123.display.Action;

/**
 * Created by long.jiao on 7/23/2015.
 */
public class ResetDisplay extends Action {

    private static final byte[] RESET_BYTES_SERIAL = {0x0C};
    private static final byte[] RESET_BYTES = {0x1F};
    private boolean isSerial;
    public ResetDisplay(boolean isSerial){
        this.isSerial = isSerial;
    }

    @Override
    protected byte[] getCommand() {
        if (isSerial)
            return RESET_BYTES_SERIAL;
        return RESET_BYTES;
    }

}
