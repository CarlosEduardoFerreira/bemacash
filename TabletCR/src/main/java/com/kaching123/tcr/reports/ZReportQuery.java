package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.print.SaleReportsProcessor;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.DepartsSale;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.ShiftModel;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.ZReportInfo;
import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus.PRINTED;
import static com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus.UPDATED;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentGateway;
import static com.kaching123.tcr.model.ContentValuesUtil._tipsPaymentType;
import static com.kaching123.tcr.model.ShiftModel.getLastDailyGuid;
import static com.kaching123.tcr.store.ShopSchema2.ZReportView2.ItemTable.PRINTER_ALIAS_GUID;
import static com.kaching123.tcr.store.ShopSchema2.ZReportView2.SaleOrderTable.KITCHEN_PRINT_STATUS;
import static com.kaching123.tcr.store.ShopSchema2.ZReportView2.SaleOrderTable.STATUS;
import static com.kaching123.tcr.util.CalculationUtil.negative;
import static com.kaching123.tcr.util.DateUtils.getEndOfDay;
import static com.kaching123.tcr.util.DateUtils.getStartOfDay;

/**
 * Created by alboyko on 26.11.2015.
 */
public final class ZReportQuery extends XReportQuery {

    private static BigDecimal salesCount;
    private static BigDecimal returnsCount;
    private static BigDecimal voidCount;

    private ZReportQuery() {
        super();
    }

    protected static final Uri URI_Z_SALE_ITEMS = ShopProvider.contentUri(ShopStore.ZReportView.URI_CONTENT);

