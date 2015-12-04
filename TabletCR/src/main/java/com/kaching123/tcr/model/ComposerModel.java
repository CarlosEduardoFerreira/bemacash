package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.ComposerTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by mayer
 */
public class ComposerModel implements IValueModel, Serializable {

    public String guid;
    public String itemHostId;
    public String itemChildId;
    public BigDecimal qty;
    public boolean tracked;
    public boolean restricted;

    public ComposerModel() {
    }

    public ComposerModel(String guid,
                         String itemHostId,
                         String itemChildId,
                         BigDecimal qty,
                         boolean storeTrackingEnabled,
                         boolean freeOfChargeComposer) {
        this.guid = guid;
        this.itemHostId = itemHostId;
        this.itemChildId = itemChildId;
        this.qty = qty;
        this.tracked = storeTrackingEnabled;
        this.restricted = freeOfChargeComposer;
    }

    public ComposerModel(Cursor c) {
        this.guid                   = c.getString(c.getColumnIndex(ComposerTable.ID));
        this.itemHostId             = c.getString(c.getColumnIndex(ComposerTable.ITEM_HOST_ID));
        this.itemChildId            = c.getString(c.getColumnIndex(ComposerTable.ITEM_CHILD_ID));
        this.qty                    = _decimalQty(c, c.getColumnIndex(ComposerTable.QUANTITY));
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
        v.put(ComposerTable.ID, guid);
        v.put(ComposerTable.ITEM_HOST_ID, itemHostId);
        v.put(ComposerTable.ITEM_CHILD_ID, itemChildId);
        v.put(ComposerTable.QUANTITY, _decimalQty(qty));
        v.put(ComposerTable.STORE_TRACKING_ENABLED, tracked);
        v.put(ComposerTable.FREE_OF_CHARGE_COMPOSER, restricted);
        return v;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(ComposerTable.ITEM_CHILD_ID, itemChildId);
        v.put(ComposerTable.QUANTITY, _decimalQty(qty));
        v.put(ComposerTable.STORE_TRACKING_ENABLED, tracked);
        v.put(ComposerTable.FREE_OF_CHARGE_COMPOSER, restricted);
        return v;
    }
}
