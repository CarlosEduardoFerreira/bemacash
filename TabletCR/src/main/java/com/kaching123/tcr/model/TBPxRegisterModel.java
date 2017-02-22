package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.TBPxRegisterTable;

import java.io.Serializable;

/**
 * Created by vkompaniets on 14.08.2016.
 */
public class TBPxRegisterModel implements IValueModel, Serializable {

    public long id;
    public String tbpId;
    public long registerId;

    public TBPxRegisterModel(long id, String tbpId, long registerId) {
        this.id = id;
        this.tbpId = tbpId;
        this.registerId = registerId;
    }

    @Override
    public String getGuid() {
        return String.valueOf(id);
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(TBPxRegisterTable.ID, id);
        cv.put(TBPxRegisterTable.TBP_ID, tbpId);
        cv.put(TBPxRegisterTable.REGISTER_ID, registerId);
        return cv;
    }

    @Override
    public String getIdColumn() {
        return null;
    }
}
