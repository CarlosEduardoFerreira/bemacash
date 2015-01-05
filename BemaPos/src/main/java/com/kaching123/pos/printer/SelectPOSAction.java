package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 [Function] Select printer operating mode.
 [Format] ASCII GS F9h 5 n
 Hexadecimal 1D F9 35 n
 Decimal 29 249 53 n
 [Range] n = 0; n = 1; n = 48; n = 49;
 [Default] n = 0
 [Description] If n is 0 (00h or 30h), ESC/Bema is selected.
 If n is 1 (01h or 31h), ESC/POS is selected.

 * Created by gdubina on 04.12.13.
 */
public class SelectPOSAction extends Action{

    private static final byte[] COMMAND = new byte[]{GS, F9, 0x35, 0x1};

    @Override
    public byte[] getCommand() {
        return COMMAND;
    }
}
