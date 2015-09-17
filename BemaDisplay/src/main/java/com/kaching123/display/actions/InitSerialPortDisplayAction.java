package com.kaching123.display.actions;

import com.kaching123.display.Action;

/**
 * Created by pkabakov on 25.02.14.
 */
public class InitSerialPortDisplayAction extends Action {

    private static final byte E_0x1B = 0x1B;
    private static final byte E_0x40 = 0x40;

    private static final byte RESET = 0x1f;
    private static final byte NORMAL_MODE = 0x11;
    private static final byte HIDE_CURSOR = 0x14;

    private static final byte[] COMMAND_BYTES = {NORMAL_MODE, HIDE_CURSOR};

    private static final byte[] INTEGRATED_COMMAND_BYTES = {E_0x1B, E_0x40};

    private boolean isIntegrated;

    public InitSerialPortDisplayAction(boolean isIntegrated){
        this.isIntegrated = isIntegrated;
    }

    @Override
    protected byte[] getCommand() {
        if (isIntegrated)
            return INTEGRATED_COMMAND_BYTES;
        else {
            return COMMAND_BYTES;
        }
    }

}
