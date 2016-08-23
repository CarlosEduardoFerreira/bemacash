package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.SaleAddonTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;
import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;

/**
 * Created by gdubina on 06/11/13.
 */
public class SaleModifierModel implements IValueModel, Serializable{

    public String guid;
    public String addonGuid;
    public String saleItemGuid;
    public BigDecimal extraCost;
    public ModifierType type;

    // Item, that will be added via modifier, or Linked modifier item, that might be affected, e.g. with no-option.
    public String childItemGuid;

    // item to be added or to be removed. Should be positive
    public BigDecimal childItemQty;


    public SaleModifierModel(String guid,
                             String addonGuid,
                             String saleItemGuid,
                             BigDecimal extraCost,
                             ModifierType type,
                             String childItemGuid,
                             BigDecimal childItemQty) {
        this.guid = guid;
        this.addonGuid = addonGuid;
        this.saleItemGuid = saleItemGuid;
        this.extraCost = extraCost;
        this.type = type;
        this.childItemGuid = childItemGuid;
        this.childItemQty = childItemQty;
    }

    public SaleModifierModel(Cursor c){
        this(
                c.getString(c.getColumnIndex(SaleAddonTable.GUID)),
                c.getString(c.getColumnIndex(SaleAddonTable.ADDON_GUID)),
                c.getString(c.getColumnIndex(SaleAddonTable.ITEM_GUID)),
                _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST), BigDecimal.ZERO),
                _modifierType(c, c.getColumnIndex(SaleAddonTable.TYPE)),
                c.getString(c.getColumnIndex(SaleAddonTable.CHILD_ITEM_ID)),
                _decimalQty(c, c.getColumnIndex(SaleAddonTable.CHILD_ITEM_QTY))
        );
    }

    @Override
    public String getGuid() {
        throw new UnsupportedOperationException("model contains two columns key");
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(SaleAddonTable.GUID, guid);
        values.put(SaleAddonTable.ADDON_GUID, addonGuid);
        values.put(SaleAddonTable.ITEM_GUID, saleItemGuid);
        values.put(SaleAddonTable.EXTRA_COST, _decimal(extraCost));
        values.put(SaleAddonTable.TYPE, _enum(type));
        values.put(SaleAddonTable.CHILD_ITEM_ID, childItemGuid);
        values.put(SaleAddonTable.CHILD_ITEM_QTY, _decimalQty(childItemQty));

        return values;
    }
}
