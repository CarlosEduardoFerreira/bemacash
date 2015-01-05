package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler2;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;
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

    private static void calculate(Context context, String orderGuid, String childOrderGuid,
                                  List<SaleOrderItemViewModel> items, BigDecimal tips, PrintHandler handler, BigDecimal transactionFee) {
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;


        for (SaleOrderItemViewModel item : items) {
            String itemDescription = item.description;
            if (item.modifier != null) {
                itemDescription = itemDescription + ", " + item.modifier.addonTitle;
            }

            final SaleOrderItemModel itemModel = item.itemModel;
            final BigDecimal itemQty = item.getQty();
            final BigDecimal itemSubtotal = getSubTotal(itemQty, itemModel.finalGrossPrice);
            final BigDecimal itemDiscount = getSubTotal(itemQty, itemModel.finalDiscount);
            final BigDecimal itemTax = getSubTotal(itemQty, itemModel.finalTax);
            final BigDecimal singleItemPrice = itemModel.finalGrossPrice.add(itemModel.finalTax).subtract(itemModel.finalDiscount);

            totalSubtotal = totalSubtotal.add(itemSubtotal);
            totalDiscount = totalDiscount.add(itemDiscount);
            totalTax = totalTax.add(itemTax);
            List<Unit> units;
            if (childOrderGuid != null) {
                units = loadUnitItemsForRefund(context, item.itemModel.itemGuid, childOrderGuid);
            } else if (orderGuid != null) {
                units = loadUnitItems(context, item.itemModel.itemGuid, orderGuid);
            } else {
                units = null;
            }
            ArrayList<SaleOrderItemViewModel.AddonInfo> addons = item.getAddons();
            handler.handleItem(item.getSaleItemGuid(), itemDescription,
                    itemQty, itemSubtotal, itemDiscount,
                    itemTax, singleItemPrice, units, addons, transactionFee, item.fullPrice);
        }

        handler.handleTotal(totalSubtotal, totalDiscount, totalTax, tips, transactionFee);
    }

    public static interface PrintHandler {
        void handleItem(String saleItemGuid, String description,
                        BigDecimal qty, BigDecimal itemSubtotal,
                        BigDecimal itemDiscount, BigDecimal itemTax,
                        BigDecimal singleItemPrice, List<Unit> units,
                        ArrayList<SaleOrderItemViewModel.AddonInfo> addons,
                        BigDecimal transactionFee, BigDecimal itemFullPrice);

        void handleTotal(BigDecimal totalSubtotal, BigDecimal totalDiscount, BigDecimal totalTax, BigDecimal tipsAmount, BigDecimal transactionFee);
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