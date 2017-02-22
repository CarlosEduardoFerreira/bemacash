package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;

import java.io.Serializable;
import java.util.List;

import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

/**
 * Created by idyuzheva on 24.10.2014.
 */
public class MunicipalityModel implements Serializable, com.kaching123.tcr.model.IValueModel {

    private static final Uri URI_MUNICIPALITY = ShopProvider.contentUri(ShopStore.MunicipalityTable.URI_CONTENT);

    public long id;
    public String code;
    public String name;
    public long stateId;
    public String countryId;

    private List<String> mIgnoreFields;

    public MunicipalityModel(Cursor c) {
        this(c.getLong(c.getColumnIndex(ShopStore.MunicipalityTable.ID)),
                c.getString(c.getColumnIndex(ShopStore.MunicipalityTable.CODE)),
                c.getString(c.getColumnIndex(ShopStore.MunicipalityTable.NAME)),
                c.getLong(c.getColumnIndex(ShopStore.MunicipalityTable.STATE_ID)),
                c.getString(c.getColumnIndex(ShopStore.MunicipalityTable.COUNTRY_ID)), null);
    }

    public MunicipalityModel(long id, String code, String name, long stateId, String countryId, List<String> ignoreFields) {
        this.id = id;
        this.name = name;
        this.countryId = countryId;
        this.stateId = stateId;
        this.code = code;

        this.mIgnoreFields = ignoreFields;
    }

    public static MunicipalityModel getById(final Context context, final int id) {
        try (
                Cursor cursor = ProviderAction.query(URI_MUNICIPALITY)
                        .where(ShopStore.MunicipalityTable.ID + " = ?", id)
                        .perform(context)
        ) {
            MunicipalityModel model = null;
            if (cursor != null && cursor.moveToFirst()) {
                model = new MunicipalityModel(cursor);
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
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.MunicipalityTable.ID))
            values.put(ShopStore.MunicipalityTable.ID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.MunicipalityTable.NAME))
            values.put(ShopStore.MunicipalityTable.NAME, name);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.MunicipalityTable.COUNTRY_ID))
            values.put(ShopStore.MunicipalityTable.COUNTRY_ID, countryId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.MunicipalityTable.STATE_ID))
            values.put(ShopStore.MunicipalityTable.STATE_ID, stateId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.MunicipalityTable.CODE))
            values.put(ShopStore.MunicipalityTable.CODE, code);
        return values;
    }

    public String getRawCode() {
        return code.replaceAll("-", "");
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.MunicipalityTable.ID;
    }
}
