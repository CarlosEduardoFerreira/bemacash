package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Optional;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.activity.DashboardActivity.SalesStatisticsConverter;
import com.kaching123.tcr.activity.DashboardActivity.SalesStatisticsModel;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler2;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleItemInfo;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleOrderInfo;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.TipsModel.PaymentType;
import com.kaching123.tcr.model.XReportInfo;
import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.store.ShopProvider;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentGateway;
import static com.kaching123.tcr.model.ContentValuesUtil._tipsPaymentType;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;
import static com.kaching123.tcr.util.CalculationUtil.negative;

/*import com.kaching123.tcr.store.ShopSchema2.XReportView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.XReportView2.DepartmentTable;*/

/**
 * Created by gdubina on 22.01.14.
 */
public final class XReportQuery {

    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(XReportView.URI_CONTENT);
    private static final Uri ORDER_URI = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);
    private static final Uri URI_PAYMENTS = ShopProvider.getContentUri(PaymentTransactionView.URI_CONTENT);
    private static final Uri URI_SHIFT = ShopProvider.getContentUri(ShiftTable.URI_CONTENT);
    private static final Uri URI_SHIFT_LIMITED = ShopProvider.getContentWithLimitUri(ShiftTable.URI_CONTENT, 1);
    private static final Uri URI_TIPS = ShopProvider.getContentUri(EmployeeTipsTable.URI_CONTENT);
    private static final Uri URI_DRAWER_CASH_MOVEMENT = ShopProvider.getContentUri(ShopStore.CashDrawerMovementTable.URI_CONTENT);

    private XReportQuery() {
    }

    public static XReportInfo loadXReport(Context context, String shiftGuid) {

        Date startDate = null;
        Date endDate = null;

        BigDecimal openAmount = BigDecimal.ZERO;
        BigDecimal cashSale = BigDecimal.ZERO;
        BigDecimal safeDrops = BigDecimal.ZERO;
        BigDecimal payOuts = BigDecimal.ZERO;
        BigDecimal cashBack = BigDecimal.ZERO;

        Cursor c = ProviderAction.query(URI_SHIFT)
                .where(ShiftTable.GUID + " = ?", shiftGuid)
                .perform(context);

        if (c.moveToNext()) {
            startDate = new Date(c.getLong(c.getColumnIndex(ShiftTable.START_TIME)));
            endDate = new Date(c.getLong(c.getColumnIndex(ShiftTable.END_TIME)));
            openAmount = _decimal(c.getString(c.getColumnIndex(ShiftTable.OPEN_AMOUNT)));
        }
        c.close();

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
        BigDecimal debit = BigDecimal.ONE;

        HashMap<String, BigDecimal> cards = new HashMap<String, BigDecimal>();
        while (c.moveToNext()) {
            BigDecimal transactionAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.AMOUNT)));
            BigDecimal transactionTip = _decimal(c.getString(c.getColumnIndex(PaymentTransactionView2.EmployeeTipsTable.AMOUNT)));
            BigDecimal amount = transactionAmount.subtract(transactionTip);
            BigDecimal cashBackAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.CASH_BACK)));
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
            BigDecimal amount = _decimal(c, 0);
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
        Cursor cur = ProviderAction.query(URI_DRAWER_CASH_MOVEMENT)
                .where(ShopStore.CashDrawerMovementTable.SHIFT_GUID + " = ?", shiftGuid)
                .perform(context);

        while (cur.moveToNext()) {
            if (cur.getInt(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == MovementType.DROP.getValue())
                safeDrops = safeDrops.add(_decimal(cur.getString(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT))));
            else if (cur.getInt(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == MovementType.PAYOUT.getValue())
                payOuts = payOuts.add(_decimal(cur.getString(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT))));
        }
        cur.close();
        return new XReportInfo(startDate, endDate, grossSale, discount, returned, netSale, gratuity, tax, totalTender, cogs, grossMargin, grossMarginInPercent, creditCard, cash, tenderCreditReceipt, offlineCredit, check, ebtCash, ebtFoodstamp, debit, cards, drawerDifference, transactionFee, openAmount, cashSale, safeDrops, payOuts, cashBack);
    }

    private static Date getStartOfDay() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
        return calendar.getTime();
    }

    private static Date getEndOfDay() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 23, 59, 59);
        return calendar.getTime();
    }

    private static List<String> getDailyShiftGuidList(Context context) {
        final Cursor c = ProviderAction.query(URI_SHIFT)
                .where(ShiftTable.START_TIME + " > ? OR " + ShiftTable.END_TIME + " > ? OR " + ShiftTable.END_TIME + " IS NULL", getStartOfDay().getTime(), getStartOfDay().getTime())
                .perform(context);
        final List<String> guidList = new ArrayList<String>();
        if (c != null && c.moveToFirst()) {
            do {
                final String currentGuid = c.getString(c.getColumnIndex(ShiftTable.GUID));
                guidList.add(currentGuid);
            } while (c.moveToNext());
        }
        return guidList;
    }

    public static XReportInfo loadDailySalesXReport(Context context, long registerId) {

        final Date startDate = getStartOfDay();
        final Date endDate = getEndOfDay();
        final List<String> guidList = getDailyShiftGuidList(context);

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

        HashMap<String, BigDecimal> cards = new HashMap<String, BigDecimal>();

        String lastShiftGuid = getLastShiftGuidDaily(context, registerId);
        openAmount = getLastShiftDailyOpenAmount(context, lastShiftGuid);
        transactionFee = transactionFee.add(getDailyOrdersTransactionFee(context, OrderStatus.COMPLETED, registerId));//returnInfo is negative

        for (String guid : guidList) {
            Logger.d("===== Daily Sales Report. guid:" + guid + " =====");
            final StatInfo saleInfo = getDailyOrders(context, guid, OrderStatus.COMPLETED, registerId);
            final StatInfo returnInfo = getDailyOrders(context, guid, OrderStatus.RETURN, registerId);

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
                BigDecimal transactionAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.AMOUNT)));
                BigDecimal transactionTip = _decimal(c.getString(c.getColumnIndex(PaymentTransactionView2.EmployeeTipsTable.AMOUNT)));
                BigDecimal amount = transactionAmount.subtract(transactionTip);
                BigDecimal cashBackAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.CASH_BACK)));
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
            cashSale = cash;
            c = ProviderAction.query(URI_TIPS)
                    .projection(EmployeeTipsTable.AMOUNT, EmployeeTipsTable.PAYMENT_TYPE)
                    .where(EmployeeTipsTable.CREATE_TIME + " = ?", getStartOfDay().getTime())
                    .where(EmployeeTipsTable.SHIFT_ID + " = ?", guid)
                    .perform(context);

            while (c.moveToNext()) {
                BigDecimal amount = _decimal(c, 0);
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

        Cursor cur = ProviderAction.query(URI_DRAWER_CASH_MOVEMENT)
                .where(ShopStore.CashDrawerMovementTable.SHIFT_GUID + " = ?", lastShiftGuid)
                .perform(context);

        while (cur.moveToNext()) {
            if (cur.getInt(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == MovementType.DROP.getValue() )
                safeDrops = safeDrops.add(_decimal(cur.getString(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT))));
            else if (cur.getInt(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.TYPE)) == MovementType.PAYOUT.getValue())
                payOuts = payOuts.add(_decimal(cur.getString(cur.getColumnIndex(ShopStore.CashDrawerMovementTable.AMOUNT))));
        }
        cur.close();

        return new XReportInfo(startDate, endDate, grossSale, discount, returned, netSale, gratuity,
                tax, totalTender, cogs, grossMargin, grossMarginInPercent, creditCard, cash,
                tenderCreditReceipt, offlineCredit, check, ebtCash, ebtFoodstamp, debit, cards, drawerDifference, transactionFee, openAmount, cashSale, safeDrops, payOuts, cashBack);
    }

    private static BigDecimal getDailyOrdersTransactionFee(Context context, OrderStatus type, long registerId) {
        Cursor c = ProviderAction.query(ORDER_URI)
                .projection(ShopStore.SaleOrderTable.TRANSACTION_FEE)
                .where(ShopStore.SaleOrderTable.CREATE_TIME + " > ?", getStartOfDay().getTime())
                .where(ShopStore.SaleOrderTable.REGISTER_ID + " = ?", registerId)
                .where(ShopStore.SaleOrderTable.STATUS + " = ?", type.ordinal())
                .perform(context);

        BigDecimal totalTransactionFee = BigDecimal.ZERO;
        while (c.moveToNext()) {
            totalTransactionFee = _decimal(c, 0);
        }
        c.close();

        return totalTransactionFee;
    }

    private static BigDecimal getLastShiftDailyOpenAmount(Context context, String shiftGuid) {
        BigDecimal openAmount = BigDecimal.ZERO;
        Cursor c = ProviderAction.query(URI_SHIFT)
                .where(ShiftTable.GUID + " = ?", shiftGuid)
                .perform(context);

        if (c.moveToNext()) {
            openAmount = _decimal(c.getString(c.getColumnIndex(ShiftTable.OPEN_AMOUNT)));
        }
        c.close();

        return openAmount;
    }

    private static String getLastShiftGuidDaily(Context context, long registerId) {
        String lastShiftGuid = null;
        Cursor c = ProviderAction.query(URI_SHIFT)
                .projection(ShiftTable.GUID)
                .where(ShiftTable.REGISTER_ID + " = ?", registerId)
                .orderBy(ShiftTable.START_TIME + " DESC")
                .perform(context);

        if (c.moveToFirst()) {
            lastShiftGuid = c.getString(c.getColumnIndex(ShiftTable.GUID));
        }
        c.close();

        return lastShiftGuid;
    }

    private static BigDecimal getShiftOrdersTransactionFee(Context context, String shiftGuid, OrderStatus type) {
        Cursor c = ProviderAction.query(ORDER_URI)
                .projection(ShopStore.SaleOrderTable.TRANSACTION_FEE)
                .where(ShopStore.SaleOrderTable.SHIFT_GUID + " = ?", shiftGuid)
                .where(ShopStore.SaleOrderTable.STATUS + " = ?", type.ordinal())
                .perform(context);

        BigDecimal totalTransactionFee = BigDecimal.ZERO;
        while (c.moveToNext()) {
            totalTransactionFee = _decimal(c, 0);
            Logger.d("trace Transaction fee:" + totalTransactionFee);
        }
        c.close();

        return totalTransactionFee;
    }

    private static BigDecimal getDrawerDifference(Context context, String shiftGuid) {
        Optional<SalesStatisticsModel> salesStatisticsModel = ProviderAction.query(URI_SHIFT)
                .where(ShiftTable.GUID + " = ?", shiftGuid)
                .orderBy(ShopStore.ShiftTable.START_TIME + " DESC")
                .perform(context).toFluentIterable(new SalesStatisticsConverter(context)).get(0);
        if (salesStatisticsModel != null && salesStatisticsModel.isPresent()) {
            return salesStatisticsModel.get().getDrawerDifference();
        }
        return null;
    }

    private static StatInfo getDailyOrders(Context context, String shiftGuid, OrderStatus type, long registerId) {
        Cursor c = ProviderAction.query(URI_SALE_ITEMS)
                .where(SaleOrderTable.SHIFT_GUID + " = ?", shiftGuid)
                .where(SaleOrderTable.CREATE_TIME + " > ?", getStartOfDay().getTime())
                .where(SaleOrderTable.REGISTER_ID + " = ?", registerId)
                .where(SaleOrderTable.STATUS + " = ?", type.ordinal())
                .perform(context);
        return getOrders(context, c, shiftGuid, type);
    }

    private static StatInfo getShiftOrders(Context context, String shiftGuid, OrderStatus type) {
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
            public void handleItem(SaleItemInfo i, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
                SaleItemInfo2 i2 = (SaleItemInfo2) i;
                statInfo.grossSale = statInfo.grossSale.add(CalculationUtil.getSubTotal(i.qty, i.totalPrice));
                statInfo.cogs = statInfo.cogs.add(CalculationUtil.getSubTotal(i.qty, i2.itemCost));
            }
        };

        for (Entry<String, SaleOrderInfo> e : ordersInfo.entrySet()) {
            SaleOrderCostInfo result = calculate(e.getValue(), handler2);
            //statInfo.grossSale2 = statInfo.grossSale2.add(result.subTotalItemTotal);
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
            return new HashMap<String, SaleOrderInfo>(0);
        }
        HashMap<String, SaleOrderInfo> result = new HashMap<String, SaleOrderInfo>();
        if (c.moveToFirst()) {
            do {
                String orderGuid = c.getString(c.getColumnIndex(SaleItemTable.ORDER_GUID));
                SaleOrderInfo r = result.get(orderGuid);
                if (r == null) {
                    r = new SaleOrderInfo(_bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.DISCOUNT)),
                            _discountType(c, c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE)));
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
        BigDecimal price = _decimal(c, c.getColumnIndex(SaleItemTable.PRICE));
        SaleItemInfo2 value = (SaleItemInfo2) result.map.get(saleItemId);
        if (value == null) {
            value = new SaleItemInfo2(
                    saleItemId,
                    c.getString(c.getColumnIndex(SaleItemTable.ITEM_GUID)),
                    null,
                    c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                    c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)),
                    _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY)),
                    price,
                    _bool(c, c.getColumnIndex(SaleItemTable.DISCOUNTABLE)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.DISCOUNT)),
                    _discountType(c, c.getColumnIndex(SaleItemTable.DISCOUNT_TYPE)),
                    _bool(c, c.getColumnIndex(SaleItemTable.TAXABLE)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.TAX)),
                    TextUtils.isEmpty(itemGuid) ? price : _decimal(c, c.getColumnIndex(ItemTable.COST)),
                    null,
                    null,
                    null,
                    null
            );

            result.map.put(saleItemId, value);
        }

        value.totalPrice = _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE));
        value.finalDiscount = _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT));
        value.finalTax = _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_TAX));
        /*BigDecimal extra = _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST));
        if (extra != null && value.totalPrice != null) {
            value.totalPrice = value.totalPrice.add(extra);
        }*/
    }

    public static class SaleItemInfo2 extends SaleItemInfo {

        public BigDecimal itemCost;
        public String departmentGuid;
        public String departmentTitle;
        public String ean;
        public String productCode;

        public String categoryGuid;
        public String categoryTitle;

        public BigDecimal finalDiscount;
        public BigDecimal finalTax;

        public SaleItemInfo2(String saleItemGiud, String itemGiud, String description, String ean, String productCode, BigDecimal qty, BigDecimal totalPrice, boolean discountable, BigDecimal discount, DiscountType discountType, boolean isTaxable, BigDecimal tax, BigDecimal itemCost, String departmentGuid, String departmentTitle, String categoryGuid, String categoryTitle) {
            super(saleItemGiud, itemGiud, description, qty, totalPrice, discountable, discount, discountType, isTaxable, tax);
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
        //BigDecimal grossSale2 = BigDecimal.ZERO;
        BigDecimal grossSale = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal cogs = BigDecimal.ZERO;
        BigDecimal transactionFee = BigDecimal.ZERO;
    }
}
