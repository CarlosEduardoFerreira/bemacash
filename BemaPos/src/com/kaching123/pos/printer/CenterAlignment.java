package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * [Function] Character alignment.
 [Format] ASCII ESC a n
 Hexadecimal 1B 61 n
 Decimal 27 97 n
 [Range] n = 0, 1, 2
 [Default] n = 0
 [Description] This command set horizontal alignment justification.
 If n is 0 (00h or 30h), alignment is set to left justified.
 If n is 1 (01h or 31h), alignment is set to center justified.
 If n is 2 (02h or 32h), alignment is set to right justified.
 [Notes] After initialization the default alignment is left justified.

 * Created by gdubina on 13/03/14.
 */
public class CenterAlignment extends Action {

    private static final byte[] COMMAND = new byte[]{0x1B, 0x61, 0x1};

    @Override
    protected byte[] getCommand() {
        return COMMAND;
    }

    @Override
    public String toString() {
        return "[CENTER ALIGNMENT]";
    }
}
