package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ComposerTable;
import com.kaching123.tcr.util.ContentValuesUtilBase;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;


/**
 * Created by mayer
 */
public class ComposerModel implements IValueModel, Serializable {

    private static final Uri URI_ITEM = ShopProvider.contentUri(ShopStore.ComposerTable.URI_CONTENT);

    public String guid;
    public String itemHostId;
    public String itemChildId;
    public BigDecimal qty;
    public boolean tracked;
    public boolean restricted;

    private List<String> mIgnoreFields;

    public ComposerModel() {
    }

    public ComposerModel(String guid,
                         String itemHostId,
                         String itemChildId,
                         BigDecimal qty,
                         boolean storeTrackingEnabled,
                         boolean freeOfChargeComposer,
                         List<String> ignoreFields) {
        this.guid = guid;
        this.itemHostId = itemHostId;
        this.itemChildId = itemChildId;
        this.qty = qty;
        this.tracked = storeTrackingEnabled;
        this.restricted = freeOfChargeComposer;

        this.mIgnoreFields = ignoreFields;
    }

    public ComposerModel(Cursor c) {
        this.guid                   = c.getString(c.getColumnIndex(ComposerTable.ID));
        this.itemHostId             = c.getString(c.getColumnIndex(ComposerTable.ITEM_HOST_ID));
        this.itemChildId            = c.getString(c.getColumnIndex(ComposerTable.ITEM_CHILD_ID));
        this.qty                    = _decimalQty(c, c.getColumnIndex(ComposerTable.QUANTITY), BigDecimal.ZERO);
        this.tracked = _bool(c, c.getColumnIndex(ComposerTable.STORE_TRACKING_ENABLED));
        this.restricted = _bool(c, c.getColumnIndex(ComposerTable.FREE_OF_CHARGE_COMPOSER));
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ComposerTable.ID)) v.put(ComposerTable.ID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ComposerTable.ITEM_HOST_ID)) v.put(ComposerTable.ITEM_HOST_ID, itemHostId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ComposerTable.ITEM_CHILD_ID)) v.put(ComposerTable.ITEM_CHILD_ID, itemChildId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ComposerTable.QUANTITY)) v.put(ComposerTable.QUANTITY, _decimalQty(qty));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ComposerTable.STORE_TRACKING_ENABLED)) v.put(ComposerTable.STORE_TRACKING_ENABLED, tracked);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ComposerTable.FREE_OF_CHARGE_COMPOSER)) v.put(ComposerTable.FREE_OF_CHARGE_COMPOSER, restricted);
        return v;
    }

    @Override
    public String getIdColumn() {
        return ComposerTable.ID;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        v.put(ComposerTable.ITEM_CHILD_ID, itemChildId);
        v.put(ComposerTable.QUANTITY, _decimalQty(qty));
        v.put(ComposerTable.STORE_TRACKING_ENABLED, tracked);
        v.put(ComposerTable.FREE_OF_CHARGE_COMPOSER, restricted);
        return v;
    }

    public static HashMap<String, BigDecimal> getChildsGuidQtyByHostId(final Context context, final String itemGuid) {
        try (
                Cursor c = ProviderAction.query(URI_ITEM)
                        .where(ComposerTable.ITEM_HOST_ID + " = ?", itemGuid)
                        .perform(context)
        ) {
            HashMap<String, BigDecimal> childs = new HashMap<>();
            if (c != null) {
                while (c.moveToNext()) {
                    childs.put(c.getString(c.getColumnIndex(ComposerTable.ITEM_CHILD_ID)), _decimalQty(c, c.getColumnIndex(ComposerTable.QUANTITY)));
                }
            }
            return childs;
        }
    }

    public static List<ComposerModel> getChildsByHostId(final Context context, final String itemGuid) {
        Cursor c = ProviderAction.query(URI_ITEM)
                    .where(ComposerTable.ITEM_HOST_ID + " = ?", itemGuid)
                    .perform(context);

            List<ComposerModel> childs = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    childs.add(new ComposerModel(c));
                }
                c.close();
            }
            return childs;
    }
}
