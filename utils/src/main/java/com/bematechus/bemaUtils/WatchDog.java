package com.bematechus.bemaUtils;

/**
 * Created by b1107005 on 5/23/2015.
 */
public class WatchDog {
    private Long timeOut = 0L;
    public WatchDog ()
    {

    }
    public void Start ( Long timeOut)
    {
        this.timeOut = timeOut+ System.currentTimeMillis();
    }
    public boolean isTimeOut ()
    {
        return ( System.currentTimeMillis() > timeOut);
    }
}
