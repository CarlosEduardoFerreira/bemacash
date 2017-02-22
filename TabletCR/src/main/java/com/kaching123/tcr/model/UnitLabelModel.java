package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.UnitLabelWrapFunction;
import com.kaching123.tcr.model.converter.UnitLabelFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.UnitLabelTable;

import java.io.Serializable;
import java.util.List;
/**
 * Created by alboyko 07.12.2015
 */
public class UnitLabelModel implements Serializable, IValueModel {

    private static final long serialVersionUID = 1L;
    private static final Uri URI_UNIT_LABEL = ShopProvider.contentUri(ShopStore.UnitLabelTable.URI_CONTENT);

    public String guid;
    public String description;
    public String shortcut;

    private List<String> mIgnoreFields;

    public UnitLabelModel(String guid, String description, String shortCut, List<String> ignoreFields) {
        this.guid = guid;
        this.description = description;
        this.shortcut = shortCut;

        this.mIgnoreFields = ignoreFields;
    }

    public UnitLabelModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(UnitLabelTable.GUID)),
                c.getString(c.getColumnIndex(UnitLabelTable.DESCRIPTION)),
                c.getString(c.getColumnIndex(UnitLabelTable.SHORTCUT)),
                null
        );
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(UnitLabelTable.GUID)) cv.put(UnitLabelTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(UnitLabelTable.DESCRIPTION)) cv.put(UnitLabelTable.DESCRIPTION, description);
        if (mIgnoreFields == null || !mIgnoreFields.contains(UnitLabelTable.SHORTCUT)) cv.put(UnitLabelTable.SHORTCUT, shortcut);

        return cv;
    }

    public ContentValues toUpdateValues() {
        ContentValues cv = new ContentValues();
        cv.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        cv.put(UnitLabelTable.DESCRIPTION, description);
        cv.put(UnitLabelTable.SHORTCUT, shortcut);
        return cv;
    }

    @Override
    public String toString() {
        return description + " as " + shortcut;
    }


    public static UnitLabelModel getDefaultModel(Context context) {
        try (
                Cursor cursor = ProviderAction.query(URI_UNIT_LABEL)
                        .orderBy(UnitLabelTable.UPDATE_TIME)
                        .perform(context)
        ) {
            UnitLabelModel unitLabelModel = null;
            List<UnitLabelModel> models = null;
            if (cursor != null && cursor.moveToFirst()) {
                models = new UnitLabelWrapFunction().apply(cursor);
            }
            if (models != null && !models.isEmpty())
                unitLabelModel = models.get(0);

            return unitLabelModel;
        }
    }

    public static UnitLabelModel getById(final Context context, final String guid) {
        return getByProperty(context, UnitLabelTable.GUID, guid);
    }

    public static UnitLabelModel getByShortcut(final Context context, final String shortcut) {
        return getByProperty(context, UnitLabelTable.SHORTCUT, shortcut);
    }

    private static UnitLabelModel getByProperty(Context context, String property, String propertyValue) {
        try (
                Cursor cursor = ProviderAction.query(URI_UNIT_LABEL)
                        .where(property + " = ?", propertyValue)
                        .perform(context)
        ) {
            UnitLabelModel unitLabel = null;
            if (cursor != null && cursor.moveToFirst()) {
                unitLabel = new UnitLabelFunction().apply(cursor);
            }
            return unitLabel;
        }
    }

    /**
     * Get actual unit label shortcut for current item.
     *
     * @param context     context
     * @param unitLabel   - item model unitLabel
     * @param unitLabelId - item model unitLabelId
     * @return actual unit label shortcut
     */
    public static String getUnitLabelShortcut(Context context, String unitLabel, String unitLabelId) {
        String shortcut = "";
        UnitLabelModel ulm;
        if (!TextUtils.isEmpty(unitLabel)) {
            shortcut = unitLabel;
        } else if (!TextUtils.isEmpty(unitLabelId)) {
            ulm = getById(context, unitLabelId);
            if (ulm != null) {
                shortcut = ulm.shortcut;
            }
        }
        return shortcut;
    }

    public String getIdColumn() {
        return UnitLabelTable.GUID;
    }

}
