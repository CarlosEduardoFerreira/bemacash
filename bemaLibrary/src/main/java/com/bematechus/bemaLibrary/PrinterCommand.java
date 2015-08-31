package com.bematechus.bemaLibrary;

/**
 * Created by b1107005 on 5/24/2015.
 */
class PrinterCommand {
    protected byte [] commandBuffer = null;

    public byte[] getBytes ()
    {
        return commandBuffer.clone();
    }
    
    

}
