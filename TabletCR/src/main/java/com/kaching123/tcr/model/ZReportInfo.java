package com.kaching123.tcr.model;

import com.kaching123.tcr.reports.SalesByItemsReportQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by alboyko on 26.11.2015
 */
public class ZReportInfo extends XReportInfo {

    public BigDecimal salesCounter;
    public BigDecimal voidsCounter;
    public BigDecimal refundsCounter;

    public ZReportInfo(Date begin,
                       Date end,
                       BigDecimal grossSales,
                       BigDecimal discounts,
                       BigDecimal returns,
                       BigDecimal netSales,
                       BigDecimal gratuity,
                       BigDecimal tax,
                       BigDecimal totalTender,
                       BigDecimal cogs,
                       BigDecimal grossMargin,
                       BigDecimal grossMarginPercent,
                       BigDecimal tenderCreditCard,
                       BigDecimal tenderCash,
                       BigDecimal tenderCreditReceipt,
                       BigDecimal tenderOfflineCredit,
                       BigDecimal tenderCheck,
                       BigDecimal tenderEbtCash,
                       BigDecimal tenderEbtFoodstamp,
                       BigDecimal tenderDebit,
                       HashMap<String, BigDecimal> cards,
                       BigDecimal drawerDifference,
                       BigDecimal transactionFee,
                       BigDecimal openAmount,
                       BigDecimal cashSale,
                       BigDecimal safeDrops,
                       BigDecimal payOuts,
                       BigDecimal cashBack,
                       TreeMap<String, DepartsSale> departsSales,
                       ArrayList<TaxGroupSale> taxSales,
                       ArrayList<SalesByItemsReportQuery.ReportItemInfo> itemSales,
                       BigDecimal totalValue,
                       BigDecimal salesCounter,
                       BigDecimal voidsCounter,
                       BigDecimal refundsCounter) {
        super(begin, end, grossSales, discounts, returns, netSales, gratuity, tax, totalTender, cogs, grossMargin, grossMarginPercent, tenderCreditCard, tenderCash, tenderCreditReceipt, tenderOfflineCredit, tenderCheck, tenderEbtCash, tenderEbtFoodstamp, tenderDebit, cards, drawerDifference, transactionFee, openAmount, cashSale, safeDrops, payOuts, cashBack, departsSales, itemSales, taxSales, totalValue);
        this.salesCounter = salesCounter;
        this.voidsCounter = voidsCounter;
        this.refundsCounter = refundsCounter;
    }
}