    public static ZReportInfo loadZReport(Context context, String shiftGuid) {
        BigDecimal openAmount = BigDecimal.ZERO;
        BigDecimal cashSale = BigDecimal.ZERO;
        BigDecimal safeDrops = BigDecimal.ZERO;
        BigDecimal payOuts = BigDecimal.ZERO;
        BigDecimal cashBack = BigDecimal.ZERO;

        TreeMap<String, DepartsSale> departsSales = new TreeMap<>();

        salesCount = BigDecimal.ZERO;
        returnsCount = BigDecimal.ZERO;
        voidCount = BigDecimal.ZERO;

        Date startDate = null;
        Date endDate = null;

        ShiftModel shiftModel = ShiftModel.getById(context, shiftGuid);
        if (shiftModel != null) {
            startDate = shiftModel.startTime;
            endDate = shiftModel.endTime;
            openAmount = shiftModel.openAmount;
        }

        DepartsSale prepaidTotalSale = new DepartsSale("Prepaid", BigDecimal.ZERO);

        ArrayList<SalesByItemsReportQuery.ReportItemInfo> result
                = new ArrayList<>(createQuery()
                .getItems(context, startDate.getTime(), shiftGuid, OrderType.PREPAID));
        final ArrayList<SalesByItemsReportQuery.ReportItemInfo> groupedResult
                = SaleReportsProcessor.getGroupedResult(result, OrderType.PREPAID);
        for (SalesByItemsReportQuery.ReportItemInfo item : groupedResult) {
            prepaidTotalSale.sales = prepaidTotalSale.sales.add(item.revenue);
        }
        departsSales.put(prepaidTotalSale.departTitle, prepaidTotalSale);
        result = new ArrayList<>(createQuery().getItems(context, startDate.getTime(), shiftGuid, OrderType.SALE));
        Collection<SalesByDepartmentsReportQuery.DepartmentStatistics> deps =
                new SalesByDepartmentsReportQuery().getItems(context, startDate.getTime(), shiftGuid);
        totalValue = BigDecimal.ZERO;
        for (SalesByDepartmentsReportQuery.DepartmentStatistics d : deps) {
            if (d.description != null)
                departsSales.put(d.description, new DepartsSale(d.description, d.revenue));
            d.reset();
            totalValue = totalValue.add(d.revenue);
        }


        final StatInfo saleInfo = getShiftOrders(context, shiftGuid, OrderStatus.COMPLETED);
        final StatInfo returnInfo = getShiftOrders(context, shiftGuid, OrderStatus.RETURN);


        BigDecimal positiveTips = BigDecimal.ZERO;
        BigDecimal negatoveTips = BigDecimal.ZERO;
        Cursor tipsCursor = ProviderAction.query(URI_TIPS)
                .projection(EmployeeTipsTable.AMOUNT)
                .where(EmployeeTipsTable.SHIFT_ID + " = ?", shiftGuid)
                .perform(context);

        while (tipsCursor.moveToNext()) {
            BigDecimal tips = _decimal(tipsCursor, 0, BigDecimal.ZERO);
            if (tips.compareTo(BigDecimal.ZERO) > 0) {
                positiveTips = positiveTips.add(tips);
            } else {
                negatoveTips = negatoveTips.add(tips);
            }
        }

        BigDecimal discount = saleInfo.discount;
        BigDecimal grossSale = saleInfo.grossSale;

        tipsCursor.close();

        BigDecimal returned = negative(returnInfo.grossSale.subtract(returnInfo.discount).add(negatoveTips));
        BigDecimal tax = saleInfo.tax.add(returnInfo.tax);//returnInfo is negative
        BigDecimal cogs = saleInfo.cogs.add(returnInfo.cogs);//returnInfo is negative
        BigDecimal transactionFee = saleInfo.transactionFee.add(getShiftOrdersTransactionFee(context, shiftGuid, OrderStatus.COMPLETED));

        Logger.d("[ZREPORT]\t=== ZReport ===");
        Logger.d("[ZREPORT]\tStart date/time:   %s", startDate);
        Logger.d("[ZREPORT]\tEnd date/time:     %s", endDate);

        Logger.d("[ZREPORT]\tGross Sale:   %s", grossSale);
        //Logger.d("[XREPORT]\tGross2:       %s", saleInfo.grossSale2);
        Logger.d("[ZREPORT]\tDiscount:     %s", discount);
        Logger.d("[ZREPORT]\tReturns:      %s", returned);
        Logger.d("[ZREPORT]\t------------------");

        BigDecimal netSale = grossSale.subtract(returned).subtract(discount);

        BigDecimal drops = BigDecimal.ZERO;
        BigDecimal payouts = BigDecimal.ZERO;
        Cursor cashDrawerCursor = ProviderAction.query(URI_CASH_DRAWER_DATA)
                .projection(ShopStore.CashDrawerMovementTable.AMOUNT)
                .where(ShopStore.CashDrawerMovementTable.SHIFT_GUID + " = ?", shiftGuid)
                .perform(context);

        ArrayList<BigDecimal> cashDropsPayouts = new ArrayList<BigDecimal>();

        while (cashDrawerCursor.moveToNext()) {
            cashDropsPayouts.add(_decimal(cashDrawerCursor.getString(cashDrawerCursor.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT)), BigDecimal.ZERO));
        }
        cashDrawerCursor.close();

        for (BigDecimal bigDecimal : cashDropsPayouts) {
            if (bigDecimal.signum() > 0) {
                payouts = payouts.add(bigDecimal);
            } else {
                drops = drops.add(bigDecimal.abs());
            }
        }

        Cursor c = ProviderAction.query(URI_PAYMENTS)
                .projection(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.AMOUNT,
                        ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.GATEWAY,
                        ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CARD_NAME,
                        ShopSchema2.PaymentTransactionView2.EmployeeTipsTable.AMOUNT,
                        ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CASH_BACK,
                        ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.TYPE,
                        ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.ORDER_GUID
                )
                .where(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.SHIFT_GUID + " = ?", shiftGuid)
//                .where("(" + ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.STATUS + " = ? OR " + ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.STATUS + " = ?)", PaymentTransactionModel.PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentTransactionModel.PaymentStatus.SUCCESS.ordinal())
                .perform(context);

        BigDecimal creditCard = BigDecimal.ZERO;
        BigDecimal cash = BigDecimal.ZERO;
        BigDecimal tenderCreditReceipt = BigDecimal.ZERO;
        BigDecimal offlineCredit = BigDecimal.ZERO;
        BigDecimal offlineDebit = BigDecimal.ZERO;
        BigDecimal offlineVoucher = BigDecimal.ZERO;
        BigDecimal check = BigDecimal.ZERO;
        BigDecimal ebtCash = BigDecimal.ZERO;
        BigDecimal ebtFoodstamp = BigDecimal.ZERO;
        BigDecimal voucher = BigDecimal.ZERO;
        BigDecimal debit = BigDecimal.ZERO;

