package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * Created by gdubina on 04/03/14.
 * [Function] Print barcode.
 * [Format] ASCII (1) GS k m d1 ... dk NUL
 * (2) GS k m n d1 ... dn
 * Hexadecimal (1) 1D 6B m d1 ... dk 00
 * (2) 1D 6B m n d1 ... dn
 * Decimal (1) 29 107 m d1 ... dk 0
 * (2) 29 107 m n d1 ... dn
 * [Range] (1) 0 ≤ m ≤ 6 (k and d depends on the barcode system used)
 * (2) 65 ≤ m ≤ 73 (n and d depends on the barcode system used)
 * [Description] Selects a bar code system and prints the bar code.
 * <p/>
 * lets use variant #2 for printing code93
 */
public class BarcodeAction extends Action implements Countable {

    private byte bytes[];
    private String barCode;

    public BarcodeAction(String barCode) {
        this.barCode = barCode;
        bytes = new byte[barCode.length() + 4];
        bytes[0] = GS;
        bytes[1] = 0x6B;
        bytes[2] = 0x48;//code93
        bytes[3] = _byte((char) barCode.length());//n - length
        int offset = 4;
        for (int i = 0; i < barCode.length(); i++) {
            bytes[i + offset] = _byte(barCode.charAt(i));
        }
    }

    @Override
    protected byte[] getCommand() {
        return bytes;
    }

    @Override
    public String toString() {
        return "[BARCODE]" + barCode;
    }

    @Override
    public int getLineCount() {
        return 4;
    }
}
