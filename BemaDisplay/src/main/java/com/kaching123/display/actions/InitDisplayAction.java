package com.kaching123.display.actions;

import com.kaching123.display.Action;

/**
 * Created by pkabakov on 25.02.14.
 */
public class InitDisplayAction extends Action {

    private static final byte NORMAL_MODE = 0x11;
    private static final byte HIDE_CURSOR = 0x14;

    private static final byte E_0x1B = 0x11;
    private static final byte E_0x40 = 0x14;

    private static final byte[] COMMAND_BYTES = {E_0x1B, E_0x40};

    @Override
    protected byte[] getCommand() {
        return COMMAND_BYTES;
    }

}
