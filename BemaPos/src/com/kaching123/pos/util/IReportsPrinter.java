package com.kaching123.pos.util;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by vkompaniets on 24.01.14.
 */

public interface IReportsPrinter extends IHeaderFooterPrinter {

    public void startBody();
    public void add(String title);
    public void add(String title, BigDecimal qty, BigDecimal price);
    public void add(String title, BigDecimal value);
    public void addComments(String title, String comment);
    public void add(Date clockIn, Date clockOut, boolean isSameDay);
    public void add(Date date, String title, BigDecimal qty);
    public void add(String title, BigDecimal onHand, BigDecimal unitCost, BigDecimal totalCost, boolean active);
    public void add(String title, String ean, String productCode, BigDecimal onHand, BigDecimal unitCost, BigDecimal totalCost, boolean active);
    public void add5Columns(String title, String ean, String productCode, BigDecimal qty, BigDecimal revenue);
    public void add4Columns(String title, BigDecimal qty1, BigDecimal qty2, BigDecimal qty3);
    public void addHourly(String title, BigDecimal value);
    public void addBold(String title, BigDecimal value);
    public void addBold(String title);
    public void addWithTab(String title, BigDecimal value);
    public void addWithTab(String left, String right, boolean bold);
    public void addShiftHrs(String label, String hrs);
    public void endBody();

    public void total(String label, BigDecimal total);
    public void total2(String label, BigDecimal total);

    public void dateRange(String label, Date start, Date end);
    public void time(String label, Date time);
    public void subHeader(String itemLabel, String qtyLabel, String revenueLabel);
    public void subHeader2(String dateLabel, String titleLabel, String qtyLabel);
    public void subHeader(String itemLabel, String revenueLabel);
    public void subHeader4Columns(String col1, String col2, String col3, String col4);
    public void subHeader5Columns(String col1, String col2, String col3, String col4, String col5);
    public void subHeader7Columns(String col1, String col2, String col3, String col4, String col5, String col6, String col7);

}
