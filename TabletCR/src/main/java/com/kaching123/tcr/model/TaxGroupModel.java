package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.TaxGroupTable;

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
    public boolean isDefault;

    public TaxGroupModel(String guid, String title, BigDecimal tax) {
        this(guid, title, tax, false);
    }

    public TaxGroupModel(String guid, String title, BigDecimal tax, boolean isDefault) {
        this.guid = guid;
        this.title = title;
        this.tax = tax;
        this.isDefault = isDefault;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(TaxGroupTable.GUID, guid);
        values.put(TaxGroupTable.TITLE, title);
        values.put(TaxGroupTable.TAX, _decimal(tax, 3));
        values.put(TaxGroupTable.IS_DEFAULT, isDefault ? 1 : 0);
        return values;
    }

    public ContentValues toUpdateValues() {
        ContentValues values = new ContentValues();
        values.put(TaxGroupTable.TITLE, title);
        values.put(TaxGroupTable.TAX, _decimal(tax, 3));
        values.put(TaxGroupTable.IS_DEFAULT, isDefault ? 1 : 0);
        return values;
    }

}
