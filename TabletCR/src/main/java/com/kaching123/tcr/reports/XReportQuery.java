package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Optional;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.activity.DashboardActivity.SalesStatisticsConverter;
import com.kaching123.tcr.activity.DashboardActivity.SalesStatisticsModel;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.print.SaleReportsProcessor;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler2;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleItemInfo;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleOrderInfo;
import com.kaching123.tcr.model.DepartsSale;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.ShiftModel;
import com.kaching123.tcr.model.TipsModel.PaymentType;
import com.kaching123.tcr.model.XReportInfo;
import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopSchema2.XReportView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.XReportView2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.XReportView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionView;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.store.ShopStore.XReportView;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentGateway;
import static com.kaching123.tcr.model.ContentValuesUtil._tipsPaymentType;
import static com.kaching123.tcr.model.ShiftModel.getLastDailyGuid;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;
import static com.kaching123.tcr.util.CalculationUtil.negative;
import static com.kaching123.tcr.util.DateUtils.getEndOfDay;
import static com.kaching123.tcr.util.DateUtils.getStartOfDay;


/**
 * Created by gdubina on 22.01.14.
 */
public class XReportQuery {

    protected static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(XReportView.URI_CONTENT);
    protected static final Uri URI_SALE_ORDER = ShopProvider.contentUri(ShopStore.SaleOrderTable.URI_CONTENT);
    protected static final Uri URI_PAYMENTS = ShopProvider.getContentUri(PaymentTransactionView.URI_CONTENT);
    protected static final Uri URI_SHIFT = ShopProvider.getContentUri(ShiftTable.URI_CONTENT);
    private static final Uri URI_SHIFT_LIMITED = ShopProvider.getContentWithLimitUri(ShiftTable.URI_CONTENT, 1);
    protected static final Uri URI_TIPS = ShopProvider.getContentUri(EmployeeTipsTable.URI_CONTENT);
    protected static final Uri URI_CASH_DRAWER_DATA = ShopProvider.contentUri(ShopStore.CashDrawerMovementTable.URI_CONTENT);
    private static final Uri URI_SALE_ITEM_DEPARTMENT = ShopProvider.getContentUri(ShopStore.SaleItemDeptView.URI_CONTENT);
    private static final Uri URI_DEPARTMENT = ShopProvider.getContentUri(ShopStore.DepartmentTable.URI_CONTENT);
    protected static BigDecimal totalValue = BigDecimal.ZERO;


    protected XReportQuery() {
    }

    protected static SalesByItemsReportQuery createQuery() {
        return new SalesByItemsReportQuery(isSale());
    }

    protected static boolean isSale() {
        return true;
    }

