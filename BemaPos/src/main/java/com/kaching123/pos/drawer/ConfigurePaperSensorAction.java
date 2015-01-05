package com.kaching123.pos.drawer;

import com.kaching123.pos.Action;

/**
 * [Function] Enable/disable paper near-end sensor.
 * [Format] ASCII GS F9h , n
 * Hexadecimal 1D F9 2C n
 * Decimal 29 249 44 n
 * [Default] n = 1
 * [Description] Enable or disable paper near-end sensor (PNES). This setting is saved to configuration (nonvolatile) memory.
 * n = 1 or 31h – enable PNES.
 * n = 0 or 30h – disable PNES.
 *
 * Created by gdubina on 09/01/14.
 */
public class ConfigurePaperSensorAction extends Action{

    private static final byte[] COMMAND_BYTES = new byte[]{GS, F9, 0x2C, 1};

    @Override
    protected byte[] getCommand() {
        return COMMAND_BYTES;
    }
}
