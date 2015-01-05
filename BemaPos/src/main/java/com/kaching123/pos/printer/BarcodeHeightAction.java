package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * Created by gdubina on 04/03/14.

 [Function] Select barcode height.
 [Format] ASCII GS h n
 Hexadecimal 1D 68 n
 Decimal 29 104 n
 [Range] 1 ≤ n ≤ 255
 [Default] n = 192
 [Description] Select barcode height. n specifies the number of dots in the vertical direction.
 [Reference] GS k

 */
public class BarcodeHeightAction extends Action{

    private byte bytes[];
    private int h;

    public BarcodeHeightAction(int h){
        this.h = h;
        bytes = new byte[3];
        bytes[0] = GS;
        bytes[1] = 0x68;
        bytes[2] = _byte((char)h);
    }

    @Override
    protected byte[] getCommand() {
        return bytes;
    }

    @Override
    public String toString() {
        return "BarcodeHeightAction: " + h;
    }
}
