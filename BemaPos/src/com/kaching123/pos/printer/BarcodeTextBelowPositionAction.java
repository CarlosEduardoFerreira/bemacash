package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * Created by gdubina on 04/03/14.

 [Function] Select print position for HRI characters.
 [Format] ASCII GS H n
 Hexadecimal 1D 48 n
 Decimal 29 72 n
 [Range] 0 ≤ n ≤ 3, 48 ≤ n ≤ 51
 [Default] n = 0

 */
public class BarcodeTextBelowPositionAction extends Action {

    private byte bytes[];

    public BarcodeTextBelowPositionAction(){
        bytes = new byte[3];
        bytes[0] = GS;
        bytes[1] = 0x48;
        bytes[2] = _byte((char)2);
    }

    @Override
    protected byte[] getCommand() {
        return bytes;
    }

    @Override
    public String toString() {
        return "BarcodeTextBelowPositionAction";
    }
}
