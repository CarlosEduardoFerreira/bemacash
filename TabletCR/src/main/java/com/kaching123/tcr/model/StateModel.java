package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import static com.kaching123.tcr.util.ContentValuesUtilBase._decimal;

/**
 * Created by idyuzheva on 06.10.2014.
 */
public class StateModel implements Serializable, IValueModel {

    private static final Uri URI_STATE = ShopProvider.contentUri(ShopStore.StateTable.URI_CONTENT);

    public long id;
    public int code;
    public String name;
    public String abbreviation;
    public String countryId;
    public BigDecimal maxAmount;

    private List<String> mIgnoreFields;

    public StateModel(Cursor c) {
        this(c.getLong(c.getColumnIndex(ShopStore.StateTable.ID)),
                c.getString(c.getColumnIndex(ShopStore.StateTable.NAME)),
                c.getString(c.getColumnIndex(ShopStore.StateTable.COUNTRY_ID)),
                c.getString(c.getColumnIndex(ShopStore.StateTable.ABBREVIATION)),
                c.getInt(c.getColumnIndex(ShopStore.StateTable.CODE)),
                _decimal(c, c.getColumnIndex(ShopStore.StateTable.MAX_SALES_AMOUNT)), null);

    }

    public StateModel(long id,
                      String name,
                      String countryId,
                      String abbreviation,
                      int code,
                      BigDecimal maxAmount, List<String> ignoreFields) {
        this.id = id;
        this.name = name;
        this.countryId = countryId;
        this.abbreviation = abbreviation;
        this.code = code;
        this.maxAmount = maxAmount;

        this.mIgnoreFields = ignoreFields;
    }

    public static StateModel getStateById(final Context context, final int id) {
        try (
                Cursor cursor = ProviderAction.query(URI_STATE)
                        .where(ShopStore.StateTable.ID + " = ?", id)
                        .perform(context)
        ) {
            StateModel model = null;
            if (cursor != null && cursor.moveToFirst()) {
                model = new StateModel(cursor);
            }
            return model;
        }
    }

    @Override
    public String getGuid() {
        return String.valueOf(id);
    }

    @Override
    public ContentValues toValues() {
        final ContentValues values = new ContentValues();

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.StateTable.ID)) values.put(ShopStore.StateTable.ID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.StateTable.NAME)) values.put(ShopStore.StateTable.NAME, name);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.StateTable.COUNTRY_ID)) values.put(ShopStore.StateTable.COUNTRY_ID, countryId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.StateTable.ABBREVIATION)) values.put(ShopStore.StateTable.ABBREVIATION, abbreviation);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.StateTable.CODE)) values.put(ShopStore.StateTable.CODE, code);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.StateTable.MAX_SALES_AMOUNT)) values.put(ShopStore.StateTable.MAX_SALES_AMOUNT, _decimal(maxAmount));

        return values;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.StateTable.ID;
    }

}
