package com.kaching123.tcr.countries.costarica;

import com.kaching123.pos.Action;

/**
 * Created by alboyko on 03.10.2016.
 */
/*
* 1B 26 03 24 24 0B
00 00 00 07 FE 00 18 01 F0 20 0F 40
40 F0 02 4F 00 F0 F0 0F 20 40 F0 20
2F 00 40 F8 01 80 06 06 00
1b 25 01*/
public class DrawCurrencySignAction extends Action {

    byte[] COMMAND = new byte[]{
            0x1B, 0x26, 0x03, 0x24, 0x24, 0x0B,
            00, 00, 00, 0x07, FE, 0x00, 0x18, 0x01, F0, 0x20, 0x0F, 0x40,
            40, F0, 0x02, 0x4F, 0x00, F0, F0, 0x0F, 0x20, 0x40, F0, 0x20,
            0x2F, 0x00, 0x40, F8, 0x01, (byte)0x80, 0x06, 0x06, 0x00,
            0x1b, 0x25, 0x01,
            0x24, 0x0a, 0x0d
    };

    public DrawCurrencySignAction() {
    }

    @Override
    public byte[] getCommand() {
        return COMMAND;
    }
}