    public static DepartsSale getDepartSale(String departTitle, Cursor c) {
        BigDecimal finalItemPrice = BigDecimal.ZERO;
        BigDecimal itemSubTotal = getSubTotal(_decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.QUANTITY)), BigDecimal.ZERO), _decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.PRICE)), BigDecimal.ZERO));
        if (_bool(c, c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.DISCOUNTABLE))) {
            BigDecimal itemDiscount = CalculationUtil.getItemDiscountValue(_decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.PRICE)), BigDecimal.ZERO), _decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.DISCOUNT)), BigDecimal.ZERO), _enum(DiscountType.class, (c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.DISCOUNT_TYPE))), DiscountType.PERCENT));
            BigDecimal itemSubDiscount = getSubTotal(_decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.QUANTITY)), BigDecimal.ZERO), itemDiscount);
            finalItemPrice = itemSubTotal.subtract(itemSubDiscount);
        }
        BigDecimal tmpOderDiscountVal = CalculationUtil.getDiscountValue(finalItemPrice, _decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleOrderTable.DISCOUNT)), BigDecimal.ZERO), _enum(DiscountType.class, (c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleOrderTable.DISCOUNT_TYPE))), DiscountType.PERCENT));
        BigDecimal tmpOderDiscountPercent = CalculationUtil.getDiscountValueInPercent(finalItemPrice, _decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleOrderTable.DISCOUNT)), BigDecimal.ZERO), _enum(DiscountType.class, (c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleOrderTable.DISCOUNT_TYPE))), DiscountType.PERCENT));

        if (BigDecimal.ZERO.compareTo(tmpOderDiscountVal) != 0 && _bool(c, c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.DISCOUNTABLE))) {
            BigDecimal itemOrderDiscount = CalculationUtil.getDiscountValue(finalItemPrice, tmpOderDiscountPercent, DiscountType.PERCENT);
            finalItemPrice = finalItemPrice.subtract(itemOrderDiscount);
        }

        if (_bool(c, c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.TAXABLE))) {
            BigDecimal itemFinalTax = CalculationUtil.getTaxVatValue(finalItemPrice, _decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.SaleItemTable.TAX)), BigDecimal.ZERO));
            finalItemPrice = finalItemPrice.add(itemFinalTax);
        }

        return new DepartsSale(departTitle, finalItemPrice);
    }

    public static XReportInfo loadXReport(Context context, String shiftGuid) {

        Date startDate = null;
        Date endDate = null;

        BigDecimal openAmount = BigDecimal.ZERO;
        BigDecimal cashSale = BigDecimal.ZERO;
        BigDecimal safeDrops = BigDecimal.ZERO;
        BigDecimal payOuts = BigDecimal.ZERO;
        BigDecimal cashBack = BigDecimal.ZERO;

        TreeMap<String, DepartsSale> departsSales = new TreeMap<String, DepartsSale>();

        Cursor c = ProviderAction.query(URI_SHIFT)
                .where(ShiftTable.GUID + " = ?", shiftGuid)
                .perform(context);

        if (c.moveToNext()) {
            startDate = new Date(c.getLong(c.getColumnIndex(ShiftTable.START_TIME)));
            endDate = new Date(c.getLong(c.getColumnIndex(ShiftTable.END_TIME)));
            openAmount = _decimal(c.getString(c.getColumnIndex(ShiftTable.OPEN_AMOUNT)), BigDecimal.ZERO);
        }
        c.close();

        DepartsSale prepaidTotalSale = new DepartsSale("Prepaid", BigDecimal.ZERO);
        ArrayList<SalesByItemsReportQuery.ReportItemInfo> result = new ArrayList<SalesByItemsReportQuery.ReportItemInfo>(createQuery().getItems(context, startDate.getTime(), shiftGuid, OrderType.PREPAID));
        final ArrayList<SalesByItemsReportQuery.ReportItemInfo> groupedResult = SaleReportsProcessor.getGroupedResult(result, OrderType.PREPAID);
        for (SalesByItemsReportQuery.ReportItemInfo item : groupedResult) {
            prepaidTotalSale.sales = prepaidTotalSale.sales.add(item.revenue);
        }
        departsSales.put(prepaidTotalSale.departTitle, prepaidTotalSale);
        result = new ArrayList<SalesByItemsReportQuery.ReportItemInfo>(createQuery().getItems(context, startDate.getTime(), shiftGuid, OrderType.SALE));
        Collection<SalesByDepartmentsReportQuery.DepartmentStatistics> deps = new SalesByDepartmentsReportQuery().getItems(context, startDate.getTime(), shiftGuid);
        totalValue = BigDecimal.ZERO;
        for (SalesByDepartmentsReportQuery.DepartmentStatistics d : deps) {
            if (d.description != null)
                departsSales.put(d.description, new DepartsSale(d.description, d.revenue));
            d.reset();
            totalValue = totalValue.add(d.revenue);
        }
        c = ProviderAction.query(URI_SALE_ITEM_DEPARTMENT)
                .where(ShopSchema2.SaleItemDeptView2.SaleOrderTable.SHIFT_GUID + " = ?", shiftGuid)
                .where("(" + ShopSchema2.SaleItemDeptView2.PaymentTransactionTable.STATUS + " = ? OR " + ShopSchema2.SaleItemDeptView2.PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                .perform(context);
        Logger.d("tcrtcr: " + c.getCount());
//
//
//        while (c.moveToNext()) {
//            String temp = c.getString(c.getColumnIndex(ShopSchema2.SaleItemDeptView2.DepartmentTable.TITLE));
//            if (temp != null) {
//                if (departsSales.size() == 0) {
//                    DepartsSale tempDepartSale = getDepartSale(temp, c);
//                    departsSales.put(tempDepartSale.departTitle, tempDepartSale);
//                } else if (departsSales.size() != 0) {
//                    if (!departsSales.containsKey(temp)) {
//                        DepartsSale tempDepartSale = getDepartSale(temp, c);
//                        departsSales.put(temp, tempDepartSale);
//                    } else {
//                        DepartsSale tempDepartSale = getDepartSale(temp, c);
//                        departsSales.get(temp).sales = departsSales.get(temp).sales.add(tempDepartSale.sales);
//                    }
//                }
//            }
//        }
//        c.close();

        final StatInfo saleInfo = getShiftOrders(context, shiftGuid, OrderStatus.COMPLETED);
        final StatInfo returnInfo = getShiftOrders(context, shiftGuid, OrderStatus.RETURN);

        BigDecimal grossSale = saleInfo.grossSale;
        BigDecimal discount = saleInfo.discount;
        BigDecimal returned = negative(returnInfo.grossSale.subtract(returnInfo.discount));
        BigDecimal tax = saleInfo.tax.add(returnInfo.tax);//returnInfo is negative
        BigDecimal cogs = saleInfo.cogs.add(returnInfo.cogs);//returnInfo is negative
        BigDecimal transactionFee = saleInfo.transactionFee.add(getShiftOrdersTransactionFee(context, shiftGuid, OrderStatus.COMPLETED));

        Logger.d("[XREPORT]\t=== XReport ===");
        Logger.d("[XREPORT]\tStart date/time:   %s", startDate);
        Logger.d("[XREPORT]\tEnd date/time:     %s", endDate);

        Logger.d("[XREPORT]\tGross Sale:   %s", grossSale);
        Logger.d("[XREPORT]\tTransaction Fee:          %s", transactionFee);
        //Logger.d("[XREPORT]\tGross2:       %s", saleInfo.grossSale2);
        Logger.d("[XREPORT]\tDiscount:     %s", discount);
        Logger.d("[XREPORT]\tReturns:      %s", returned);
        Logger.d("[XREPORT]\t------------------");

        BigDecimal netSale = grossSale.subtract(discount).subtract(returned);

        c = ProviderAction.query(URI_PAYMENTS)
                .projection(PaymentTransactionTable.AMOUNT, PaymentTransactionTable.GATEWAY, PaymentTransactionTable.CARD_NAME, PaymentTransactionView2.EmployeeTipsTable.AMOUNT, PaymentTransactionTable.CASH_BACK)
                .where(PaymentTransactionTable.SHIFT_GUID + " = ?", shiftGuid)
                .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                .perform(context);

        BigDecimal creditCard = BigDecimal.ZERO;
        BigDecimal cash = BigDecimal.ZERO;
        BigDecimal tenderCreditReceipt = BigDecimal.ZERO;
        BigDecimal offlineCredit = BigDecimal.ZERO;
        BigDecimal check = BigDecimal.ZERO;
        BigDecimal ebtCash = BigDecimal.ZERO;
        BigDecimal ebtFoodstamp = BigDecimal.ZERO;
        BigDecimal debit = BigDecimal.ZERO;

        HashMap<String, BigDecimal> cards = new HashMap<String, BigDecimal>();
        while (c.moveToNext()) {
            BigDecimal transactionAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.AMOUNT)), BigDecimal.ZERO);
            BigDecimal transactionTip = _decimal(c.getString(c.getColumnIndex(PaymentTransactionView2.EmployeeTipsTable.AMOUNT)), BigDecimal.ZERO);
            BigDecimal amount = transactionAmount.subtract(transactionTip);
            BigDecimal cashBackAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.CASH_BACK)), BigDecimal.ZERO);
            cashBack = cashBackAmount;
            PaymentGateway gateway = _paymentGateway(c, c.getColumnIndex(PaymentTransactionTable.GATEWAY));
            if (gateway.isTrueCreditCard()) {
                creditCard = creditCard.add(amount);
            } else if (gateway == PaymentGateway.CASH) {
                cash = cash.add(amount);
            } else if (gateway == PaymentGateway.CREDIT) {
                tenderCreditReceipt = tenderCreditReceipt.add(amount);
            } else if (gateway == PaymentGateway.OFFLINE_CREDIT) {
                offlineCredit = offlineCredit.add(amount);
            } else if (gateway == PaymentGateway.CHECK) {
                check = check.add(amount);
            } else if (gateway == PaymentGateway.PAX_EBT_CASH) {
                ebtCash = ebtCash.add(amount);
            } else if (gateway == PaymentGateway.PAX_EBT_FOODSTAMP) {
                ebtFoodstamp = ebtFoodstamp.add(amount);
            } else if (gateway == PaymentGateway.PAX_DEBIT) {
                debit = debit.add(amount);
            }


            if (gateway.isCreditCard()) {
                String card = c.getString(c.getColumnIndex(PaymentTransactionTable.CARD_NAME));
                if (card == null)
                    card = gateway.name();
                BigDecimal value = cards.get(card);
                if (value == null) {
                    value = transactionAmount;
                } else {
                    value = value.add(transactionAmount);
                }
                cards.put(card, value);
            }
        }

        BigDecimal gratuity = BigDecimal.ZERO;
        c = ProviderAction.query(URI_TIPS)
                .projection(EmployeeTipsTable.AMOUNT, EmployeeTipsTable.PAYMENT_TYPE)
                .where(EmployeeTipsTable.SHIFT_ID + " = ?", shiftGuid)
                .perform(context);

        while (c.moveToNext()) {
            BigDecimal amount = _decimal(c, 0, BigDecimal.ZERO);
            PaymentType type = _tipsPaymentType(c, 1);

            if (type == PaymentType.CASH) {
                cash = cash.add(amount);
            } else if (type == PaymentType.CREDIT) {
                creditCard = creditCard.add(amount);
            }
            gratuity = gratuity.add(amount);
        }
        c.close();

        Logger.d("[XREPORT]\tNet Sale:     %s", netSale);
        Logger.d("[XREPORT]\tGratuity:     %s", gratuity);
        Logger.d("[XREPORT]\tTax:          %s", tax);
        Logger.d("[XREPORT]\t------------------");

        BigDecimal totalTender = netSale.add(gratuity).add(tax);
        Logger.d("[XREPORT]\tTotal Tender: %s", totalTender);

        Logger.d("[XREPORT]\tCOGS:         %s", cogs);

        BigDecimal grossMargin = totalTender.subtract(cogs);
        BigDecimal grossMarginInPercent = CalculationUtil.value(CalculationUtil.getDiscountValueInPercent(totalTender, grossMargin, DiscountType.VALUE));
        Logger.d("[XREPORT]");
        Logger.d("[XREPORT]\tGross Margin: %s", grossMargin);
        Logger.d("[XREPORT]\t              %s", grossMarginInPercent);

        Logger.d("[XREPORT]");
        Logger.d("[XREPORT]\tTender Summary");
        Logger.d("[XREPORT]");
        Logger.d("[XREPORT]\tCredit card: %s", creditCard);
        Logger.d("[XREPORT]\tCash:        %s", cash);
        Logger.d("[XREPORT]\tCredit receipt:   %s", tenderCreditReceipt);
        Logger.d("[XREPORT]\tOffline credit:   %s", offlineCredit);
        Logger.d("[XREPORT]\tCheck:   %s", check);
        //Logger.d("[XREPORT]\tAnother:     %s", another);

        Logger.d("[XREPORT]");
        Logger.d("[XREPORT]\tCredit Card Details");
        Logger.d("[XREPORT]");
        for (Entry<String, BigDecimal> e : cards.entrySet()) {
            Logger.d("[XREPORT]\t%s\t%s", e.getKey(), e.getValue());
        }

        final BigDecimal drawerDifference = getDrawerDifference(context, shiftGuid);
        cashSale = cash;
        Cursor cur = ProviderAction.query(URI_CASH_DRAWER_DATA)
                .where(ShopStore.CashDrawerMovementTable.SHIFT_GUID + " = ?", shiftGuid)
                .perform(context);

        while (cur.moveToNext()) {
            if (cur.getInt(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == MovementType.DROP.getValue())
                safeDrops = safeDrops.add(_decimal(cur.getString(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT)), BigDecimal.ZERO));
            else if (cur.getInt(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == MovementType.PAYOUT.getValue())
                payOuts = payOuts.add(_decimal(cur.getString(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT)), BigDecimal.ZERO));
        }
        cur.close();
        return new XReportInfo(startDate, endDate, grossSale, discount, returned, netSale, gratuity, tax, totalTender,
                cogs, grossMargin, grossMarginInPercent, creditCard, cash, tenderCreditReceipt, offlineCredit, check,
                ebtCash, ebtFoodstamp, debit, cards, drawerDifference, transactionFee, openAmount, cashSale, safeDrops,
                payOuts, cashBack, departsSales, result, totalValue);
    }

    public static XReportInfo loadDailySalesXReport(Context context, long registerID, long fromDate, long toDate) {

        final Date startDate = getStartOfDay();
        final Date endDate = getEndOfDay();
        final List<String> guidList = ShiftModel.getDailyGuidList(context);

        BigDecimal grossSale = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal returned = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal transactionFee = BigDecimal.ZERO;
        BigDecimal cogs = BigDecimal.ZERO;
        BigDecimal netSale = BigDecimal.ZERO;
        BigDecimal creditCard = BigDecimal.ZERO;
        BigDecimal cash = BigDecimal.ZERO;
        BigDecimal tenderCreditReceipt = BigDecimal.ZERO;
        BigDecimal offlineCredit = BigDecimal.ZERO;
        BigDecimal check = BigDecimal.ZERO;
        BigDecimal ebtCash = BigDecimal.ZERO;
        BigDecimal ebtFoodstamp = BigDecimal.ZERO;
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal gratuity = BigDecimal.ZERO;
        BigDecimal totalTender = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal grossMarginInPercent = BigDecimal.ZERO;
        BigDecimal drawerDifference = BigDecimal.ZERO;
        BigDecimal openAmount = BigDecimal.ZERO;
        BigDecimal cashSale = BigDecimal.ZERO;
        BigDecimal safeDrops = BigDecimal.ZERO;
        BigDecimal payOuts = BigDecimal.ZERO;
        BigDecimal cashBack = BigDecimal.ZERO;

        TreeMap<String, DepartsSale> departsSales = new TreeMap<String, DepartsSale>();
        ArrayList<SalesByItemsReportQuery.ReportItemInfo> result = new ArrayList<SalesByItemsReportQuery.ReportItemInfo>();
        HashMap<String, BigDecimal> cards = new HashMap<String, BigDecimal>();

        String lastShiftGuid = getLastDailyGuid(context, registerID);
        openAmount = getLastShiftDailyOpenAmount(context, lastShiftGuid);
        transactionFee = transactionFee.add(getDailyOrdersTransactionFee(context, OrderStatus.COMPLETED, registerID, fromDate, toDate));//returnInfo is negative

        for (String guid : guidList) {
            final StatInfo saleInfo = getDailyOrders(context, guid, OrderStatus.COMPLETED, registerID);
            final StatInfo returnInfo = getDailyOrders(context, guid, OrderStatus.RETURN, registerID);

            grossSale = grossSale.add(saleInfo.grossSale);
            Logger.d("||grossSale:" + grossSale);

            discount = discount.add(saleInfo.discount);
            Logger.d("||discount:" + discount);

            returned = returned.add(negative(returnInfo.grossSale.subtract(returnInfo.discount)));
            Logger.d("||returned:" + returned);


            tax = tax.add(saleInfo.tax.add(returnInfo.tax));//returnInfo is negative
            cogs = cogs.add(saleInfo.cogs.add(returnInfo.cogs));//returnInfo is negative

            Cursor c = ProviderAction.query(URI_PAYMENTS)
                    .projection(PaymentTransactionTable.AMOUNT, PaymentTransactionTable.GATEWAY, PaymentTransactionTable.CARD_NAME, PaymentTransactionView2.EmployeeTipsTable.AMOUNT, PaymentTransactionTable.CASH_BACK)
                    .where(PaymentTransactionTable.SHIFT_GUID + " = ?", guid)
                    .where(PaymentTransactionTable.CREATE_TIME + " > ?", startDate.getTime())
                    .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                    .perform(context);

            while (c.moveToNext()) {
                BigDecimal transactionAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.AMOUNT)), BigDecimal.ZERO);
                BigDecimal transactionTip = _decimal(c.getString(c.getColumnIndex(PaymentTransactionView2.EmployeeTipsTable.AMOUNT)), BigDecimal.ZERO);
                BigDecimal amount = transactionAmount.subtract(transactionTip);
                BigDecimal cashBackAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.CASH_BACK)), BigDecimal.ZERO);
                cashBack = cashBack.add(cashBackAmount);
                PaymentGateway gateway = _paymentGateway(c, c.getColumnIndex(PaymentTransactionTable.GATEWAY));
                if (gateway.isTrueCreditCard()) {
                    creditCard = creditCard.add(amount);
                } else if (gateway == PaymentGateway.CASH) {
                    cash = cash.add(amount);
                } else if (gateway == PaymentGateway.CREDIT) {
                    tenderCreditReceipt = tenderCreditReceipt.add(amount);
                } else if (gateway == PaymentGateway.OFFLINE_CREDIT) {
                    offlineCredit = offlineCredit.add(amount);
                } else if (gateway == PaymentGateway.CHECK) {
                    check = check.add(amount);
                } else if (gateway == PaymentGateway.PAX_EBT_CASH) {
                    ebtCash = ebtCash.add(amount);
                } else if (gateway == PaymentGateway.PAX_EBT_FOODSTAMP) {
                    ebtFoodstamp = ebtFoodstamp.add(amount);
                } else if (gateway == PaymentGateway.PAX_DEBIT) {
                    debit = debit.add(amount);
                }

                if (gateway.isCreditCard()) {
                    String card = c.getString(c.getColumnIndex(PaymentTransactionTable.CARD_NAME));
                    if (card == null)
                        card = gateway.name();
                    BigDecimal value = cards.get(card);
                    if (value == null) {
                        value = transactionAmount;
                    } else {
                        value = value.add(transactionAmount);
                    }
                    cards.put(card, value);
                }
            }

            DepartsSale prepaidTotalSale = new DepartsSale("Prepaid", BigDecimal.ZERO);
            result = new ArrayList<SalesByItemsReportQuery.ReportItemInfo>(createQuery().getItems(context, startDate.getTime(), guid, OrderType.PREPAID));
            final ArrayList<SalesByItemsReportQuery.ReportItemInfo> groupedResult = SaleReportsProcessor.getGroupedResult(result, OrderType.PREPAID);
            for (SalesByItemsReportQuery.ReportItemInfo item : groupedResult) {
                prepaidTotalSale.sales = prepaidTotalSale.sales.add(item.revenue);
            }
            departsSales.put(prepaidTotalSale.departTitle, prepaidTotalSale);

            result = new ArrayList<SalesByItemsReportQuery.ReportItemInfo>(createQuery().getItems(context, startDate.getTime(), guid, OrderType.SALE));
            Collection<SalesByDepartmentsReportQuery.DepartmentStatistics> deps = new SalesByDepartmentsReportQuery().getItems(context, startDate.getTime(), guid);
            totalValue = BigDecimal.ZERO;
            for (SalesByDepartmentsReportQuery.DepartmentStatistics d : deps) {
                if (d.description != null)
                    departsSales.put(d.description, new DepartsSale(d.description, d.revenue));
                d.reset();
                totalValue = totalValue.add(d.revenue);
            }

            cashSale = cash;
            c = ProviderAction.query(URI_TIPS)
                    .projection(EmployeeTipsTable.AMOUNT, EmployeeTipsTable.PAYMENT_TYPE)
                    .where(EmployeeTipsTable.CREATE_TIME + " = ?", getStartOfDay().getTime())
                    .where(EmployeeTipsTable.SHIFT_ID + " = ?", guid)
                    .perform(context);

            while (c.moveToNext()) {
                BigDecimal amount = _decimal(c, 0, BigDecimal.ZERO);
                PaymentType type = _tipsPaymentType(c, 1);

                if (type == PaymentType.CASH) {
                    cash = cash.add(amount);
                } else if (type == PaymentType.CREDIT) {
                    creditCard = creditCard.add(amount);
                }
                gratuity = gratuity.add(amount);
            }
            c.close();

            for (Entry<String, BigDecimal> e : cards.entrySet()) {
                Logger.d("[XREPORT]\t%s\t%s", e.getKey(), e.getValue());
            }
            drawerDifference = drawerDifference.add(getDrawerDifference(context, guid));
        }

        netSale = grossSale.subtract(discount).subtract(returned);
        Logger.d("||netSale:" + netSale);

        totalTender = netSale.add(gratuity).add(tax);
        Logger.d("||totalTender:" + totalTender);

        grossMargin = totalTender.subtract(cogs);
        Logger.d("||totalTender:" + totalTender);
        grossMarginInPercent = CalculationUtil.value(CalculationUtil
                .getDiscountValueInPercent(totalTender, grossMargin, DiscountType.VALUE));

        Cursor cur = ProviderAction.query(URI_CASH_DRAWER_DATA)
                .where(ShopStore.CashDrawerMovementTable.SHIFT_GUID + " = ?", lastShiftGuid)
                .perform(context);

        while (cur.moveToNext()) {
            if (cur.getInt(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == MovementType.DROP.getValue())
                safeDrops = safeDrops.add(_decimal(cur.getString(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT)), BigDecimal.ZERO));
            else if (cur.getInt(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == MovementType.PAYOUT.getValue())
                payOuts = payOuts.add(_decimal(cur.getString(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT)), BigDecimal.ZERO));
        }
        cur.close();

        return new XReportInfo(startDate, endDate, grossSale, discount, returned, netSale, gratuity,
                tax, totalTender, cogs, grossMargin, grossMarginInPercent, creditCard, cash,
                tenderCreditReceipt, offlineCredit, check, ebtCash, ebtFoodstamp, debit, cards, drawerDifference, transactionFee,
                openAmount, cashSale, safeDrops, payOuts, cashBack, departsSales, result, totalValue);
    }

    protected static BigDecimal getDailyOrdersTransactionFee(Context context, OrderStatus type, long registerId) {
        Cursor c = ProviderAction.query(URI_SALE_ORDER)
                .projection(ShopStore.SaleOrderTable.TRANSACTION_FEE)
                .where(ShopStore.SaleOrderTable.CREATE_TIME + " > ?", getStartOfDay().getTime())
                .where(ShopStore.SaleOrderTable.REGISTER_ID + " = ?", registerId)
                .where(ShopStore.SaleOrderTable.STATUS + " = ?", type.ordinal())
                .perform(context);

        BigDecimal totalTransactionFee = BigDecimal.ZERO;
        while (c.moveToNext()) {
            totalTransactionFee = _decimal(c, 0, BigDecimal.ZERO);
        }
        c.close();

        return totalTransactionFee;
    }

    protected static BigDecimal getDailyOrdersTransactionFee(Context context, OrderStatus type, long registerId, long fromDate, long toDate) {
        Cursor c = null;
        Query query = ProviderAction.query(URI_SALE_ORDER)
                .projection(ShopStore.SaleOrderTable.TRANSACTION_FEE)
                .where(ShopStore.SaleOrderTable.CREATE_TIME + " > ?", getStartOfDay().getTime());
        if (registerId == 0) {
            c = query.where(ShopStore.SaleOrderTable.STATUS + " = ?", type.ordinal())
                    .perform(context);
        } else {
            c = query.where(ShopStore.SaleOrderTable.REGISTER_ID + " = ?", registerId)
                    .where(ShopStore.SaleOrderTable.STATUS + " = ?", type.ordinal())
                    .perform(context);
        }


        BigDecimal totalTransactionFee = BigDecimal.ZERO;
        while (c.moveToNext()) {
            totalTransactionFee = _decimal(c, 0, BigDecimal.ZERO);
        }
        c.close();

        return totalTransactionFee;
    }

    protected static BigDecimal getLastShiftDailyOpenAmount(Context context, String shiftGuid) {
        BigDecimal openAmount = BigDecimal.ZERO;
        try {
            Cursor c = ProviderAction.query(URI_SHIFT)
                    .where(ShiftTable.GUID + " = ?", shiftGuid)
                    .perform(context);

            if (c.moveToNext()) {
                openAmount = _decimal(c.getString(c.getColumnIndex(ShiftTable.OPEN_AMOUNT)), BigDecimal.ZERO);
            }
            c.close();
        } catch (Exception e) {
            //Log.d("XRepQ", e.getMessage());
        }


        return openAmount;
    }

    protected static BigDecimal getShiftOrdersTransactionFee(Context context, String shiftGuid, OrderStatus type) {
        Cursor c = ProviderAction.query(URI_SALE_ORDER)
                .projection(ShopStore.SaleOrderTable.TRANSACTION_FEE)
                .where(ShopStore.SaleOrderTable.SHIFT_GUID + " = ?", shiftGuid)
                .where(ShopStore.SaleOrderTable.STATUS + " = ?", type.ordinal())
                .perform(context);

        BigDecimal totalTransactionFee = BigDecimal.ZERO;
        while (c.moveToNext()) {
            totalTransactionFee = _decimal(c, 0, BigDecimal.ZERO);
            Logger.d("trace Transaction fee:" + totalTransactionFee);
        }
        c.close();

        return totalTransactionFee;
    }

    protected static BigDecimal getDrawerDifference(Context context, String shiftGuid) {
        Optional<SalesStatisticsModel> salesStatisticsModel = ProviderAction.query(URI_SHIFT)
                .where(ShiftTable.GUID + " = ?", shiftGuid)
                .orderBy(ShopStore.ShiftTable.START_TIME + " DESC")
                .perform(context).toFluentIterable(new SalesStatisticsConverter(context)).get(0);
        if (salesStatisticsModel != null && salesStatisticsModel.isPresent()) {
            return salesStatisticsModel.get().getDrawerDifference();
        }
        return null;
    }

    protected static StatInfo getDailyOrders(Context context, String shiftGuid, OrderStatus type, long registerId) {
        Cursor c = null;
        Query query = ProviderAction.query(URI_SALE_ITEMS)
                .where(SaleOrderTable.SHIFT_GUID + " = ?", shiftGuid)
                .where(SaleOrderTable.CREATE_TIME + " > ?", getStartOfDay().getTime())
                .where(SaleOrderTable.STATUS + " = ?", type.ordinal());

        if (registerId == 0) {
            c = query.perform(context);
        } else {
            c = query.where(SaleOrderTable.REGISTER_ID + " = ?", registerId)
                    .perform(context);
        }

//        Cursor c = ProviderAction.query(URI_SALE_ITEMS)
//                .where(SaleOrderTable.SHIFT_GUID + " = ?", shiftGuid)
//                .where(SaleOrderTable.CREATE_TIME + " > ?", getStartOfDay().getTime())
//                .where(SaleOrderTable.REGISTER_ID + " = ?", registerId)
//                .where(SaleOrderTable.STATUS + " = ?", type.ordinal())
//                .perform(context);
        return getOrders(context, c, shiftGuid, type);
    }

    protected static StatInfo getShiftOrders(Context context, String shiftGuid, OrderStatus type) {
        Cursor c = ProviderAction.query(URI_SALE_ITEMS)
                .where(SaleOrderTable.SHIFT_GUID + " = ?", shiftGuid)
                .where(SaleOrderTable.STATUS + " = ?", type.ordinal())
                .perform(context);
        return getOrders(context, c, shiftGuid, type);
    }

    private static StatInfo getOrders(Context context, Cursor c, String shiftGuid, OrderStatus type) {
        HashMap<String, SaleOrderInfo> ordersInfo = readCursor(c);
        c.close();

        final StatInfo statInfo = new StatInfo();

        Handler2 handler2 = new Handler2() {
            @Override
            public void splitItem(SaleItemInfo item) {

            }

            @Override
            public void handleItem(SaleItemInfo i, BigDecimal itemFinalPrice,
                                   BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
                SaleItemInfo2 i2 = (SaleItemInfo2) i;
                statInfo.grossSale = statInfo.grossSale.add(getSubTotal(i.qty, i.totalPrice));
                statInfo.cogs = statInfo.cogs.add(getSubTotal(i.qty, i2.itemCost));
            }
        };

        for (Entry<String, SaleOrderInfo> e : ordersInfo.entrySet()) {
            SaleOrderCostInfo result = calculate(e.getValue(), handler2);
            statInfo.discount = statInfo.discount.add(result.totalDiscount);
            statInfo.tax = statInfo.tax.add(result.totalTax);
        }
        return statInfo;
    }

    public static SaleOrderCostInfo calculate(SaleOrderInfo value, Handler2 handler2) {
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (SaleItemInfo i : value.map.values()) {
            SaleItemInfo2 i2 = (SaleItemInfo2) i;

            subTotal = subTotal.add(getSubTotal(i2.qty, i2.totalPrice));
            totalDiscount = totalDiscount.add(getSubTotal(i2.qty, i2.finalDiscount));
            totalTax = totalTax.add(getSubTotal(i2.qty, i2.finalTax));

            BigDecimal itemFinalPrice = i2.totalPrice.add(i2.finalTax).subtract(i2.finalDiscount);
            BigDecimal ebtFinalPrice = i2.totalPrice;
            if (handler2 != null) {
                handler2.handleItem(i, itemFinalPrice, i2.finalDiscount, i2.finalTax);
            }
        }
        return new SaleOrderCostInfo(
                subTotal,
                totalDiscount,
                totalTax
        );
    }

    private static class SaleOrderCostInfo {
        BigDecimal subTotalItemTotal;
        BigDecimal totalDiscount;
        BigDecimal totalTax;

        private SaleOrderCostInfo(BigDecimal subTotalItemTotal, BigDecimal totalDiscount, BigDecimal totalTax) {
            this.subTotalItemTotal = subTotalItemTotal;
            this.totalDiscount = totalDiscount;
            this.totalTax = totalTax;
        }
    }
    //public static calculate(

    private static HashMap<String, SaleOrderInfo> readCursor(Cursor c) {
        if (c == null) {
            return new HashMap<>(0);
        }
        HashMap<String, SaleOrderInfo> result = new HashMap<>();
        if (c.moveToFirst()) {
            do {
                String orderGuid = c.getString(c.getColumnIndex(SaleItemTable.ORDER_GUID));
                SaleOrderInfo r = result.get(orderGuid);
                if (r == null) {
                    r = new SaleOrderInfo(_bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.DISCOUNT), BigDecimal.ZERO),
                            _discountType(c, c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE), BigDecimal.ZERO));
                    result.put(orderGuid, r);
                }
                readCursorRow(c, r);
            } while (c.moveToNext());
        }

        return result;
    }

    private static void readCursorRow(Cursor c, SaleOrderInfo result) {
        String saleItemId = c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID));
        String itemGuid = c.getString(c.getColumnIndex(ItemTable.GUID));
        BigDecimal price = _decimal(c, c.getColumnIndex(SaleItemTable.PRICE), BigDecimal.ZERO);
        SaleItemInfo2 value = (SaleItemInfo2) result.map.get(saleItemId);
        if (value == null) {
            value = new SaleItemInfo2(
                    saleItemId,
                    c.getString(c.getColumnIndex(SaleItemTable.ITEM_GUID)),
                    null,
                    c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                    c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)),
                    _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY), BigDecimal.ZERO),
                    price,
                    _bool(c, c.getColumnIndex(SaleItemTable.DISCOUNTABLE)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.DISCOUNT), BigDecimal.ZERO),
                    _discountType(c, c.getColumnIndex(SaleItemTable.DISCOUNT_TYPE)),
                    _bool(c, c.getColumnIndex(SaleItemTable.TAXABLE)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.TAX), BigDecimal.ZERO),
                    _decimal(c, c.getColumnIndex(SaleItemTable.TAX2), BigDecimal.ZERO),
                    TextUtils.isEmpty(itemGuid) ? price : _decimal(c, c.getColumnIndex(ItemTable.COST), BigDecimal.ZERO),
                    null,
                    null,
                    null,
                    null,
                    _bool(c, c.getColumnIndex(SaleItemTable.EBT_ELIGIBLE))
            );

            result.map.put(saleItemId, value);
        }

        value.totalPrice = _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE), BigDecimal.ZERO);
        value.finalDiscount = _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT), BigDecimal.ZERO);
        value.finalTax = _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_TAX), BigDecimal.ZERO);
        /*BigDecimal extra = _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST));
        if (extra != null && value.totalPrice != null) {
            value.totalPrice = value.totalPrice.add(extra);
        }*/
    }

    public static class SaleItemInfo2 extends SaleItemInfo {

        public String departmentGuid;
        public String departmentTitle;
        public String categoryGuid;
        public String categoryTitle;

        public String ean;
        public String productCode;

        public BigDecimal finalDiscount;
        public BigDecimal finalTax;
        public BigDecimal itemCost;

        public SaleItemInfo2(String saleItemGiud, String itemGiud, String description, String ean,
                             String productCode, BigDecimal qty, BigDecimal totalPrice,
                             boolean discountable, BigDecimal discount, DiscountType discountType,
                             boolean isTaxable, BigDecimal tax, BigDecimal tax2, BigDecimal itemCost,
                             String departmentGuid, String departmentTitle,
                             String categoryGuid, String categoryTitle, boolean isEbtEligible) {
            super(saleItemGiud, itemGiud, description, qty, totalPrice, discountable, discount,
                    discountType, isTaxable, tax, tax2, isEbtEligible, BigDecimal.ZERO);
            this.itemCost = itemCost;
            this.departmentGuid = departmentGuid;
            this.departmentTitle = departmentTitle;
            this.categoryGuid = categoryGuid;
            this.categoryTitle = categoryTitle;
            this.ean = ean;
            this.productCode = productCode;
        }
    }

    public static class StatInfo {
        BigDecimal grossSale = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal tax2 = BigDecimal.ZERO;
        BigDecimal cogs = BigDecimal.ZERO;
        BigDecimal transactionFee = BigDecimal.ZERO;
    }
}
