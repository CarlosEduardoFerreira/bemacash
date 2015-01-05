package com.kaching123.tcr.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by vkompaniets on 21.01.14.
 */
public class XReportInfo {

    public Date begin;
    public Date end;

    public BigDecimal grossSales;
    public BigDecimal discounts;
    public BigDecimal returns;
    public BigDecimal netSales;
    public BigDecimal gratuity;
    public BigDecimal tax;
    public BigDecimal totalTender;
    public BigDecimal cogs;
    public BigDecimal grossMargin;
    public BigDecimal tenderCreditCard;
    public BigDecimal tenderCash;
    public BigDecimal tenderCreditReceipt;
    public BigDecimal tenderOfflineCredit;
    public BigDecimal tenderCheck;
    public BigDecimal tenderEbtCash;
    public BigDecimal tenderEbtFoodstamp;
    public BigDecimal tenderDebit;
    public BigDecimal drawerDifference;
    public BigDecimal transactionFee;
    /*public BigDecimal ccAmex;
    public BigDecimal ccVisa;
    public BigDecimal ccMasterCard;*/

    public HashMap<String, BigDecimal> cards;

    public BigDecimal grossMarginPercent;

    public XReportInfo(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
    }

    public XReportInfo(Date begin, Date end, BigDecimal grossSales, BigDecimal discounts, BigDecimal returns, BigDecimal netSales, BigDecimal gratuity, BigDecimal tax, BigDecimal totalTender, BigDecimal cogs, BigDecimal grossMargin, BigDecimal grossMarginPercent, BigDecimal tenderCreditCard, BigDecimal tenderCash, BigDecimal tenderCreditReceipt, BigDecimal tenderOfflineCredit, BigDecimal tenderCheck, BigDecimal tenderEbtCash, BigDecimal tenderEbtFoodstamp, BigDecimal tenderDebit, HashMap<String, BigDecimal> cards, BigDecimal drawerDifference, BigDecimal transactionFee) {
        this.begin = begin;
        this.end = end;
        this.grossSales = grossSales;
        this.discounts = discounts;
        this.returns = returns;
        this.netSales = netSales;
        this.gratuity = gratuity;
        this.tax = tax;
        this.totalTender = totalTender;
        this.cogs = cogs;
        this.grossMargin = grossMargin;
        this.tenderCreditCard = tenderCreditCard;
        this.tenderCash = tenderCash;
        this.tenderCreditReceipt = tenderCreditReceipt;
        this.tenderOfflineCredit = tenderOfflineCredit;
        this.tenderCheck = tenderCheck;
        this.tenderEbtCash = tenderEbtCash;
        this.tenderEbtFoodstamp = tenderEbtFoodstamp;
        this.tenderDebit = tenderDebit;
        this.cards = cards;
        this.grossMarginPercent = grossMarginPercent;
        this.drawerDifference = drawerDifference;
        this.transactionFee = transactionFee;
    }
}
