package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 [Function] Select print mode(s).
 [Format] ASCII ESC ! n
 Hexadecimal 1B 21 n
 Decimal 27 33 n
 [Range] 0 ≤ n ≤ 255
 [Default] n = 0

 * Created by gdubina on 04.12.13.
 */
public class SelectDoublePrintModeAction extends Action {

    private static final byte[] COMMAND_ON = new byte[]{ESC, 0x21, 0x20};
    private static final byte[] COMMAND_OFF = new byte[]{ESC, 0x21, 0x00};

    private boolean on;

    public SelectDoublePrintModeAction(boolean on) {
        this.on = on;
    }

    @Override
    protected byte[] getCommand() {
        return on ? COMMAND_ON : COMMAND_OFF;
    }

    @Override
    public String toString() {
        return "[DOUBLE] " + (on ? "ON" : "OFF");
    }
}
