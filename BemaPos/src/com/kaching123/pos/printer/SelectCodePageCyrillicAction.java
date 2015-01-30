package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 [Function] Select character code table.
 [Format] ASCII ESC t n
 Hexadecimal 1B 74 n
 Decimal 27 116 n
 [Range] n = 0, 2, 3, 17, 19, 22
 [Default] n = 2
 [Description] Selects a page n from the character code table.

 * Created by gdubina on 04.12.13.
 */
public class SelectCodePageCyrillicAction extends Action {

    private static final byte[] COMMAND = new byte[]{ESC, 0x74, _byte((char)17)};

    public SelectCodePageCyrillicAction() {
    }

    @Override
    protected byte[] getCommand() {
        return COMMAND;
    }
}
