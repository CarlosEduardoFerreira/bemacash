package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.TaxGroupTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
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

    public TaxGroupModel(Cursor c) {
        this.guid = c.getString(c.getColumnIndex(TaxGroupTable.GUID));
        this.title = c.getString(c.getColumnIndex(TaxGroupTable.TITLE));
        this.tax = _decimal(c, c.getColumnIndex(TaxGroupTable.TAX));
        this.isDefault = _bool(c, c.getColumnIndex(TaxGroupTable.IS_DEFAULT));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaxGroupModel that = (TaxGroupModel) o;

        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return !(tax != null ? !tax.equals(that.tax) : that.tax != null);

    }

    @Override
    public int hashCode() {
        int result = guid != null ? guid.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (tax != null ? tax.hashCode() : 0);
        return result;
    }
}
