package com.kaching123.pos.util;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by vkompaniets on 21.01.14.
 */
public interface IXReportPrinter extends IPrinter{
    public void header(String header);
    public void titledDate(String title, Date date);
    public void pair (String name, BigDecimal cost, boolean brackets);
    public void pair (String name, BigDecimal cost);
    public void pair (String left, String right);
    public void percent(BigDecimal percent);
    public void boldPair (String name, BigDecimal cost, boolean brackets);
    public void subtitle (String name, boolean bold);
    public void footer(String footer);
    public void center(String label);
}
