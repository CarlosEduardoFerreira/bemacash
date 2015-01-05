package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 [Function] Set and save ESC/POS ideogram mode.
 [Format] ASCII GS F9h 8 n
 Hexadecimal 1D F9 38 n
 Decimal 29 249 56 n
 [Visibility] Public
 [Range] 0 ≤ n ≤ 3
 [Default] n = 0
 [Description] If n is 0 (00h or 30h), UTF8 (Unicode) ideogram mode is selected.
 If n is 1 (01h or 31h), ESC/POS Japanese ideogram mode is selected.
 If n is 2 (02h or 32h), ESC/POS Simplified Chinese ideogram mode selected.
 If n is 3 (03h or 33h), ESC/POS Traditional Chinese ideogram mode is selected.

 * Created by hamsterksu on 04.12.13.
 */
public class SelectPOSUtf8Action extends Action{

    private static final byte[] COMMAND = new byte[]{GS, F9, 0x38, 0x00};

    @Override
    protected byte[] getCommand() {
        return COMMAND;
    }
}
