package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.SaleAddonTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 06/11/13.
 */
public class SaleOrderItemAddonModel implements IValueModel, Serializable{

    public String guid;
    public String addonGuid;
    public String saleItemGuid;
    public BigDecimal extraCost;
    public ModifierType type;

    public SaleOrderItemAddonModel(String guid, String addonGuid, String saleItemGuid, BigDecimal extraCost, ModifierType type) {
        this.guid = guid;
        this.addonGuid = addonGuid;
        this.saleItemGuid = saleItemGuid;
        this.extraCost = extraCost;
        this.type = type;
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
        return values;
    }
}
