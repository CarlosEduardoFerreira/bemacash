package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;

import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;

/**
 * Created by gdubina on 07/11/13.
 */
public class SaleOrderItemFunction implements Function<Cursor, SaleOrderItemModel> {

    @Override
    public SaleOrderItemModel apply(Cursor c) {
        return new SaleOrderItemModel(
                c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID)),
                c.getString(c.getColumnIndex(SaleItemTable.ORDER_GUID)),
                c.getString(c.getColumnIndex(SaleItemTable.ITEM_GUID)),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY), BigDecimal.ZERO),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.KITCHEN_PRINTED_QTY), BigDecimal.ZERO),
                _priceType(c, c.getColumnIndex(SaleItemTable.PRICE_TYPE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.PRICE), BigDecimal.ZERO),
                _bool(c, c.getColumnIndex(SaleItemTable.DISCOUNTABLE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.DISCOUNT), BigDecimal.ZERO),
                _discountType(c, c.getColumnIndex(SaleItemTable.DISCOUNT_TYPE)),
                _bool(c, c.getColumnIndex(SaleItemTable.TAXABLE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.TAX), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(SaleItemTable.TAX2), BigDecimal.ZERO),
                c.getLong(c.getColumnIndex(SaleItemTable.SEQUENCE)),
                c.getString(c.getColumnIndex(SaleItemTable.PARENT_GUID)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_TAX), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT), BigDecimal.ZERO),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.TMP_REFUND_QUANTITY), BigDecimal.ZERO),
                c.getString(c.getColumnIndex(SaleItemTable.NOTES)),
                c.getInt(c.getColumnIndex(SaleItemTable.HAS_NOTES)) == 1,
                c.getInt(c.getColumnIndex(SaleItemTable.IS_PREPAID_ITEM)) == 1,
                c.getInt(c.getColumnIndex(SaleItemTable.IS_GIFT_CARD)) == 1,
                _decimal(c, c.getColumnIndex(SaleItemTable.LOYALTY_POINTS), BigDecimal.ZERO),
                _bool(c, c.getColumnIndex(SaleItemTable.POINTS_FOR_DOLLAR_AMOUNT)),
                c.getString(c.getColumnIndex(SaleItemTable.DISCOUNT_BUNDLE_ID)),
                _bool(c, c.getColumnIndex(SaleItemTable.EBT_ELIGIBLE)));
    }
}
