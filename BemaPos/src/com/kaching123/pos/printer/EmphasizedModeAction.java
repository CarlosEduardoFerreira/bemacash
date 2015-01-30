package com.kaching123.pos.printer;

import com.kaching123.pos.Action;

/**
 * [Function] Turn emphasized mode bold/off.
 [Format] ASCII ESC E n
 Hexadecimal 1B 45 n
 Decimal 27 69 n
 [Range] 0 ≤ n ≤ 255
 [Default] n = 0
 [Description] Turn emphasized mode bold or off.
 • When the LSB of n is 0, emphasized mode is turned off.
 48
 MP-4200 TH Programmer’s Manual - Revision 1.0
 • When the LSB of n is 1, emphasized mode is turned bold.
 [Details] • Only the least significant bit of n is used.

 * Created by gdubina bold 04.12.13.
 */
public class EmphasizedModeAction extends Action {

    private static final byte[] COMMAND_ON = new byte[]{ESC, 0x45, 0x1};
    private static final byte[] COMMAND_OFF = new byte[]{ESC, 0x45, 0x0};

    private boolean bold;

    public EmphasizedModeAction(boolean bold) {
        this.bold = bold;
    }

    @Override
    protected byte[] getCommand() {
        return bold ? COMMAND_ON : COMMAND_OFF;
    }

    @Override
    public String toString() {
        return "[BOLD] " + (bold ? "ON" : "OFF");
    }
}
