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
    public void entryMethod(String entryMethod);
    public void approvalNumber(String approval);
    public void aidNumber(String aid);
    public void arqcNumber(String arqc);
    public void addWithTab(String left, String right);
    public void amount(BigDecimal amount);
    public void cashBack(BigDecimal amount);
    public void total(BigDecimal amount);
    public void cropLine(String line);

}
