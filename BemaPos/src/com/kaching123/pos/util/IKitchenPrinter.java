package com.kaching123.pos.util;

import java.math.BigDecimal;

/**
 * Created by vkompaniets on 14.02.14.
 */
public interface IKitchenPrinter extends IPrinter {
    public void header(String shopName, String registerTitle, String orderNumLabel, int orderSeqNum, String operatorLabel, String operatorName, String stationLabel, String station, String orderHolder, String orderTitle);
    public void add(BigDecimal qty, String description);
    public void addModifier(String description);
    public void addAddsOn(String description);
    public void tabbed(String label, String value);

    void center(String message);
}
