package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * [Function] Print NV bit image.
 [Format] ASCII FS p n m
 Hexadecimal 1C 70 n m
 Decimal 28 112 n m
 [Range] 1 ≤ n ≤ 255; 0 ≤ m ≤ 3, 48 ≤ m ≤ 51
 [Description] Print n-th NV bit image using mode specified by m.

 * Created by vkompaniets on 27.05.2014.
 */
public class LogoAction extends Action implements Countable {

    @Override
    protected byte[] getCommand() {
        return new byte[]{/*LF,*/ FS, 0x70, 0x01, 0x00};
    }

    @Override
    public String toString() {
        return "[LOGO]";
    }

    @Override
    public int getLineCount() {
        return 3;
    }
}
