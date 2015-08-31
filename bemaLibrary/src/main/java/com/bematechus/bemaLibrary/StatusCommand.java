package com.bematechus.bemaLibrary;

/**
 * Created by b1107005 on 5/25/2015.
 */
class StatusCommand extends PrinterCommand {
    public StatusCommand ()
    {

        commandBuffer = new byte[]{0x10, 0x04, 0x01, //printer status
                0x10, 0x04, 0x02, //offline status
                0x10, 0x04, 0x03, //error status
                0x10, 0x04, 0x04}; //paper status


    }
    public int getReturnSize()
    {
        return commandBuffer.length / 3;
    }
}
