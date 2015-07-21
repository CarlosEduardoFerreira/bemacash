package com.bematechus.bemaLibrary;

/**
 * Created by b1107005 on 5/25/2015.
 */
class PaperCutCommand extends PrinterCommand {
    public PaperCutCommand (byte lines)
    {
        commandBuffer = new byte[] { 0x1d, 0x56, 0x42, 0x00};

        commandBuffer[3] = lines;
    }
}
