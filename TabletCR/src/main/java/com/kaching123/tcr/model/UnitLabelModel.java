package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.function.UnitLabelWrapFunction;
import com.kaching123.tcr.model.converter.UnitLabelFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.UnitLabelTable;

import java.io.Serializable;
import java.util.List;
/**
 * Created by alboyko 07.12.2015
 */
public class UnitLabelModel implements Serializable, IValueModel {

    private static final long serialVersionUID = 1L;
    private static final Uri URI_UNIT_LABEL = ShopProvider.contentUri(UnitLabelTable.URI_CONTENT);

    public String guid;
    public String description;
    public String shortcut;

    public UnitLabelModel(String guid, String description, String shortCut) {
        this.guid = guid;
        this.description = description;
        this.shortcut = shortCut;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(UnitLabelTable.GUID, guid);
        cv.put(UnitLabelTable.DESCRIPTION, description);
        cv.put(UnitLabelTable.SHORTCUT, shortcut);
        return cv;
    }

    public ContentValues toUpdateValues() {
        ContentValues cv = new ContentValues();
        cv.put(UnitLabelTable.DESCRIPTION, description);
        cv.put(UnitLabelTable.SHORTCUT, shortcut);
        return cv;
    }

    @Override
    public String toString() {
        return description + " as " + shortcut;
    }


    public static UnitLabelModel getDefaultModel(Context context) {
        final Cursor cursor = ProviderAction.query(URI_UNIT_LABEL)
                .orderBy(UnitLabelTable.UPDATE_TIME)
                .perform(context);
        UnitLabelModel unitLabelModel = null;
        List<UnitLabelModel> models = null;
        if (cursor != null && cursor.moveToFirst()) {
            models = new UnitLabelWrapFunction().apply(cursor);
            cursor.close();
        }
        if (models != null && !models.isEmpty())
                unitLabelModel = models.get(0);

        return unitLabelModel;
    }

    public static UnitLabelModel getById(final Context context, final String guid) {
        return getByProperty(context, UnitLabelTable.GUID, guid);
    }

    public static UnitLabelModel getByShortcut(final Context context, final String shortcut) {
        return getByProperty(context, UnitLabelTable.SHORTCUT, shortcut);
    }

    private static UnitLabelModel getByProperty(Context context, String property, String propertyValue) {
        final Cursor cursor = ProviderAction.query(URI_UNIT_LABEL)
                .where(property + " = ?", propertyValue)
                .perform(context);
        UnitLabelModel unitLabel = null;
        if (cursor != null && cursor.moveToFirst()) {
            unitLabel = new UnitLabelFunction().apply(cursor);
            cursor.close();
        }
        return unitLabel;
    }

    /**
     * Get actual unit label shortcut for current item.
     * @param context context
     * @param item - item model
     * @return actual unit label shortcut
     * */
    public static String getUnitLabelShortcut(Context context, ItemModel item) {
        return getUnitLabelShortcut(context, item.unitsLabel, item.unitsLabelId);
    }

    /**
     * Get actual unit label shortcut for current item.
     * @param context context
     * @param unitLabel - item model unitLabel
     * @param unitLabelId - item model unitLabelId
     * @return actual unit label shortcut
     * */
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
}
