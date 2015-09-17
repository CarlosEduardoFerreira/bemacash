package com.kaching123.display.actions;

import com.kaching123.display.Action;

/**
 * Created by pkabakov on 25.02.14.
 */
public class InitDisplayAction extends Action {
    private static final byte RESET = 0x1f;
    private static final byte NORMAL_MODE = 0x11;
    private static final byte HIDE_CURSOR = 0x14;

    private static final byte[] COMMAND_BYTES = {RESET,NORMAL_MODE, HIDE_CURSOR};

    @Override
    protected byte[] getCommand() {
        return COMMAND_BYTES;
    }

}
