package com.bematechus.bemaLibrary;

/**
 * Created by b1107005 on 5/25/2015.
 */
class DrawerStatusCommand extends PrinterCommand {
    public DrawerStatusCommand ()
    {

        commandBuffer = new byte[] { 0x1d, 0x72, 0x02};


    }
}
