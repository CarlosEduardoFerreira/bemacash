package com.kaching123.pos.util;

import java.util.Date;

/**
 * Created by vkompaniets on 14.01.14.
 */
public interface IHeaderFooterPrinter extends IPrinter {

    public void header(String message);
    public void header(String guest, String message);
    public void header(String orderPrefix, String registerTitle, int orderSeqNum, Date date, String operatorName);
    public void footer(String label);
    public void footer(String label, boolean bold);
    public void lotoTitle(String label);
    public void lotoBody(String label);


    public void subTitle(String string);
}
