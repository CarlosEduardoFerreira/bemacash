package com.kaching123.pos.util;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by pkabakov on 23.12.13.
 */
public interface ITextPrinter extends IHeaderFooterPrinter{

    public void add(String title, boolean bold, boolean crop);
    public void addAddsOn(String title, BigDecimal price);
    public void addCashBack(String title, BigDecimal price);
    public void add(String title, BigDecimal qty, BigDecimal price, List<String> units);
    public void addItemDiscount(String title, BigDecimal discoutn);
    public void addWithTab(String left, String right, boolean fixedLeft, boolean bold);
    public void addWithTab2(String left, String right, boolean fixedLeft, boolean bold);

    public void orderFooter(String label, BigDecimal price);
    public void orderFooter(String label, BigDecimal price, boolean bold);
    public void powerBy(String label);
    public void payment(String cardName, BigDecimal amount);
    public void add(String content);
    public void addNotes(String notes);
    public void change(String cardName, BigDecimal amount);
}
