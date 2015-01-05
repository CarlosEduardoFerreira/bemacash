package com.kaching123.pos.util;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by vkompaniets on 13.01.14.
 */
public interface ISignaturePrinter extends IHeaderFooterPrinter {

    public void date(Date date);
    public void cardName(String cardName);
    public void authNumber(String authNumber);
    public void shiftedNumber(String shiftedNumber);
    public void addWithTab(String left, String right);
    public void amount(BigDecimal amount);
    public void cropLine(String line);

}
