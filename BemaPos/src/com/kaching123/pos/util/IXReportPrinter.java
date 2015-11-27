package com.kaching123.pos.util;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by vkompaniets on 21.01.14.
 */
public interface IXReportPrinter extends IPrinter {
    void header(String header);
    void titledDate(String title, Date date);
    void pair(String name, BigDecimal cost, boolean brackets);
    void pair(String name, BigDecimal cost);
    void pair(String left, String right);
    void percent(BigDecimal percent);
    void boldPair(String name, BigDecimal cost, boolean brackets);
    void subtitle(String name, boolean bold);
    void subPair (String name, BigDecimal cost, int tabSize, boolean bold);
    void footer(String footer);
    void center(String label);
}
