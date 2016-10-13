package com.kaching123.pos.util;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by vkompaniets on 13.01.14.
 */
public interface ISignaturePrinter extends IHeaderFooterPrinter {

    void date(Date date);

    void cardName(String cardName);

    void authNumber(String authNumber);

    void shiftedNumber(String shiftedNumber);

    void entryMethod(String entryMethod);

    void approvalNumber(String approval);

    void aidNumber(String aid);

    void arqcNumber(String arqc);

    void addWithTab(String left, String right);

    void amount(BigDecimal amount);

    void cashBack(BigDecimal amount);

    void total(BigDecimal amount);

    void cropLine(String line);

}
