package com.bematechus.bemaLibrary.SB8010A;

/**
 * Created by b1107005 on 7/9/2015.
 */
public class CashDrawer {

    private android.cashdrawer.CashDrawer nativeDrawer = null;
    private static CashDrawer instance = null;
    public enum DrawerStatus { DrawerOpen, DrawerClosed };
    private boolean pinHighClosed = true;
    public void setPinHighClosed ()
    {
        pinHighClosed = true;
    }
    public void setPinHighOpen ()
    {
        pinHighClosed = false;
    }


    private CashDrawer()
    {
        nativeDrawer = new android.cashdrawer.CashDrawer();

    }
    static synchronized public CashDrawer getInstance()
    {
        if ( instance == null)
        {
            instance = new CashDrawer();
        }
        return instance;
    }

    synchronized  public void open ()
    {
        nativeDrawer.openCashDrawer();
    }
    synchronized  public DrawerStatus getStatus()
    {
        int i = nativeDrawer.getCashDrawerStatus();
        if ( pinHighClosed)
        {
            if(i == 1 )
                return DrawerStatus.DrawerClosed;
            else
                return DrawerStatus.DrawerOpen;
        }
        else {
            if (i == 0)
                return DrawerStatus.DrawerClosed;
            else
                return DrawerStatus.DrawerOpen;
        }
    }

}
