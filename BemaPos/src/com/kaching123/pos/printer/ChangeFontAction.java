package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 [Function] Select character font.
 [Format] ASCII ESC M n
 Hexadecimal 1B 4D n
 Decimal 27 77 n
 [Range] n = 0, 1, 48, 49
 [Description] Select a character font.

 * Created by gdubina on 04.12.13.
 */
public class ChangeFontAction extends Action {

    private static final byte[] COMMAND_C = new byte[]{ESC, 0x4d,0x0};
    private static final byte[] COMMAND_D = new byte[]{ESC, 0x4d, 0x1};

    private boolean d;

    public ChangeFontAction(boolean d) {
        this.d = d;
    }

    @Override
    protected byte[] getCommand() {
        return d ? COMMAND_D : COMMAND_C;
    }
}
