package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;

import org.json.JSONException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleComposerTable;
import com.kaching123.tcr.util.JdbcJSONObject;

import static com.kaching123.tcr.util.ContentValuesUtilBase._bool;
import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;

/**
 * Created by vkompaniets on 08.02.2016.
 */
public class SaleComposerModel implements IValueModel, Serializable {

    public String id;
    public String saleItemId;
    public String saleModifierId;
    public String hostItemId;
    public String childItemId;
    public BigDecimal qty;
    public boolean stockTracking;
    public boolean restrictQty;

    private List<String> mIgnoreFields;

    public SaleComposerModel(String id, String saleItemId, String saleModifierId, String hostItemId, String childItemId, BigDecimal qty, boolean stockTracking, boolean restrictQty, List<String> ignoreFields) {
        this.id = id;
        this.saleItemId = saleItemId;
        this.saleModifierId = saleModifierId;
        this.hostItemId = hostItemId;
        this.childItemId = childItemId;
        this.qty = qty;
        this.stockTracking = stockTracking;
        this.restrictQty = restrictQty;

        this.mIgnoreFields = ignoreFields;
    }

    public SaleComposerModel(JdbcJSONObject json) throws JSONException {
        this.id = json.getString(SaleComposerTable.ID);
        this.saleItemId = json.getString(SaleComposerTable.SALE_ITEM_ID);
        this.saleModifierId = json.getString(SaleComposerTable.SALE_MODIFIER_ID);
        this.hostItemId = json.getString(SaleComposerTable.HOST_ITEM_ID);
        this.childItemId = json.getString(SaleComposerTable.CHILD_ITEM_ID);
        this.qty = json.getBigDecimal(SaleComposerTable.QTY);
        this.stockTracking = json.getBoolean(SaleComposerTable.STOCK_TRACKING_ENABLED);
        this.restrictQty = json.getBoolean(SaleComposerTable.RESTRICT_QTY);
    }

    public SaleComposerModel(Cursor c){
        this(
                c.getString(c.getColumnIndex(SaleComposerTable.ID)),
                c.getString(c.getColumnIndex(SaleComposerTable.SALE_ITEM_ID)),
                c.getString(c.getColumnIndex(SaleComposerTable.SALE_MODIFIER_ID)),
                c.getString(c.getColumnIndex(SaleComposerTable.HOST_ITEM_ID)),
                c.getString(c.getColumnIndex(SaleComposerTable.CHILD_ITEM_ID)),
                _decimalQty(c, c.getColumnIndex(SaleComposerTable.QTY)),
                _bool(c, c.getColumnIndex(SaleComposerTable.STOCK_TRACKING_ENABLED)),
                _bool(c, c.getColumnIndex(SaleComposerTable.RESTRICT_QTY)), null);

    }

    @Override
    public String getGuid() {
        return id;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleComposerTable.ID)) values.put(SaleComposerTable.ID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleComposerTable.SALE_ITEM_ID)) values.put(SaleComposerTable.SALE_ITEM_ID, saleItemId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleComposerTable.SALE_MODIFIER_ID)) values.put(SaleComposerTable.SALE_MODIFIER_ID, saleModifierId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleComposerTable.HOST_ITEM_ID)) values.put(SaleComposerTable.HOST_ITEM_ID, hostItemId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleComposerTable.CHILD_ITEM_ID)) values.put(SaleComposerTable.CHILD_ITEM_ID, childItemId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleComposerTable.QTY)) values.put(SaleComposerTable.QTY, _decimalQty(qty));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleComposerTable.STOCK_TRACKING_ENABLED)) values.put(SaleComposerTable.STOCK_TRACKING_ENABLED, stockTracking);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleComposerTable.RESTRICT_QTY)) values.put(SaleComposerTable.RESTRICT_QTY, restrictQty);

        return values;
    }

    @Override
    public String getIdColumn() {
        return SaleComposerTable.ID;
    }

    public static List<SaleComposerModel> getByItemGuidAndSaleItemGuid(Context context, String guid, String saleItemGuid) {
        List<SaleComposerModel> items = new ArrayList<>();
        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(SaleComposerTable.URI_CONTENT))
                        .where(SaleComposerTable.CHILD_ITEM_ID + " = ?", guid)
                        .where(SaleComposerTable.SALE_ITEM_ID + " = ?", saleItemGuid)
                        .perform(context)
        ) {
            while (c != null && c.moveToNext()) items.add(new SaleComposerModel(c));
        }
        return items;
    }
}
