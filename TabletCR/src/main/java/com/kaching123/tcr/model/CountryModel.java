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
 * Created by idyuzheva on 06.10.2014.
 */
public class CountryModel implements Serializable, com.kaching123.tcr.model.IValueModel {

    private static final Uri URI_COUNTRY = ShopProvider.contentUri(ShopStore.CountryTable.URI_CONTENT);

    public long sid;
    public String id;
    public String name;

    private List<String> mIgnoreFields;

    public CountryModel(Cursor c) {
        this(c.getLong(c.getColumnIndex(ShopStore.CountryTable.SID)),
                c.getString(c.getColumnIndex(ShopStore.CountryTable.ID)),
                c.getString(c.getColumnIndex(ShopStore.CountryTable.NAME)), null);

    }

    public CountryModel(long sid, String id, String name, List<String> ignoreFields) {
        this.sid = sid;
        this.id = id;
        this.name = name;

        this.mIgnoreFields = ignoreFields;
    }

    public static CountryModel getCountryById(final Context context, final String id) {
        try (
                Cursor cursor = ProviderAction.query(URI_COUNTRY)
                        .where(ShopStore.CountryTable.ID + " = ?", id)
                        .perform(context)
        ) {
            CountryModel model = null;
            if (cursor != null && cursor.moveToFirst()) {
                model = new CountryModel(cursor);
            }
            return model;
        }
    }

    @Override
    public String getGuid() {
        return String.valueOf(sid);
    }

    @Override
    public ContentValues toValues() {
        final ContentValues values = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.CountryTable.SID)) values.put(ShopStore.CountryTable.SID, sid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.CountryTable.ID)) values.put(ShopStore.CountryTable.ID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.CountryTable.NAME)) values.put(ShopStore.CountryTable.NAME, name);
        return values;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.CountryTable.ID;
    }

}
