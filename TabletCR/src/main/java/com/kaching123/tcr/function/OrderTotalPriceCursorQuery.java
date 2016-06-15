package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler2;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;
import static com.kaching123.tcr.util.CalculationUtil.getTaxVatValueNoScale;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by gdubina on 10.12.13.
 */
public final class OrderTotalPriceCursorQuery {

    private static final Uri SALE_ITEMS_URI = ShopProvider.getContentUri(SaleOrderItemsView.URI_CONTENT);
    private static final Uri SALE_UNITS_URI = ShopProvider.getContentUri(ShopStore.UnitTable.URI_CONTENT);
    private static final Uri TIPS_URI = ShopProvider.getContentUri(EmployeeTipsTable.URI_CONTENT);
    private static final Uri ORDER_URI = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);
    private static final Uri PAYMENT_TRANSACTION_URI = ShopProvider.getContentUri(ShopStore.PaymentTransactionTable.URI_CONTENT);

    private OrderTotalPriceCursorQuery() {
    }

    public static void loadSync(Context context, String orderGuid, PrintHandler handler) {
        assert handler != null;
        List<SaleOrderItemViewModel> items = loadItems(context, orderGuid);
        BigDecimal tips = loadTips(context, orderGuid);
        BigDecimal transactionFee = getTransactionFee(orderGuid, context);
//        BigDecimal cashBackAmount = getCashBackAmount(orderGuid, context);
        calculate(context, orderGuid, null, items, tips, handler, transactionFee);
    }

    public static void loadSync(Context context, String orderGuid, LotteryHandler lotteryHandler) {
        assert lotteryHandler != null;
        List<SaleOrderItemViewModel> items = loadItems(context, orderGuid);
        List<PaymentTransactionModel> payments = ReadPaymentTransactionsFunction.loadByOrderSingle(context, orderGuid);
        BigDecimal tips = loadTips(context, orderGuid);
        BigDecimal transactionFee = getTransactionFee(orderGuid, context);
        calculateForLottery(context, null, items, tips, lotteryHandler, transactionFee, payments);
    }

