package com.bematechus.bemaLibrary;

/**
 * Created by b1107005 on 5/25/2015.
 */

/*

        Bitmap
        Bit 0     -> 0 offline, 1 online
        Bit 1     -> drawer pin sensor (low/high).
        Bit 2     -> 0 cover is closed, 1 cover is open
        Bit 3     -> 0 cutter ok, 1 cutter error
        Bit 4     -> 0 paper ok, 1 paper end
     */
public class PrinterStatus {

    private byte bitmap = 0;
    private PrinterStatus ()
    {

    }
    PrinterStatus (byte bitmap)
    {
        this.bitmap = bitmap;
    }
    public boolean isOnline ()
    {
        return (( bitmap & 0x01)>0);
    }
    public boolean isDrawerSensorHigh ()
    {
        return (( bitmap & 0x02)>0);
    }
    public boolean isCoverOpen ()
    {
        return (( bitmap & 0x04)>0);
    }
    public boolean isCutterOk()
    {
        return (( bitmap & 0x08)==0);
    }
    public boolean isPaperOk()
    {
        return (( bitmap & 0x10)==0);
    }
}
