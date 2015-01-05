package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 [Function] Turn on/off double-strike mode.
 [Format] ASCII ESC G n
 Hexadecimal 1B 47 n
 Decimal 27 71 n
 [Range] 0 ≤ n ≤ 255
 [Default] n = 0
 [Description] Turn double-strike mode on or off.
 • When the LSB of n is 0, double-strike mode is turned off.
 • When the LSB of n is 1, double-strike mode is turned on.

 * Created by gdubina on 04.12.13.
 */
public class DoubleStrikeModeAction extends Action{

    private static final byte[] COMMAND_ON = new byte[]{ESC, 47, 0x1};
    private static final byte[] COMMAND_OFF = new byte[]{ESC, 47, 0x0};

    private boolean on;

    public DoubleStrikeModeAction(boolean on) {
        this.on = on;
    }

    @Override
    protected byte[] getCommand() {
        return on ? COMMAND_ON : COMMAND_OFF;
    }
}
