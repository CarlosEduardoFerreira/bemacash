package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleOrderInfo;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.reports.XReportQuery.SaleItemInfo2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleReportItemsView2.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopSchema2.SaleReportItemsView2.CategoryTable;
import com.kaching123.tcr.store.ShopSchema2.SaleReportItemsView2.DepartmentTable;
import com.kaching123.tcr.store.ShopSchema2.SaleReportItemsView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleReportItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleReportItemsView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleReportItemsView;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;

/**
 * Created by gdubina on 23.01.14.
 */
public abstract class SalesBaseReportQuery<T extends IReportResult> {

    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(SaleReportItemsView.URI_CONTENT);
    private static final Uri URI_BILL_DESCRIPTION = ShopProvider.getContentUri(ShopStore.BillPaymentDescriptionTable.URI_CONTENT);

    private static boolean isSale;

    public SalesBaseReportQuery() {
        this(true);
    }

    public SalesBaseReportQuery(boolean isSale) {
        this.isSale = isSale;
    }

    public Collection<T> getItems(Context context, long startTime, long endTime, long registerId) {
        return getItems(context, startTime, endTime, registerId, OrderType.SALE);
    }

    public Collection<T> getItems(Context context, long startTime, String shiftGuid) {
        return getItems(context, startTime, shiftGuid, OrderType.SALE);
    }

    public Collection<T> getItems(Context context, long startTime, String shiftGuid, OrderType orderType) {

        Query query = ProviderAction.query(URI_SALE_ITEMS);
        if (startTime > 0)
            query.where(SaleOrderTable.CREATE_TIME + " >= ? ", startTime);
        if (isSale) {
            query.where("(" + SaleOrderTable.STATUS + " = ? or " + SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal());
        } else {
            query.where(SaleOrderTable.STATUS + " = ?", OrderStatus.RETURN.ordinal());
        }
        if (orderType != null) {
            query.where(SaleOrderTable.ORDER_TYPE + " = ?", orderType.ordinal());
        }

        Cursor c = query.perform(context);

        HashMap<String, SaleOrderInfo> ordersInfo = readCursor(c, context);
        c.close();

        SalesReportHandler handler2 = createHandler();

        for (Entry<String, SaleOrderInfo> e : ordersInfo.entrySet()) {
            XReportQuery.calculate(e.getValue(), handler2);
        }

        return handler2.getResult();
    }

    public Collection<T> getItems(Context context, long startTime, long endTime, long registerId, OrderType orderType) {

        Query query = ProviderAction.query(URI_SALE_ITEMS)
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime);
        if (isSale) {
            query.where("(" + SaleOrderTable.STATUS + " = ? or " + SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal());
        } else {
            query.where(SaleOrderTable.STATUS + " = ?", OrderStatus.RETURN.ordinal());
        }
        if (registerId > 0) {
            query.where(SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }
        if (orderType != null) {
            query.where(SaleOrderTable.ORDER_TYPE + " = ?", orderType.ordinal());
        }

        Cursor c = query.perform(context);

        HashMap<String, SaleOrderInfo> ordersInfo = readCursor(c, context);
        c.close();

        SalesReportHandler handler2 = createHandler();

        for (Entry<String, SaleOrderInfo> e : ordersInfo.entrySet()) {
            XReportQuery.calculate(e.getValue(), handler2);
        }

        return handler2.getResult();
    }

    protected abstract SalesReportHandler<T> createHandler();


    private static HashMap<String, SaleOrderInfo> readCursor(Cursor c, Context context) {
        if (c == null) {
            return new HashMap<String, SaleOrderInfo>(0);
        }
        HashMap<String, SaleOrderInfo> result = new HashMap<String, SaleOrderInfo>();
        if (c.moveToFirst()) {
            do {
                String orderGuid = c.getString(c.getColumnIndex(SaleItemTable.ORDER_GUID));
                SaleOrderInfo r = result.get(orderGuid);
                if (r == null) {
                    c.getColumnIndex(SaleOrderTable.TAXABLE);
                    c.getColumnIndex(SaleOrderTable.DISCOUNT);
                    c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE);
                    c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE);
                    r = new SaleOrderInfo(_bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.DISCOUNT), BigDecimal.ZERO),
                            _discountType(c, c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE), BigDecimal.ZERO));
                    result.put(orderGuid, r);
                }
                readCursorRow(c, r, context);
            } while (c.moveToNext());
        }

        return result;
    }

    private static void readCursorRow(Cursor c, SaleOrderInfo result, Context context) {
        String saleItemId = c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID));

        SaleItemInfo2 value = (SaleItemInfo2) result.map.get(saleItemId);
        if (value == null) {
            OrderType orderType = _orderType(c, c.getColumnIndex(SaleOrderTable.ORDER_TYPE));
            int descIndex = orderType == OrderType.SALE ? c.getColumnIndex(ItemTable.DESCRIPTION) : c.getColumnIndex(BillPaymentDescriptionTable.DESCRIPTION);
            String description = null;
            String productId = null;
            if ((c.getString(descIndex) == null || c.getString(descIndex).equalsIgnoreCase(""))){
                Query query = ProviderAction.query(URI_BILL_DESCRIPTION);
                if (!isSale)
                    query.where(ShopStore.BillPaymentDescriptionTable.ORDER_ID + " = ? ", c.getString(c.getColumnIndex(SaleItemTable.PARENT_GUID)));
                else
                    query.where(ShopStore.BillPaymentDescriptionTable.ORDER_ID + " = ? ", c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID)));
                Cursor temp = query.perform(context);

                if (temp.moveToFirst()) {
                    description = temp.getString(temp.getColumnIndex(ShopStore.BillPaymentDescriptionTable.DESCRIPTION));
                    productId = temp.getString(temp.getColumnIndex(ShopStore.BillPaymentDescriptionTable.PREPAID_ORDER_ID));
                }
                temp.close();
            }

            value = new SaleItemInfo2(
                    saleItemId,
                    c.getString(c.getColumnIndex(SaleItemTable.ITEM_GUID)),
                    description == null ? c.getString(descIndex) : description,
                    c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                    productId == null ? c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)) : productId,
                    _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY), BigDecimal.ZERO),
                    _decimal(c, c.getColumnIndex(SaleItemTable.PRICE), BigDecimal.ZERO),
                    _bool(c, c.getColumnIndex(SaleItemTable.DISCOUNTABLE)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.DISCOUNT), BigDecimal.ZERO),
                    _discountType(c, c.getColumnIndex(SaleItemTable.DISCOUNT_TYPE)),
                    _bool(c, c.getColumnIndex(SaleItemTable.TAXABLE)),
                    _decimal(c, c.getColumnIndex(SaleItemTable.TAX), BigDecimal.ZERO),
                    _decimal(c, c.getColumnIndex(SaleItemTable.TAX2), BigDecimal.ZERO),
                    _decimal(c, c.getColumnIndex(ItemTable.COST), BigDecimal.ZERO),
                    c.getString(c.getColumnIndex(CategoryTable.DEPARTMENT_GUID)),
                    c.getString(c.getColumnIndex(DepartmentTable.TITLE)),
                    c.getString(c.getColumnIndex(ItemTable.CATEGORY_ID)),
                    c.getString(c.getColumnIndex(CategoryTable.TITLE)),
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
}
