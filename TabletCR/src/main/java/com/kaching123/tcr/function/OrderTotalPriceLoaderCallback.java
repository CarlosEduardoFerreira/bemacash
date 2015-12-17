package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleItemInfo;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleOrderCostInfo;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleOrderInfo;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleAddonSubItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleAddonTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;

import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;

/**
 * @author Ivan v. Rikhmayer
 */
public abstract class OrderTotalPriceLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final Uri URI_ORDER_ITEMS = ShopProvider.getContentUri(SaleOrderItemsView.URI_CONTENT);
    public static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);

    public static final String[] PROJECTION = new String[]{
            SaleItemTable.ORDER_GUID,
            SaleItemTable.SALE_ITEM_GUID,
            SaleItemTable.ITEM_GUID,
            ItemTable.DESCRIPTION,
            SaleItemTable.QUANTITY,
            SaleItemTable.PRICE,
            SaleItemTable.DISCOUNTABLE,
            SaleItemTable.DISCOUNT,
            SaleItemTable.DISCOUNT_TYPE,
            SaleItemTable.TAXABLE,
            SaleItemTable.TAX,
            SaleAddonTable.EXTRA_COST,
            SaleAddonTable.TYPE,
            SaleAddonTable.CHILD_ITEM_ID,
            SaleAddonTable.CHILD_ITEM_QTY,
            SaleAddonSubItemTable.SALE_PRICE,
            SaleOrderTable.TAXABLE,
            SaleOrderTable.DISCOUNT,
            SaleOrderTable.DISCOUNT_TYPE,
            SaleOrderTable.TRANSACTION_FEE};

    private Context context;

    private String GUID;

    public OrderTotalPriceLoaderCallback(Context context, String GUID) {
        /*if (context == null || TextUtils.isEmpty(GUID)) {

        }*/
        this.context = context;
        this.GUID = GUID;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder.forUri(URI_ORDER_ITEMS)
                .projection(PROJECTION)
                .where(SaleItemTable.ORDER_GUID + " = ? ", GUID)
                .orderBy(SaleItemTable.SEQUENCE)
                .build(context);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        culcSubtotal(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        culcSubtotal(null);
    }

    public abstract void onZeroValue();

    public abstract void onCalcTotal(
            boolean isTaxableOrder,
            BigDecimal orderDiscount, DiscountType orderDiscountType, BigDecimal orderDiscountVal,
            BigDecimal totalItemTotal,
            BigDecimal totalTaxVatValue,
            BigDecimal totalItemDiscount, BigDecimal totalOrderPrice,
            BigDecimal availableDiscount,
            BigDecimal transactionFee);

    private void culcSubtotal(Cursor c) {

        if (c == null) {
            onZeroValue();
            return;
        }
        SaleOrderInfo info = readCursor(c);
        SaleOrderCostInfo result = OrderTotalPriceCalculator.calculate(info);

        onCalcTotal(result.isTaxableOrder,
                result.orderDiscount,
                result.orderDiscountType,
                result.tmpOderDiscountVal,
                result.subTotalItemTotal,
                result.totalTaxVatValue,
                result.totalItemDiscount,
                result.totalOrderPrice, result.totalDiscountableItemTotal, info.transactionFee);
    }

    private static SaleOrderInfo readCursor(Cursor c) {
        if (c == null)
            return null;

        SaleOrderInfo result = null;

        if (c.moveToFirst()) {
            do {
                if (result == null) {
                    result = new SaleOrderInfo(_bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.DISCOUNT)),
                            _discountType(c, c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE)));
                }
                readCursorRow(c, result);
            } while (c.moveToNext());
        }
        return result == null ? new SaleOrderInfo(false, BigDecimal.ZERO, DiscountType.VALUE, BigDecimal.ZERO) : result;
    }

    public static void readCursorRow(Cursor c, SaleOrderInfo result) {
        String saleItemId = c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID));

        SaleItemInfo value = result.map.get(saleItemId);
        if (value == null) {
            value = new SaleItemInfo(
                    saleItemId,
                    c.getString(c.getColumnIndex(SaleItemTable.ITEM_GUID)),
                    c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)),
                    _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.PRICE)),
                    _bool(c, c.getColumnIndex(SaleItemTable.DISCOUNTABLE)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.DISCOUNT)),
                    _discountType(c, c.getColumnIndex(SaleItemTable.DISCOUNT_TYPE)),
                    _bool(c, c.getColumnIndex(SaleItemTable.TAXABLE)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.TAX)));

            result.map.put(saleItemId, value);
        }
        /*
        BigDecimal extra = _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST));
        if (extra != null && value.totalPrice != null) {
            value.totalPrice = value.totalPrice.add(extra);
        }*/
        BigDecimal extra;
        if (_modifierType(c, c.getColumnIndex(SaleAddonTable.TYPE)) == ModifierType.OPTIONAL){
            extra = null;
        } else if (c.getString(c.getColumnIndex(SaleAddonTable.CHILD_ITEM_ID)) != null) {
            extra = getSubTotal(_decimalQty(c, c.getColumnIndex(SaleAddonTable.CHILD_ITEM_QTY)), _decimal(c, c.getColumnIndex(SaleAddonSubItemTable.SALE_PRICE)));
        } else {
            extra = _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST));
        }

        if (extra != null && value.totalPrice != null) {
            value.totalPrice = value.totalPrice.add(extra);
        }

    }


}
