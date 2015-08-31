package com.bematechus.bemaLibrary;

/**
 * Created by b1107005 on 5/25/2015.
 */
class OpenDrawerCommand  extends PrinterCommand{
    public OpenDrawerCommand (byte pulseWidth)
    {

        commandBuffer = new byte[] { 0x10, 0x14, 0x01, 0x00, 0x00};

        commandBuffer[4] = pulseWidth;
    }
}
