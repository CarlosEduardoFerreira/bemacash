package com.kaching123.tcr.model.converter;

import android.database.Cursor;
import android.text.TextUtils;

import com.google.common.base.Function;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleAddonTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.UnitLabelTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.ContentValuesUtilBase;

import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;

/**
 * Created by gdubina on 07/11/13.
 */
public class SaleOrderItemViewFunction implements Function<Cursor, SaleOrderItemViewModel> {

    @Override
    public SaleOrderItemViewModel apply(Cursor c) {
        SaleOrderItemModel itemModel = new SaleOrderItemModel(
                c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID)),
                c.getString(c.getColumnIndex(SaleItemTable.ORDER_GUID)),
                c.getString(c.getColumnIndex(ItemTable.GUID)),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY), BigDecimal.ZERO),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.KITCHEN_PRINTED_QTY), BigDecimal.ZERO),
                _priceType(c, c.getColumnIndex(SaleItemTable.PRICE_TYPE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.PRICE), BigDecimal.ZERO),
                _bool(c, c.getColumnIndex(SaleItemTable.DISCOUNTABLE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.DISCOUNT), BigDecimal.ZERO),
                _discountType(c, c.getColumnIndex(SaleItemTable.DISCOUNT_TYPE)),
                _bool(c, c.getColumnIndex(SaleItemTable.IS_MULTIPLE_DISCOUNT)),
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
                _bool(c, c.getColumnIndex(SaleItemTable.POINTS_FOR_DOLLAR_AMOUNT)));

        SaleOrderItemAddonModel saleAddon = null;
        String addonGuid = c.getString(c.getColumnIndex(SaleAddonTable.ADDON_GUID));
        if (!TextUtils.isEmpty(addonGuid)) {
            saleAddon = new SaleOrderItemAddonModel(
                    c.getString(c.getColumnIndex(SaleAddonTable.GUID)),
                    addonGuid,
                    c.getString(c.getColumnIndex(SaleAddonTable.ITEM_GUID)),
                    _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST), BigDecimal.ZERO),
                    _modifierType(c, c.getColumnIndex(SaleAddonTable.TYPE)),
                    c.getString(c.getColumnIndex(ShopStore.SaleAddonTable.CHILD_ITEM_ID)),
                    ContentValuesUtilBase._decimalQty(c, c.getColumnIndex(ShopStore.SaleAddonTable.CHILD_ITEM_QTY))
            );
        }

        TaxGroupModel taxModel1 = new TaxGroupModel(c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.TaxGroupTable.GUID)),
                c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.TaxGroupTable.TITLE)),
                ContentValuesUtilBase._decimal(c, c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.TaxGroupTable.TAX)));
        TaxGroupModel taxModel2 = new TaxGroupModel(c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.TaxGroupTable2.GUID)),
                c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.TaxGroupTable2.TITLE)),
                ContentValuesUtilBase._decimal(c, c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.TaxGroupTable2.TAX)));
        return new SaleOrderItemViewModel(
                itemModel,
                c.getString(c.getColumnIndex(ItemTable.DESCRIPTION)),
                null,//stub
                c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)),
                c.getString(c.getColumnIndex(UnitLabelTable.SHORTCUT)),
                _bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                _bool(c, c.getColumnIndex(ItemTable.SERIALIZABLE)),
                _decimal(c, c.getColumnIndex(SaleOrderTable.DISCOUNT), BigDecimal.ZERO),
                _discountType(c, c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE)),
                _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE), BigDecimal.ZERO),
                !c.isNull(c.getColumnIndex(ItemTable.PRINTER_ALIAS_GUID)),
                c.getInt(c.getColumnIndex(SaleItemTable.IS_PREPAID_ITEM)) == 1,
                c.getInt(c.getColumnIndex(SaleItemTable.IS_GIFT_CARD)) == 1,
                taxModel1, taxModel2
        );
    }
}