        HashMap<String, BigDecimal> cards = new HashMap<String, BigDecimal>();
        while (c.moveToNext()) {
            BigDecimal transactionAmount = _decimal(c.getString(c.getColumnIndex(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.AMOUNT)), BigDecimal.ZERO);
            //BigDecimal transactionTip = _decimal(c.getString(c.getColumnIndex(EmployeeTipsTable.AMOUNT)));
            BigDecimal amount = transactionAmount;//.subtract(tips);
            BigDecimal cashBackAmount = _decimal(c.getString(c.getColumnIndex(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CASH_BACK)), BigDecimal.ZERO);
            cashBack = cashBackAmount;
            PaymentGateway gateway = _paymentGateway(c, c.getColumnIndex(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.GATEWAY));

            if (gateway!=null && gateway.isTrueCreditCard()) {
                creditCard = creditCard.add(amount);
            } else if (gateway == PaymentGateway.CASH) {
                cash = cash.add(amount);
            } else if (gateway == PaymentGateway.CREDIT) {
                tenderCreditReceipt = tenderCreditReceipt.add(amount);
            } else if (gateway == PaymentGateway.OFFLINE_CREDIT) {
                offlineCredit = offlineCredit.add(amount);
            } else if (gateway == PaymentGateway.CHECK) {
                check = check.add(amount);
            } else if (gateway == PaymentGateway.PAX_DEBIT) {
                debit = debit.add(amount);
            } else if (gateway == PaymentGateway.PAX_EBT_CASH) {
                ebtCash = ebtCash.add(amount);
            } else if (gateway == PaymentGateway.PAX_EBT_FOODSTAMP) {
                ebtFoodstamp = ebtFoodstamp.add(amount);
            }

            if (gateway!=null && gateway.isTrueCreditCard()) {
                String card = c.getString(c.getColumnIndex(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CARD_NAME));
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
                .projection(ShopStore.EmployeeTipsTable.AMOUNT, ShopStore.EmployeeTipsTable.PAYMENT_TYPE)
                .where(ShopStore.EmployeeTipsTable.SHIFT_ID + " = ?", shiftGuid)
                .perform(context);

        while (c.moveToNext()) {
            BigDecimal amount = _decimal(c, 0, BigDecimal.ZERO);
            TipsModel.PaymentType type = _tipsPaymentType(c, 1);

//            if (type == PaymentType.CASH) {
//                cash = cash.add(amount);
//            } else if (type == PaymentType.CREDIT) {
//                creditCard = creditCard.add(amount);
//            }
            gratuity = gratuity.add(amount);
        }
        c.close();

        Logger.d("[ZREPORT]\tNet Sale:     %s", netSale);
        Logger.d("[ZREPORT]\tGratuity:     %s", gratuity);
        Logger.d("[ZREPORT]\tTax:          %s", tax);
        Logger.d("[ZREPORT]\t------------------");

        BigDecimal totalTender = netSale.add(gratuity).add(tax);
        Logger.d("[ZREPORT]\tTotal Tender: %s", totalTender);

        Logger.d("[ZREPORT]\tCOGS:         %s", cogs);

        BigDecimal grossMargin = netSale.subtract(cogs);
        BigDecimal grossMarginInPercent = CalculationUtil.value(CalculationUtil.getDiscountValueInPercent(netSale, grossMargin, DiscountType.VALUE));
        Logger.d("[ZREPORT]");
        Logger.d("[ZREPORT]\tGross Margin: %s", grossMargin);
        Logger.d("[ZREPORT]\t              %s", grossMarginInPercent);

        Logger.d("[ZREPORT]");
        Logger.d("[ZREPORT]\tTender Summary");
        Logger.d("[ZREPORT]");
        Logger.d("[ZREPORT]\tCredit card: %s", creditCard);
        Logger.d("[ZREPORT]\tVoucher: %s", voucher);
        Logger.d("[ZREPORT]\tCash:        %s", cash);
        Logger.d("[ZREPORT]\tCredit receipt:   %s", tenderCreditReceipt);
        Logger.d("[ZREPORT]\tOffline credit:   %s", offlineCredit);
        Logger.d("[ZREPORT]\tOffline debit:   %s", offlineDebit);
        Logger.d("[ZREPORT]\tOffline voucher:   %s", offlineVoucher);

        Logger.d("[ZREPORT]\tCheck:   %s", check);
        //Logger.d("[XREPORT]\tAnother:     %s", another);

        Logger.d("[ZREPORT]");
        Logger.d("[ZREPORT]\tCredit Card Details");
        Logger.d("[ZREPORT]");
        for (Map.Entry<String, BigDecimal> e : cards.entrySet()) {
            Logger.d("[ZREPORT]\t%s\t%s", e.getKey(), e.getValue());
        }

        final BigDecimal drawerDifference = getDrawerDifference(context, shiftGuid);
        cashSale = cash;
        shiftSVRCounter(context, shiftGuid);

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

        return new ZReportInfo(startDate, endDate, grossSale, discount, returned, netSale, gratuity, tax, totalTender, cogs,
                grossMargin, grossMarginInPercent, creditCard, cash, tenderCreditReceipt, offlineCredit, check,
                ebtCash, ebtFoodstamp,
                debit, cards, drawerDifference,
                transactionFee, openAmount, cashSale, safeDrops, payOuts,
                cashBack, departsSales,
                result, totalValue, salesCount, voidCount, returnsCount);

    }

    public static ZReportInfo loadDailySalesZReport(Context context, long registerID, long fromDate, long toDate) {

        final Date startDate = getStartOfDay();
        final Date endDate = getEndOfDay();

        final List<String> guidList = ShiftModel.getDailyGuidList(context, registerID, fromDate, toDate);

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

        BigDecimal openAmount = BigDecimal.ZERO;

        BigDecimal check = BigDecimal.ZERO;
        BigDecimal ebtCash = BigDecimal.ZERO;
        BigDecimal ebtFoodstamp = BigDecimal.ZERO;

        BigDecimal voucher = BigDecimal.ZERO;
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal gratuity = BigDecimal.ZERO;
        BigDecimal totalTender = BigDecimal.ZERO;
        BigDecimal grossMargin = BigDecimal.ZERO;
        BigDecimal grossMarginInPercent = BigDecimal.ZERO;
        BigDecimal drawerDifference = BigDecimal.ZERO;

        BigDecimal cashSale = BigDecimal.ZERO;
        BigDecimal cashBack = BigDecimal.ZERO;

        TreeMap<String, DepartsSale> departsSales = new TreeMap<>();
        ArrayList<SalesByItemsReportQuery.ReportItemInfo> itemSales = new ArrayList<>();

        HashMap<String, BigDecimal> cards = new HashMap<>();
        BigDecimal positiveTips = BigDecimal.ZERO;
        BigDecimal negativeTips = BigDecimal.ZERO;

        salesCount = BigDecimal.ZERO;
        returnsCount = BigDecimal.ZERO;
        voidCount = BigDecimal.ZERO;

        String lastShiftGuid = getLastDailyGuid(context, registerID);
        openAmount = getLastShiftDailyOpenAmount(context, lastShiftGuid);
        transactionFee = transactionFee.add(getDailyOrdersTransactionFee(context, OrderStatus.COMPLETED, registerID, fromDate, toDate));//returnInfo is negative


        final StatInfo saleInfo = getDailyOrders(context, OrderStatus.COMPLETED, registerID, fromDate, toDate);
        final StatInfo returnInfo = getDailyOrders(context, OrderStatus.RETURN, registerID, fromDate, toDate);

        grossSale = grossSale.add(saleInfo.grossSale);
        Logger.d("||grossSale:" + grossSale);

        discount = discount.add(saleInfo.discount);
        Logger.d("||discount:" + discount);

        returned = returned.add(negative(returnInfo.grossSale.subtract(returnInfo.discount)));
        Logger.d("||returned:" + returned);


        tax = tax.add(saleInfo.tax.add(returnInfo.tax));//returnInfo is negative
        cogs = cogs.add(saleInfo.cogs.add(returnInfo.cogs));//returnInfo is negative

        Cursor tipsCursor = ProviderAction.query(URI_TIPS)
                .projection(ShopStore.EmployeeTipsTable.AMOUNT)
                .where(ShopStore.EmployeeTipsTable.CREATE_TIME + " > ?", fromDate)
                .where(ShopStore.EmployeeTipsTable.CREATE_TIME + " < ?", toDate)
                .perform(context);

        while (tipsCursor.moveToNext()) {
            BigDecimal tips = _decimal(tipsCursor, 0, BigDecimal.ZERO);
            if (tips.compareTo(BigDecimal.ZERO) > 0) {
                positiveTips = positiveTips.add(tips);
            } else {
                negativeTips = negativeTips.add(tips);
            }
        }


        Cursor gratuityCursor = ProviderAction.query(URI_TIPS)
                .projection(ShopStore.EmployeeTipsTable.AMOUNT, ShopStore.EmployeeTipsTable.PAYMENT_TYPE)
                .where(ShopStore.EmployeeTipsTable.CREATE_TIME + " > ?", fromDate)
                .where(ShopStore.EmployeeTipsTable.CREATE_TIME + " < ?", toDate)
                .perform(context);

        while (gratuityCursor.moveToNext()) {
            BigDecimal amount = _decimal(gratuityCursor, 0, BigDecimal.ZERO);
            TipsModel.PaymentType type = _tipsPaymentType(gratuityCursor, 1);

//                if (type == PaymentType.CASH) {
//                    cash = cash.add(amount);
//                } else if (type == PaymentType.CREDIT) {
//                    creditCard = creditCard.add(amount);
//                }
            gratuity = gratuity.add(amount);
        }

        for (String guid : guidList) {

            Cursor c = ProviderAction.query(URI_PAYMENTS)
                    .projection(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.AMOUNT,
                            ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.GATEWAY,
                            ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.TYPE,
                            ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CARD_NAME,
                            ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.ORDER_GUID,
                            ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CASH_BACK,
                            ShopSchema2.PaymentTransactionView2.EmployeeTipsTable.AMOUNT)
                    .where(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.ORDER_GUID + " = ?", guid)
//                    .where(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CREATE_TIME + " > ?", startDate.getTime())
//                    .where("(" + ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.STATUS + " = ? OR " + ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.STATUS + " = ?)",
//                            PaymentTransactionModel.PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentTransactionModel.PaymentStatus.SUCCESS.ordinal())
                    .perform(context);


            while (c.moveToNext()) {
                BigDecimal transactionAmount = _decimal(c.getString(c.getColumnIndex(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.AMOUNT)), BigDecimal.ZERO);
                BigDecimal transactionTip = _decimal(c.getString(c.getColumnIndex(ShopSchema2.PaymentTransactionView2.EmployeeTipsTable.AMOUNT)), BigDecimal.ZERO);
                BigDecimal amount = transactionAmount;//.subtract(transactionTip);
                BigDecimal cashBackAmount = _decimal(c.getString(c.getColumnIndex(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CASH_BACK)), BigDecimal.ZERO);
                cashBack = cashBackAmount;
                PaymentGateway gateway = _paymentGateway(c, c.getColumnIndex(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.GATEWAY));

                //if (gateway.isCreditCard() && gateway != PaymentGateway.PAX_DEBIT) {
                if (gateway!=null && gateway.isTrueCreditCard()) {
                    creditCard = creditCard.add(amount);
                } else if (gateway == PaymentGateway.CASH) {
                    cash = cash.add(amount);
                } else if (gateway == PaymentGateway.CREDIT_RECEIPT || gateway == PaymentGateway.CREDIT) {
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

                if (gateway!=null && gateway.isTrueCreditCard()) {
                    String card = c.getString(c.getColumnIndex(ShopSchema2.PaymentTransactionView2.PaymentTransactionTable.CARD_NAME));
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

                DepartsSale prepaidTotalSale = new DepartsSale("Prepaid", BigDecimal.ZERO);
                itemSales = new ArrayList<SalesByItemsReportQuery.ReportItemInfo>(createQuery().getItems(context, startDate.getTime(), guid, OrderType.PREPAID));
                final ArrayList<SalesByItemsReportQuery.ReportItemInfo> groupedResult = SaleReportsProcessor.getGroupedResult(itemSales, OrderType.PREPAID);
                for (SalesByItemsReportQuery.ReportItemInfo item : groupedResult) {
                    prepaidTotalSale.sales = prepaidTotalSale.sales.add(item.revenue);
                }
                departsSales.put(prepaidTotalSale.departTitle, prepaidTotalSale);

                itemSales = new ArrayList<SalesByItemsReportQuery.ReportItemInfo>(createQuery().getItems(context, startDate.getTime(), guid, OrderType.SALE));
                Collection<SalesByDepartmentsReportQuery.DepartmentStatistics> deps = new SalesByDepartmentsReportQuery().getItems(context, startDate.getTime(), guid);
                totalValue = BigDecimal.ZERO;
                for (SalesByDepartmentsReportQuery.DepartmentStatistics d : deps) {
                    departsSales.put(d.description, new DepartsSale(d.description, d.revenue));
                    d.reset();
                    totalValue = totalValue.add(d.revenue);
                }


            }

            c.close();

            for (Map.Entry<String, BigDecimal> e : cards.entrySet()) {
                Logger.d("[ZREPORT]\t%s\t%s", e.getKey(), e.getValue());
            }
//            drawerDifference = drawerDifference.add(getDrawerDifference(context, guid));
        }


        BigDecimal drops = BigDecimal.ZERO;
        BigDecimal payouts = BigDecimal.ZERO;
        Cursor cashDrawerCursor = ProviderAction.query(URI_CASH_DRAWER_DATA)
                .projection(ShopStore.CashDrawerMovementTable.AMOUNT, ShopStore.CashDrawerMovementTable.TYPE)
                .where(ShopStore.CashDrawerMovementTable.MOVEMENT_TIME + " > ?", fromDate)
                .where(ShopStore.CashDrawerMovementTable.MOVEMENT_TIME + " < ?", toDate)
                .perform(context);

        ArrayList<BigDecimal> cashPayouts = new ArrayList<BigDecimal>();
        ArrayList<BigDecimal> cashDrops = new ArrayList<BigDecimal>();

        while (cashDrawerCursor.moveToNext()) {
            if (cashDrawerCursor.getInt(cashDrawerCursor.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == 0)
                cashDrops.add(_decimal(cashDrawerCursor.getString(cashDrawerCursor.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT)), BigDecimal.ZERO));
            else
                cashPayouts.add(_decimal(cashDrawerCursor.getString(cashDrawerCursor.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT)), BigDecimal.ZERO));
        }
        cashDrawerCursor.close();

        for (BigDecimal bigDecimal : cashDrops) {
            drops = drops.add(bigDecimal.abs());
        }

        for (BigDecimal bigDecimal : cashPayouts) {
            payouts = payouts.add(bigDecimal.negate());
        }

//        grossSale = grossSale.add(positiveTips);
        returned = returned.subtract(negativeTips);
        netSale = grossSale.subtract(discount).subtract(returned);

        cashSale = cash;

        Logger.d("||netSale:" + netSale);

        totalTender = netSale.add(gratuity).add(tax);
        Logger.d("||totalTender:" + totalTender);

        grossMargin = netSale.subtract(cogs);
        Logger.d("||totalTender:" + totalTender);
        grossMarginInPercent = CalculationUtil.value(CalculationUtil.getDiscountValueInPercent(netSale, grossMargin, DiscountType.VALUE));

        dailySVRCounter(context, registerID, guidList);

        return new ZReportInfo(startDate, endDate, grossSale, discount, returned, netSale, gratuity, tax,
                totalTender, cogs, grossMargin, grossMarginInPercent, creditCard, cash, tenderCreditReceipt,
                offlineCredit, check,
                ebtCash,
                ebtFoodstamp,
                debit, cards, drawerDifference,
                transactionFee,
                openAmount,
                cashSale,
                drops, payouts,
                cashBack,
                departsSales,
                itemSales,
                totalValue, salesCount, voidCount, returnsCount);
    }

    private static void dailySVRCounter(Context context, long registerId, List<String> guidList) {  //slaes, voids, refunds - S.V.R.

        Cursor c = null;
        for (String guid : guidList) {
            Query query = ProviderAction.query(URI_Z_SALE_ITEMS)
                    .where(ShopSchema2.ZReportView2.SaleOrderItemTable.ORDER_GUID + " = ?", guid);

            if (registerId == 0) {
                c = query.perform(context);
            } else {
                c = query.where(ShopSchema2.ZReportView2.SaleOrderTable.REGISTER_ID + " = ?", registerId)
                        .perform(context);
            }

            while (c != null && c.moveToNext()) {
                BigDecimal itemQty = ContentValuesUtil._decimalQty(c, c.getColumnIndex(ShopSchema2.ZReportView2.SaleOrderItemTable.QUANTITY), BigDecimal.ZERO);

                if ((ContentValuesUtil._orderStatus(c, c.getColumnIndex(STATUS)).equals(OrderStatus.CANCELED) ||
                        (ContentValuesUtil._orderStatus(c, c.getColumnIndex(STATUS)).equals(OrderStatus.HOLDON) &&
                                c.getInt(c.getColumnIndex(ShopSchema2.ZReportView2.SaleOrderItemTable.IS_DELETED)) == 1)) &&
                        (ContentValuesUtil._kitchenPrintStatus(c, c.getColumnIndex(KITCHEN_PRINT_STATUS)).equals(PRINTED) ||
                                ContentValuesUtil._kitchenPrintStatus(c, c.getColumnIndex(KITCHEN_PRINT_STATUS)).equals(UPDATED)) &&
                        c.getString(c.getColumnIndex(PRINTER_ALIAS_GUID)) != null) {

                    voidCount = voidCount.add(itemQty);
                } else if (ContentValuesUtil._orderStatus(c, c.getColumnIndex(STATUS)).equals(OrderStatus.COMPLETED)) {
                    salesCount = salesCount.add(itemQty);
                } else if (ContentValuesUtil._orderStatus(c, c.getColumnIndex(STATUS)).equals(OrderStatus.RETURN)) {
                    returnsCount = returnsCount.add(itemQty.negate());
                }
            }
        }

        if (c != null)
            c.close();
    }

    private static void shiftSVRCounter(Context context, String shiftGuid) {
        Cursor c = ProviderAction.query(URI_Z_SALE_ITEMS)
                .where(ShopSchema2.ZReportView2.SaleOrderTable.SHIFT_GUID + " = ?", shiftGuid)
                .perform(context);

        while (c != null && c.moveToNext()) {
            BigDecimal itemQty = ContentValuesUtil._decimalQty(c, c.getColumnIndex(ShopSchema2.ZReportView2.SaleOrderItemTable.QUANTITY), BigDecimal.ZERO);

            if ((ContentValuesUtil._orderStatus(c, c.getColumnIndex(STATUS)).equals(OrderStatus.CANCELED) ||
                    (ContentValuesUtil._orderStatus(c, c.getColumnIndex(STATUS)).equals(OrderStatus.HOLDON) &&
                            c.getInt(c.getColumnIndex(ShopSchema2.ZReportView2.SaleOrderItemTable.IS_DELETED)) == 1)) &&
                    (ContentValuesUtil._kitchenPrintStatus(c, c.getColumnIndex(KITCHEN_PRINT_STATUS)).equals(PRINTED) ||
                            ContentValuesUtil._kitchenPrintStatus(c, c.getColumnIndex(KITCHEN_PRINT_STATUS)).equals(UPDATED)) &&
                    c.getString(c.getColumnIndex(PRINTER_ALIAS_GUID)) != null) {

                voidCount = voidCount.add(itemQty);
            } else if (ContentValuesUtil._orderStatus(c, c.getColumnIndex(STATUS)).equals(OrderStatus.COMPLETED)) {
                salesCount = salesCount.add(itemQty);
            } else if (ContentValuesUtil._orderStatus(c, c.getColumnIndex(STATUS)).equals(OrderStatus.RETURN)) {
                returnsCount = returnsCount.add(itemQty);
            }
        }


        if (c != null)
            c.close();

    }

}