//    private static BigDecimal getCashBackAmount(String orderGuid, Context context) {
//        BigDecimal cashBackAmount = BigDecimal.ZERO;
//        Cursor c = ProviderAction.query(PAYMENT_TRANSACTION_URI)
//                .projection(ShopStore.PaymentTransactionTable.CASH_BACK)
//                .where(ShopStore.PaymentTransactionTable.ORDER_GUID + " = ?", orderGuid)
//                .perform(context);
//        while (c.moveToNext()) {
//            cashBackAmount.add(_decimal(c, 0));
//        }
//        c.close();
//        return cashBackAmount;
//    }

    private static BigDecimal getTransactionFee(String orderGuid, Context context) {
        BigDecimal transactionFee = BigDecimal.ZERO;
        Cursor c = ProviderAction.query(ORDER_URI)
                .projection(ShopStore.SaleOrderTable.TRANSACTION_FEE)
                .where(ShopStore.SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(context);
        if (c.moveToFirst()) {
            transactionFee = _decimal(c, 0);
        }
        c.close();
        return transactionFee;
    }

    public static void loadSync(Context context, String orderGuid, String childOrderGuid, PrintHandler handler) {
        assert handler != null;
        List<SaleOrderItemViewModel> items = loadItems(context, orderGuid);
        BigDecimal tips = loadTips(context, orderGuid);
        BigDecimal transactionFee = getTransactionFee(orderGuid, context);
//        BigDecimal cashBackAmount = getCashBackAmount(orderGuid, context);

        calculate(context, orderGuid, childOrderGuid, items, tips, handler, transactionFee);
    }

    public static void loadSync(Context context, String orderGuid, Handler handler) {
        assert handler != null;
        List<SaleOrderItemViewModel> items = loadItems(context, orderGuid);
        BigDecimal tips = loadTips(context, orderGuid);

        OrderTotalPriceCalculator.calculate(items, tips, handler);
    }

    public static void loadSync(Context context, String orderGuid, Handler2 handler2) {
        assert handler2 != null;
        List<SaleOrderItemViewModel> items = loadItems(context, orderGuid);
        BigDecimal tips = loadTips(context, orderGuid);
        OrderTotalPriceCalculator.calculate(items, tips, handler2);
    }

    private static void calculateForLottery(Context context, String childOrderGuid,
                                            List<SaleOrderItemViewModel> items, BigDecimal tips, LotteryHandler handler, BigDecimal transactionFee, List<PaymentTransactionModel> payments) {
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalCashBack = BigDecimal.ZERO;

        for (PaymentTransactionModel p : payments) {
            totalCashBack = totalCashBack.add(p.cashBack.negate());
        }

        for (SaleOrderItemViewModel item : items) {
            final SaleOrderItemModel itemModel = item.itemModel;
            final BigDecimal itemQty = item.getQty();
            final BigDecimal itemSubtotal = getSubTotal(itemQty, itemModel.finalGrossPrice);
            final BigDecimal itemDiscount = getSubTotal(itemQty, itemModel.finalDiscount);
            final BigDecimal itemTax = getSubTotal(itemQty, itemModel.finalTax);

            totalSubtotal = totalSubtotal.add(itemSubtotal);
            totalDiscount = totalDiscount.add(itemDiscount);
            totalTax = totalTax.add(itemTax);
        }

        handler.handleTotal(totalSubtotal, totalDiscount, totalTax, tips, transactionFee, totalCashBack);
    }

    private static void calculate(Context context, String orderGuid, String childOrderGuid,
                                  List<SaleOrderItemViewModel> items, BigDecimal tips,
                                  PrintHandler handler, BigDecimal transactionFee) {
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        Map<TaxGroupModel, BigDecimal> taxes = new HashMap<>();
        Map<TaxGroupModel, BigDecimal> subtotals = new HashMap<>();

        for (SaleOrderItemViewModel item : items) {
            final SaleOrderItemModel itemModel = item.itemModel;
            final BigDecimal itemQty = item.getQty();
            final BigDecimal itemSubtotal = getSubTotal(itemQty, itemModel.finalGrossPrice);
            final BigDecimal itemDiscount = getSubTotal(itemQty, itemModel.finalDiscount);
            final BigDecimal itemTax = itemModel.isTaxable == false ? BigDecimal.ZERO : getSubTotal(itemQty, itemModel.finalTax);
            final BigDecimal singleItemPrice = itemModel.finalGrossPrice.add(itemModel.finalTax).subtract(itemModel.finalDiscount);

            final TaxGroupModel model1 = item.taxGroup1;
            if (!TextUtils.isEmpty(model1.getGuid())) {
                BigDecimal subTax = getTaxVatValueNoScale(itemModel.isTaxable == false ? BigDecimal.ZERO : itemModel.finalGrossPrice.subtract(itemModel.finalDiscount), itemModel.tax);
                BigDecimal currentTax = itemModel.isTaxable == false ? BigDecimal.ZERO : getSubTotal(itemQty, subTax);
                BigDecimal subTotal = getSubTotal(itemQty, itemModel.finalGrossPrice.subtract(itemModel.finalDiscount));
                if (taxes.containsKey(model1)) {
                    BigDecimal taxGroupValue = itemModel.isTaxable == false ? BigDecimal.ZERO : taxes.get(model1);
                    currentTax = taxGroupValue.add(currentTax);

                    BigDecimal sub = subtotals.get(model1);
                    subTotal = sub.add(subTotal);
                }
                taxes.put(model1, currentTax);
                subtotals.put(model1, subTotal);
            } else { // store tax
                if (taxes.get(model1) != null)
                    taxes.put(model1, taxes.get(model1).add(getSubTotal(itemQty, itemModel.isTaxable == false ? BigDecimal.ZERO : itemModel.finalTax.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : itemModel.price.multiply(itemModel.tax.divide(new BigDecimal(100))))));
                else
                    taxes.put(model1, getSubTotal(itemQty, itemModel.price.multiply(itemModel.isTaxable == false ? BigDecimal.ZERO : itemModel.finalTax.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : itemModel.tax.divide(new BigDecimal(100)))));
                subtotals.put(model1, getSubTotal(itemQty, itemModel.finalGrossPrice.subtract(itemModel.finalDiscount)));
            }
            final TaxGroupModel model2 = item.taxGroup2;
            if (!TextUtils.isEmpty(model2.getGuid())) {
                BigDecimal subTax = getTaxVatValueNoScale(itemModel.isTaxable == false ? BigDecimal.ZERO : itemModel.finalGrossPrice.subtract(itemModel.finalDiscount), itemModel.tax2);
                BigDecimal currentTax = itemModel.isTaxable == false ? BigDecimal.ZERO : getSubTotal(itemQty, subTax);
                BigDecimal subTotal = getSubTotal(itemQty, itemModel.finalGrossPrice.subtract(itemModel.finalDiscount));
                if (taxes.containsKey(model2)) {
                    BigDecimal taxGroupValue =itemModel.isTaxable == false ? BigDecimal.ZERO : taxes.get(model2);
                    currentTax = taxGroupValue.add(currentTax);

                    BigDecimal sub = subtotals.get(model2);
                    subTotal = sub.add(subTotal);
                }
                taxes.put(model2, currentTax);
                subtotals.put(model2, subTotal);
            }

            totalSubtotal = totalSubtotal.add(itemSubtotal);
            totalDiscount = totalDiscount.add(itemDiscount);
            totalTax = totalTax.add(itemModel.isTaxable == false ? BigDecimal.ZERO :itemTax);
            List<Unit> units;
            if (childOrderGuid != null) {
                units = loadUnitItemsForRefund(context, item.itemModel.itemGuid, childOrderGuid);
            } else if (orderGuid != null) {
                units = loadUnitItems(context, item.itemModel.itemGuid, orderGuid);
            } else {
                units = null;
            }

            handler.handleItem(item.getSaleItemGuid(), item.description,
                    item.unitsLabel, itemModel.priceType,
                    itemQty, itemSubtotal, itemDiscount,
                    itemTax, singleItemPrice, units, item.modifiers, transactionFee, item.getPrice(), item.getNotes(), model1, model2);
        }

        handler.handleTotal(totalSubtotal, subtotals, totalDiscount, totalTax, tips, transactionFee, taxes);
    }

    public interface PrintHandler {
        void handleItem(String saleItemGuid,
                        String description,
                        String unitLabel,
                        PriceType priceType,
                        BigDecimal qty,
                        BigDecimal itemSubtotal,
                        BigDecimal itemDiscount,
                        BigDecimal itemTax,
                        BigDecimal singleItemPrice,
                        List<Unit> units,
                        ArrayList<SaleOrderItemViewModel.AddonInfo> addons,
                        BigDecimal transactionFee,
                        BigDecimal itemFullPrice,
                        String note,
                        TaxGroupModel model1,
                        TaxGroupModel model2);

        void handleTotal(BigDecimal totalSubtotal,
                         Map<TaxGroupModel, BigDecimal> subtotals,
                         BigDecimal totalDiscount,
                         BigDecimal totalTax,
                         BigDecimal tipsAmount,
                         BigDecimal transactionFee,
                         Map<TaxGroupModel, BigDecimal> taxes);
    }

    public interface LotteryHandler {
        void handleTotal(BigDecimal totalSubtotal, BigDecimal totalDiscount, BigDecimal totalTax, BigDecimal tipsAmount, BigDecimal transactionFee, BigDecimal totalCashBack);
    }

    private static List<SaleOrderItemViewModel> loadItems(Context context, String orderGuid) {
        assert context != null;
        assert orderGuid != null;
        return _wrap(ProviderAction
                        .query(SALE_ITEMS_URI)
                        .where(SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                        .orderBy(SaleItemTable.SEQUENCE)
                        .perform(context),
                new SaleOrderItemViewModelWrapFunction(context)
        );
    }

    private static List<Unit> loadUnitItemsForRefund(Context context, String itemId, String childOrderGuid) {
        assert context != null;
        assert childOrderGuid != null;
        return _wrap(ProviderAction
                        .query(SALE_UNITS_URI)
                        .where(ShopStore.UnitTable.CHILD_ORDER_ID + " = ?", childOrderGuid)
                        .where(ShopStore.UnitTable.ITEM_ID + " = ?", itemId)
                        .perform(context),
                new UnitWrapFunction()
        );
    }

    private static List<Unit> loadUnitItems(Context context, String itemId, String orderGuid) {
        assert context != null;
        assert orderGuid != null;
        return _wrap(ProviderAction
                        .query(SALE_UNITS_URI)
                        .where(ShopStore.UnitTable.SALE_ORDER_ID + " = ?", orderGuid)
                        .where(ShopStore.UnitTable.ITEM_ID + " = ?", itemId)
                        .perform(context),
                new UnitWrapFunction()
        );
    }

    public static BigDecimal loadTips(Context context, String orderGuid) {
        BigDecimal tips = BigDecimal.ZERO;
        Cursor c = ProviderAction.query(TIPS_URI)
                .projection(EmployeeTipsTable.AMOUNT)
                .where(EmployeeTipsTable.ORDER_ID + " = ?", orderGuid)
                .perform(context);

        if (c.moveToFirst())
            tips = _decimal(c, 0);
        c.close();

        return tips;
    }

}
