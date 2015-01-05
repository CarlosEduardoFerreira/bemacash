package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by pkabakov on 25/12/13.
 */
public class TaxGroupModel implements IValueModel, Serializable {

    public final String guid;
    public final String title;
    public final BigDecimal tax;

    public TaxGroupModel(String guid, String title, BigDecimal tax) {
        this.guid = guid;
        this.title = title;
        this.tax = tax;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.TaxGroupTable.GUID, guid);
        values.put(ShopStore.TaxGroupTable.TITLE, title);
        values.put(ShopStore.TaxGroupTable.TAX, _decimal(tax));
        return values;
    }

    public ContentValues toUpdateValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.TaxGroupTable.TITLE, title);
        values.put(ShopStore.TaxGroupTable.TAX, _decimal(tax));
        return values;
    }

}
